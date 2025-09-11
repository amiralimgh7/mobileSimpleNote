package com.example.simplenote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [NoteEntity::class, OutboxEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun outboxDao(): OutboxDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "simplenote.db"
                )
                    // در طول توسعه: اگر اسکیمای قبلی ناسازگار بود، دیتابیس پاک شود
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
