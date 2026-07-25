package com.debtfreein.app.data.dao

import androidx.room.*
import com.debtfreein.app.data.model.SystemLog
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemLogDao {
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogsFlow(): Flow<List<SystemLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SystemLog)

    @Query("DELETE FROM system_logs")
    suspend fun clearLogs()
}
