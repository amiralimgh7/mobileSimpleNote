package com.example.simplenote.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val color: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    // فلگ‌های ساده برای آفلاین (اگر نخواستی استفاده کنی، ایرادی ندارد)
    val pendingCreate: Boolean = false,
    val pendingUpdate: Boolean = false
)
