package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.SessionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionLogDao {
    @Query("SELECT * FROM session_logs ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionLog)

    @Query("DELETE FROM session_logs")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM session_logs WHERE id = :id")
    suspend fun deleteSessionById(id: Int)
}
