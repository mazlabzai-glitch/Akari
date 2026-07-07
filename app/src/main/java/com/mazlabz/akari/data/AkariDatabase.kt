package com.mazlabz.akari.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Entry::class], version = 1, exportSchema = false)
abstract class AkariDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile private var instance: AkariDatabase? = null

        fun get(context: Context): AkariDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AkariDatabase::class.java,
                    "akari.db"
                ).build().also { instance = it }
            }
    }
}
