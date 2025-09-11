package com.example.simplenote.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<NoteEntity>)

    @Query("""
        SELECT * FROM notes
        WHERE (:q IS NULL OR :q = '' OR title LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%')
        ORDER BY id DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun listPage(q: String?, limit: Int, offset: Int): List<NoteEntity>

    @Query("""
        SELECT COUNT(*) FROM notes
        WHERE (:q IS NULL OR :q = '' OR title LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%')
    """)
    suspend fun count(q: String?): Int

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): NoteEntity?

    @Query("UPDATE notes SET title = :title, description = :description WHERE id = :id")
    suspend fun updateContent(id: Int, title: String, description: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Int)
}
