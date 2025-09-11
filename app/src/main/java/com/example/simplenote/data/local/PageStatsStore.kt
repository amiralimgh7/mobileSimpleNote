package com.example.simplenote.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Locale
import kotlin.math.max

private val Context.pageStatsDS by preferencesDataStore(name = "page_stats_ds")

data class PageStats(
    val count: Int,
    val totalPages: Int,
    val pageSize: Int
)

class PageStatsStore(private val context: Context) {
    private val gson = Gson()

    private fun keyFor(query: String?, pageSize: Int): String {
        val q = query?.trim()?.lowercase(Locale.US).orEmpty()
        return "$q#$pageSize"
    }

    suspend fun save(query: String?, pageSize: Int, count: Int) {
        val key = stringPreferencesKey(keyFor(query, pageSize))
        val total = if (pageSize <= 0) 1 else ((count + pageSize - 1) / pageSize)
        val stats = PageStats(count = max(0, count), totalPages = max(1, total), pageSize = pageSize)
        context.pageStatsDS.edit { prefs ->
            prefs[key] = gson.toJson(stats)
        }
    }

    suspend fun get(query: String?, pageSize: Int): PageStats? {
        val key = stringPreferencesKey(keyFor(query, pageSize))
        val raw = context.pageStatsDS.data.map { it[key] }.first()
        return raw?.let { runCatching { gson.fromJson(it, PageStats::class.java) }.getOrNull() }
    }

    /** 🔼/🔽 افزایش/کاهش خوش‌رفتار شمارنده‌ها بعد از create/delete  */
    suspend fun bump(query: String?, pageSize: Int, delta: Int) {
        val key = stringPreferencesKey(keyFor(query, pageSize))
        context.pageStatsDS.edit { prefs ->
            val cur = prefs[key]?.let { runCatching { gson.fromJson(it, PageStats::class.java) }.getOrNull() }
            val newCount = max(0, (cur?.count ?: 0) + delta)
            val total = if (pageSize <= 0) 1 else ((newCount + pageSize - 1) / pageSize)
            val stats = PageStats(count = newCount, totalPages = max(1, total), pageSize = pageSize)
            prefs[key] = gson.toJson(stats)
        }
    }

    /** برای چند سایز رایج صفحه (۶/۱۰/۱۲/۲۰/۲۴/۳۰) شمارنده را همزمان به‌روز کن */
    suspend fun bumpAllPageSizes(query: String?, delta: Int, pageSizes: IntArray = intArrayOf(6, 10, 12, 20, 24, 30)) {
        for (ps in pageSizes) bump(query, ps, delta)
    }

    suspend fun clearAll() {
        context.pageStatsDS.edit { it.clear() }
    }
}
