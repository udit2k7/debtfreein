package com.debtfreein.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_cards")
data class CreditCard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val issuer: String,
    val currentBalance: Double,
    val creditLimit: Double,
    val apr: Double, // Annual Percentage Rate, e.g. 24.99
    val dueDay: Int, // Day of month card due date falls on, e.g. 25
    val nextDueDate: String?, // Format "yyyy-MM-dd"
    val minimumPayment: Double,
    val cardLastFour: String // Last 4 digits of card, e.g. "1234"
)
