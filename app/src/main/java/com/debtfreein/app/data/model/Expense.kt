package com.debtfreein.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CreditCard::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["cardId"])]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val merchant: String,
    val timestamp: Long,
    val category: String,
    val cardId: Long?, // Nullable if cash or bank account transfer
    val rawSmsText: String?, // Nullable if added manually
    val isReimbursableClaim: Boolean = false,
    val expenseCategory: String = "Other", // Fixed costs like 'School Fees', 'Office Expense'
    val status: String? = null,
    val notes: String? = null
)
