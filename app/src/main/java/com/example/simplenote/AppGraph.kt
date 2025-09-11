package com.example.simplenote

import android.content.Context
import androidx.work.WorkManager
import com.example.simplenote.data.auth.AuthRepository
import com.example.simplenote.data.auth.TokenStore
import com.example.simplenote.data.local.AppDatabase
import com.example.simplenote.data.network.RetrofitProvider
import com.example.simplenote.data.notes.NotesRepository
import com.example.simplenote.data.remote.AuthApi
import com.example.simplenote.data.remote.NotesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppGraph {

    lateinit var appContext: Context
        private set

    lateinit var tokenStore: TokenStore
        private set

    lateinit var db: AppDatabase
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var notesRepository: NotesRepository
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
        tokenStore = TokenStore(appContext)
        db = AppDatabase.getInstance(appContext)

        val retrofit = RetrofitProvider.get(appContext)
        val authApi = retrofit.create(AuthApi::class.java)
        val notesApi = retrofit.create(NotesApi::class.java)

        authRepository = AuthRepository(authApi, tokenStore)
        notesRepository = NotesRepository(
            appContext = appContext,
            api = notesApi,
            noteDao = db.noteDao(),
            outboxDao = db.outboxDao()
        )
    }

    /** اگر توکن داشتیم یا دیتای لوکال داشتیم => کاربر قبلاً لاگین بوده (حتی آفلاین) */
    suspend fun isLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        val hasToken = try { !tokenStore.getAccess().isNullOrBlank() } catch (_: Throwable) { false }
        if (hasToken) return@withContext true
        val localCount = try { db.noteDao().count(null) } catch (_: Throwable) { 0 }
        localCount > 0
    }

    /** برای Logout: توقف سینک، پاک کردن توکن‌ها، پاک کردن کل دیتابیس لوکال */
    suspend fun logoutAndWipe() = withContext(Dispatchers.IO) {
        runCatching { WorkManager.getInstance(appContext).cancelUniqueWork("outbox-sync") }
        runCatching { tokenStore.clear() }
        runCatching { db.clearAllTables() }
    }
}
