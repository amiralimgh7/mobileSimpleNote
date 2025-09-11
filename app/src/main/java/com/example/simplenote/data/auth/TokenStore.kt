package com.example.simplenote.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "tokens")

class TokenStore(private val context: Context) {

    private object Keys {
        val ACCESS  = stringPreferencesKey("access")
        val REFRESH = stringPreferencesKey("refresh")
    }

    suspend fun saveTokens(access: String?, refresh: String?) {
        context.dataStore.edit { prefs ->
            access?.let { prefs[Keys.ACCESS] = it }
            refresh?.let { prefs[Keys.REFRESH] = it }
        }
    }

    suspend fun getAccess(): String? =
        context.dataStore.data.map { it[Keys.ACCESS] }.first()

    suspend fun getRefresh(): String? =
        context.dataStore.data.map { it[Keys.REFRESH] }.first()

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    /** بلاکینگ برای استفاده در Interceptor (چون suspend نمی‌پذیرد) */
    fun getAccessBlocking(): String? = runBlocking { getAccess() }
    fun getRefreshBlocking(): String? = runBlocking { getRefresh() }
}
