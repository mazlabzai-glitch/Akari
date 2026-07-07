package com.mazlabz.akari.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert
    suspend fun insert(entry: Entry): Long

    @Update
    suspend fun update(entry: Entry)

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM entries ORDER BY ts ASC")
    fun all(): Flow<List<Entry>>

    @Query("SELECT * FROM entries ORDER BY ts ASC")
    suspend fun snapshot(): List<Entry>

    @Query("DELETE FROM entries")
    suspend fun clear()
}
