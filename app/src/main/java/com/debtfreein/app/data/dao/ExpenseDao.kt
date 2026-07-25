package com.debtfreein.app.data.dao

import androidx.room.*
import com.debtfreein.app.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpensesFlow(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    suspend fun getAllExpenses(): List<Expense>

    @Query("SELECT * FROM expenses WHERE cardId = :cardId ORDER BY timestamp DESC")
    fun getExpensesByCardFlow(cardId: Long): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE amount = :amount AND abs(timestamp - :timestamp) <= 900000 ORDER BY abs(timestamp - :timestamp) ASC")
    suspend fun findMatchingExpenses(amount: Double, timestamp: Long): List<Expense>
}
