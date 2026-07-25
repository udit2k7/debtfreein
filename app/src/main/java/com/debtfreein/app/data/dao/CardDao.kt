package com.debtfreein.app.data.dao

import androidx.room.*
import com.debtfreein.app.data.model.CreditCard
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM credit_cards ORDER BY apr DESC")
    fun getAllCardsFlow(): Flow<List<CreditCard>>

    @Query("SELECT * FROM credit_cards ORDER BY apr DESC")
    suspend fun getAllCards(): List<CreditCard>

    @Query("SELECT * FROM credit_cards WHERE cardLastFour = :lastFour LIMIT 1")
    suspend fun getCardByLastFour(lastFour: String): CreditCard?

    @Query("SELECT * FROM credit_cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: Long): CreditCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CreditCard): Long

    @Update
    suspend fun updateCard(card: CreditCard)

    @Delete
    suspend fun deleteCard(card: CreditCard)
}
