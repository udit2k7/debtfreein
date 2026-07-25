package com.debtfreein.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.debtfreein.app.data.model.TokenSpend

@Dao
interface TokenSpendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokenSpend(spend: TokenSpend)

    @Query("SELECT * FROM token_spends ORDER BY timestamp DESC")
    suspend fun getAllTokenSpends(): List<TokenSpend>

    @Query("SELECT * FROM token_spends WHERE timestamp >= :sinceTimestamp")
    suspend fun getTokenSpendsSince(sinceTimestamp: Long): List<TokenSpend>

    @Query("SELECT SUM(costInr) FROM token_spends WHERE timestamp >= :sinceTimestamp")
    suspend fun getTotalCostInrSince(sinceTimestamp: Long): Double?
}
