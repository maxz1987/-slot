package com.example.data.repository

import com.example.data.dao.SessionLogDao
import com.example.data.entity.SessionLog
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionLogDao: SessionLogDao) {
    val allSessions: Flow<List<SessionLog>> = sessionLogDao.getAllSessions()

    suspend fun insertSession(session: SessionLog) {
        sessionLogDao.insertSession(session)
    }

    suspend fun deleteAllSessions() {
        sessionLogDao.deleteAllSessions()
    }

    suspend fun deleteSessionById(id: Int) {
        sessionLogDao.deleteSessionById(id)
    }
}
