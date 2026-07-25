package com.debtfreein.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "token_spends")
data class TokenSpend(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val model: String,
    val inputTokens: Long,
    val outputTokens: Long,
    val costInr: Double
)
