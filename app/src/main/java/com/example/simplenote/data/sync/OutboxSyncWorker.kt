package com.example.simplenote.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.simplenote.data.local.AppDatabase
import com.example.simplenote.data.local.OutboxEntity
import com.example.simplenote.data.mappers.toEntity
import com.example.simplenote.data.network.RetrofitProvider
import com.example.simplenote.data.remote.NoteCreateRequest
import com.example.simplenote.data.remote.NoteDto
import com.example.simplenote.data.remote.NoteUpdateRequest
import com.example.simplenote.data.remote.NotesApi
import com.google.gson.Gson
import okhttp3.internal.http2.ConnectionShutdownException
import retrofit2.HttpException
import java.io.IOException

private const val TAG = "OutboxSyncWorker"

class OutboxSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val db = AppDatabase.getInstance(appContext)
    private val noteDao = db.noteDao()
    private val outboxDao = db.outboxDao()
    private val api: NotesApi = RetrofitProvider.get(appContext).create(NotesApi::class.java)
    private val gson = Gson()

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork() started")

        val items: List<OutboxEntity> = try {
            outboxDao.allAsc()
        } catch (t: Throwable) {
            Log.e(TAG, "load outbox failed", t)
            return Result.retry()
        }

        for (job in items) {
            try {
                when (job.op) {
                    "create" -> handleCreate(job)
                    "update" -> handleUpdate(job)
                    "delete" -> handleDelete(job)
                    else -> {
                        Log.w(TAG, "unknown op=${job.op} -> drop")
                        outboxDao.delete(job.id)
                    }
                }
            } catch (e: IOException) {
                // قطع اینترنت/تایم‌اوت → بعداً دوباره تلاش کن
                Log.w(TAG, "network IO error, will retry later", e)
                return Result.retry()
            } catch (e: ConnectionShutdownException) {
                Log.w(TAG, "connection shutdown, retry", e)
                return Result.retry()
            } catch (e: HttpException) {
                // خطای سمت سرور؛ برای بعضی کُدها پاک می‌کنیم تا کیو گیر نکند
                Log.e(TAG, "http error for job=${job.id} op=${job.op} code=${e.code()}", e)
                if (job.op == "delete" || e.code() == 404) {
                    // اگر سرور می‌گه نیست، ما هم رکورد outbox رو پاک می‌کنیم
                    outboxDao.delete(job.id)
                    continue
                }
                // برای بقیه موارد بهتره retry کنه
                return Result.retry()
            } catch (t: Throwable) {
                Log.e(TAG, "job failed fatally -> will retry later", t)
                return Result.retry()
            }
        }

        Log.d(TAG, "doWork() finished")
        return Result.success()
    }

    private suspend fun handleCreate(job: OutboxEntity) {
        val tempId = job.noteId
        val body: NoteCreateRequest =
            gson.fromJson(job.payloadJson ?: "{}", NoteCreateRequest::class.java)

        Log.d(TAG, "handleCreate tempId=$tempId title='${body.title}'")

        val res = api.create(body)
        if (!res.isSuccessful) {
            val msg = res.errorBody()?.string().orEmpty()
            Log.w(TAG, "create failed: $msg")
            throw HttpException(res)
        }
        val created: NoteDto = res.body() ?: run {
            Log.e(TAG, "create success but body==null")
            throw IllegalStateException("Empty body")
        }

        // اگر tempId داشتیم، رکورد موقتی را پاک کن
        if (tempId < 0) {
            runCatching { noteDao.deleteById(tempId) }
            // اگر Outbox دیگری به tempId اشاره می‌کند، به id واقعی نگاشت کن (اگر این متد را داری)
            runCatching { outboxDao.replaceNoteId(tempId, created.id) }
        }

        // نسخه‌ی سروری را کش کن
        noteDao.upsert(created.toEntity())

        // آیتم outbox مربوط به این کار را حذف کن
        outboxDao.delete(job.id)

        Log.d(TAG, "create synced → newId=${created.id}")
    }

    private suspend fun handleUpdate(job: OutboxEntity) {
        val id = job.noteId
        val body: NoteUpdateRequest =
            gson.fromJson(job.payloadJson ?: "{}", NoteUpdateRequest::class.java)

        Log.d(TAG, "handleUpdate id=$id")

        val res = api.update(id, body)
        if (!res.isSuccessful) {
            val msg = res.errorBody()?.string().orEmpty()
            Log.w(TAG, "update failed: $msg")
            throw HttpException(res)
        }

        // اگر سرور بدنهٔ به‌روز را برگرداند، همان را کش کن؛ وگرنه کاری لازم نیست
        res.body()?.let { updated ->
            noteDao.upsert(updated.toEntity())
        }

        // آیتم outbox را حذف کن
        outboxDao.delete(job.id)
        Log.d(TAG, "update synced id=$id")
    }

    private suspend fun handleDelete(job: OutboxEntity) {
        val id = job.noteId
        Log.d(TAG, "handleDelete id=$id")

        val res = api.delete(id)
        if (!res.isSuccessful && res.code() != 404) {
            val msg = res.errorBody()?.string().orEmpty()
            Log.w(TAG, "delete failed: $msg")
            throw HttpException(res)
        }
        // چه 204 چه 404 → آیتم outbox حذف می‌شود
        outboxDao.delete(job.id)
        Log.d(TAG, "delete synced id=$id")
    }
}
