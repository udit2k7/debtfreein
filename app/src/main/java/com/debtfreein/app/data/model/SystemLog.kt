package com.debtfreein.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_logs")
data class SystemLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val message: String,
    val timestamp: Long,
    val level: String // "INFO", "WARN", "ERROR"
)
