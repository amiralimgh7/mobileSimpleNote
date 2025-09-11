package com.example.simplenote.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Int,
    /** "create" | "update" | "delete" */
    val op: String,
    /** JSON payload برای create/update؛ برای delete می‌تونه null باشه */
    val payloadJson: String? = null
)
