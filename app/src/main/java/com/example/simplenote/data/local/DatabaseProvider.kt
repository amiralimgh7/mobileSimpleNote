package com.example.simplenote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [NoteEntity::class, OutboxEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDb : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun outboxDao(): OutboxDao
}

object DatabaseProvider {
    @Volatile private var db: AppDb? = null

    fun get(context: Context): AppDb {
        return db ?: synchronized(this) {
            db ?: Room.databaseBuilder(
                context.applicationContext,
                AppDb::class.java,
                "simplenote.db"
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { db = it }
        }
    }
}
