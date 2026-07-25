package com.debtfreein.app.data.dao

import androidx.room.*
import com.debtfreein.app.data.model.Investment
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments ORDER BY symbol ASC")
    fun getAllInvestmentsFlow(): Flow<List<Investment>>

    @Query("SELECT * FROM investments ORDER BY symbol ASC")
    suspend fun getAllInvestments(): List<Investment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(investment: Investment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllInvestments(investments: List<Investment>)

    @Update
    suspend fun updateInvestment(investment: Investment)

    @Query("DELETE FROM investments WHERE id = :id")
    suspend fun deleteInvestmentById(id: Long)
}
