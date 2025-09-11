package com.example.simplenote.data.notes

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.simplenote.data.local.NoteDao
import com.example.simplenote.data.local.NoteEntity
import com.example.simplenote.data.local.OutboxDao
import com.example.simplenote.data.local.PageStats
import com.example.simplenote.data.local.PageStatsStore
import com.example.simplenote.data.mappers.toDto
import com.example.simplenote.data.mappers.toEntity
import com.example.simplenote.data.remote.NoteCreateRequest
import com.example.simplenote.data.remote.NoteDto
import com.example.simplenote.data.remote.NoteUpdateRequest
import com.example.simplenote.data.remote.NotesApi
import com.example.simplenote.data.sync.OutboxSyncWorker
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.ceil
import kotlin.math.max

private const val TAG = "NotesRepository"
private const val UNIQUE_OUTBOX_WORK = "outbox-sync"

data class NotesPage(
    val items: List<NoteDto>,
    val page: Int,
    val pageSize: Int,
    val count: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean
)

class NotesRepository(
    private val appContext: Context,
    private val api: NotesApi,
    private val noteDao: NoteDao,
    private val outboxDao: OutboxDao
) {
    private val statsStore = PageStatsStore(appContext)
    private val gson = Gson()

    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 64)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    private fun isOnline(): Boolean {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun load(page: Int, pageSize: Int, query: String?): Result<NotesPage> {
        val p = max(1, page)
        val size = max(1, pageSize)
        val offset = (p - 1) * size

        if (isOnline()) {
            try {
                val r = if (query.isNullOrBlank())
                    api.listNotes(page = p, pageSize = size)
                else
                    api.filterNotes(title = query, page = p, pageSize = size)

                if (r.isSuccessful) {
                    val body = r.body()
                    val remoteList = body?.results ?: emptyList()
                    val remoteCount = body?.count ?: 0

                    // کش را به‌روز کن
                    noteDao.upsertAll(remoteList.map { it.toEntity() })

                    // آمار را ذخیره کن
                    runCatching { statsStore.save(query, size, remoteCount) }

                    val totalPages = if (size <= 0) 1 else max(1, ceil(remoteCount / size.toDouble()).toInt())
                    return Result.success(
                        NotesPage(
                            items = remoteList,
                            page = p,
                            pageSize = size,
                            count = remoteCount,
                            totalPages = totalPages,
                            hasNext = p < totalPages,
                            hasPrev = p > 1
                        )
                    )
                }
            } catch (_: Throwable) {
                // drop to offline
            }
        }

        // آفلاین/خطا: از Room + آمار ذخیره‌شده
        val localPage = noteDao.listPage(query, size, offset)
        val stats: PageStats? = runCatching { statsStore.get(query, size) }.getOrNull()
        val count = stats?.count ?: runCatching { noteDao.count(query) }.getOrDefault(0)
        val totalPages = if (size <= 0) 1 else max(1, ceil(count / size.toDouble()).toInt())

        return Result.success(
            NotesPage(
                items = localPage.map { it.toDto() },
                page = p,
                pageSize = size,
                count = count,
                totalPages = totalPages,
                hasNext = p < totalPages,
                hasPrev = p > 1
            )
        )
    }

    suspend fun getLocalById(id: Int): NoteDto? =
        noteDao.getById(id)?.toDto()

    suspend fun fetchAndCacheById(id: Int): Result<NoteDto> = try {
        val r = api.getNote(id)
        if (r.isSuccessful) {
            val dto = r.body()!!
            noteDao.upsert(dto.toEntity())
            Result.success(dto)
        } else {
            Result.failure(IllegalStateException(r.errorBody()?.string().orEmpty().ifBlank { "Request failed" }))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

    /** ساخت نوت: Optimistic + تلاش فوری شبکه؛ اگر نشد Outbox */
    suspend fun create(title: String, description: String): Result<Int> {
        val tempId = -((System.currentTimeMillis() % Int.MAX_VALUE).toInt())
        val entity = NoteEntity(
            id = tempId,
            title = title,
            description = description
        )
        // 1) فوری در Room
        noteDao.upsert(entity)
        // آمار همهٔ سایزهای رایج را برای query خالی +1 کن
        runCatching { statsStore.bumpAllPageSizes(query = null, delta = +1) }
        _events.tryEmit(Unit)

        // 2) تلاش فوری شبکه (اگر آنلاین)
        if (isOnline()) {
            try {
                val res = api.create(NoteCreateRequest(title = title, description = description))
                if (res.isSuccessful) {
                    val dto = res.body()!!
                    // temp را پاک و نسخهٔ سروری را ذخیره کن
                    noteDao.deleteById(tempId)
                    noteDao.upsert(dto.toEntity())
                    runCatching { outboxDao.deleteByNoteId(tempId) }
                    _events.tryEmit(Unit)
                    return Result.success(dto.id)
                }
            } catch (t: Throwable) {
                Log.w(TAG, "create immediate failed (will outbox): ${t.message}")
            }
        }

        // 3) اگر نشد، Outbox
        val payload = gson.toJson(NoteCreateRequest(title = title, description = description))
        outboxDao.insert(com.example.simplenote.data.local.OutboxEntity(noteId = tempId, op = "create", payloadJson = payload))
        triggerOutboxSync()
        return Result.success(tempId)
    }

    /** ویرایش: Optimistic (لوکال) + Outbox */
    suspend fun update(id: Int, title: String?, description: String?): Result<Unit> {
        val t = title ?: ""
        val d = description ?: ""
        noteDao.updateContent(id, t, d)

        val payload = gson.toJson(NoteUpdateRequest(title = t, description = d))
        outboxDao.insert(com.example.simplenote.data.local.OutboxEntity(noteId = id, op = "update", payloadJson = payload))

        triggerOutboxSync()
        _events.tryEmit(Unit)
        return Result.success(Unit)
    }

    /** حذف: فوری لوکال + آپدیت شمارنده؛ اگر آنلاین شدیم همان لحظه سرور، وگرنه Outbox */
    suspend fun delete(id: Int): Result<Unit> {
        // 1) فوری از Room پاک کن
        noteDao.deleteById(id)
        // 2) آمار را -1 کن
        runCatching { statsStore.bumpAllPageSizes(query = null, delta = -1) }
        _events.tryEmit(Unit)

        // 3) اگر id منفی (موقت) بود، فقط Outbox همان نوت را پاک کن
        if (id < 0) {
            outboxDao.deleteByNoteId(id)
            return Result.success(Unit)
        }

        // 4) تلاش فوری شبکه
        if (isOnline()) {
            try {
                val res = api.delete(id)
                if (res.isSuccessful) {
                    runCatching { outboxDao.deleteByNoteId(id) }
                    return Result.success(Unit)
                }
            } catch (t: Throwable) {
                Log.w(TAG, "delete immediate failed (will outbox): ${t.message}")
            }
        }

        // 5) اگر نشد، Outbox
        outboxDao.insert(com.example.simplenote.data.local.OutboxEntity(noteId = id, op = "delete", payloadJson = null))
        triggerOutboxSync()
        return Result.success(Unit)
    }

    private fun triggerOutboxSync() {
        val wm = WorkManager.getInstance(appContext)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val req = OneTimeWorkRequestBuilder<OutboxSyncWorker>()
            .setConstraints(constraints)
            .build()
        wm.enqueueUniqueWork(UNIQUE_OUTBOX_WORK, ExistingWorkPolicy.KEEP, req)
    }
}
