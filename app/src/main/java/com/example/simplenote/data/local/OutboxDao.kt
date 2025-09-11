package com.example.simplenote.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OutboxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OutboxEntity)

    @Query("SELECT * FROM outbox ORDER BY id ASC")
    suspend fun allAsc(): List<OutboxEntity>

    @Query("DELETE FROM outbox WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM outbox WHERE noteId = :noteId")
    suspend fun deleteByNoteId(noteId: Int)

    @Query("UPDATE outbox SET noteId = :newId WHERE noteId = :oldId")
    suspend fun replaceNoteId(oldId: Int, newId: Int)

    @Query("DELETE FROM outbox")
    suspend fun clearAll()
}
