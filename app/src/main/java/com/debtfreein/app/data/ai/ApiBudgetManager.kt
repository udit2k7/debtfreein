package com.debtfreein.app.data.ai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.debtfreein.app.data.db.AppDatabase
import com.debtfreein.app.data.model.TokenSpend
import com.debtfreein.app.data.network.UpstoxExecutionService
import com.debtfreein.app.data.security.SecureStorageManager
import java.util.Calendar

class HardStopException(message: String) : Exception(message)

object ApiBudgetManager {
    private const val TAG = "ApiBudgetManager"
    private const val USD_TO_INR = 83.5
    private const val WARNING_CHANNEL_ID = "ApiBudgetWarningChannel"
    private const val WARNING_NOTIFICATION_ID = 9999

    private fun getStartOfMonthTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    suspend fun getMonthlySpend(context: Context): Double {
        val db = AppDatabase.getInstance(context)
        val startTimestamp = getStartOfMonthTimestamp()
        return db.tokenSpendDao().getTotalCostInrSince(startTimestamp) ?: 0.0
    }

    suspend fun logSpend(context: Context, model: String, inputTokens: Long, outputTokens: Long) {
        val inputCostUsd = (inputTokens.toDouble() * 0.435) / 1_000_000.0
        val outputCostUsd = (outputTokens.toDouble() * 0.87) / 1_000_000.0
        val totalCostUsd = inputCostUsd + outputCostUsd
        val costInr = totalCostUsd * USD_TO_INR

        val db = AppDatabase.getInstance(context)
        val spend = TokenSpend(
            timestamp = System.currentTimeMillis(),
            model = model,
            inputTokens = inputTokens,
            outputTokens = outputTokens,
            costInr = costInr
        )
        db.tokenSpendDao().insertTokenSpend(spend)
        Log.i(TAG, "Logged spend: $model, input: $inputTokens, output: $outputTokens, cost: ₹${String.format("%.4f", costInr)}")
        com.debtfreein.app.data.logging.FileLogger.log("AI_TOKEN_CALCULATION", "Model: $model | InputTokens: $inputTokens | OutputTokens: $outputTokens | Calculated Cost: ₹${String.format("%.4f", costInr)}", context)

        // Trigger safety check
        try {
            isTradingAllowed(context)
        } catch (e: HardStopException) {
            Log.e(TAG, "Hard stop triggered after logging spend", e)
        }
    }

    suspend fun isTradingAllowed(context: Context): Boolean {
        val totalSpend = getMonthlySpend(context)
        val budget = SecureStorageManager.getMonthlyBudget(context)

        // Hard Stop: Reaches Budget + 5%
        if (totalSpend >= budget * 1.05) {
            UpstoxExecutionService.setPaperTradingActive(false)
            UpstoxExecutionService.addLog("HARD STOP: API spend ₹${String.format("%.2f", totalSpend)} exceeded budget ₹${String.format("%.2f", budget)} + 5%. Trading suspended.")
            throw HardStopException("Hard Stop: API monthly spend is ₹${String.format("%.2f", totalSpend)}, budget limit of ₹${String.format("%.2f", budget)} (exceeded by >5%) reached!")
        }

        // Soft Warning: Reaches Budget - 5%
        if (totalSpend >= budget * 0.95) {
            pushWarningNotification(context, totalSpend, budget)
            UpstoxExecutionService.addLog("WARNING: API monthly spend is close to budget limit (₹${String.format("%.2f", totalSpend)} of ₹${String.format("%.2f", budget)} spent).")
        }

        return true
    }

    private fun pushWarningNotification(context: Context, totalSpend: Double, budget: Double) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                WARNING_CHANNEL_ID,
                "API Budget Warning Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, WARNING_CHANNEL_ID)
            .setContentTitle("API Budget Limit Warning")
            .setContentText("Spent ₹${String.format("%.2f", totalSpend)} of ₹${String.format("%.2f", budget)} budget.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(WARNING_NOTIFICATION_ID, notification)
    }
}
