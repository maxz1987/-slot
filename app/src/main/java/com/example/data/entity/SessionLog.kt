package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_logs")
data class SessionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val startingBankroll: Double,
    val endingBankroll: Double,
    val totalSpins: Int,
    val maxSingleWin: Double,
    val strategyUsed: String,
    val netProfit: Double
)
