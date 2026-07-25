package com.debtfreein.app.data.ai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.debtfreein.app.ui.MainActivity
import com.debtfreein.app.data.network.UpstoxExecutionService
import com.debtfreein.app.data.logging.FileLogger
import kotlinx.coroutines.*

class TradingBotService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var wakeLock: PowerManager.WakeLock? = null
    private var mainLoopJob: Job? = null

    private companion object {
        const val CHANNEL_ID = "TradingBotServiceChannel"
        const val NOTIFICATION_ID = 8888
        const val TAG = "TradingBotService"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DebtFreeIn::TradingBotWakeLock").apply {
            acquire()
        }
        
        Log.i(TAG, "TradingBotService created, WakeLock acquired.")
        FileLogger.log("TRADING_BOT_SERVICE", "TradingBotService created, WakeLock acquired.", applicationContext)
        startLogsAlertListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification("Initializing...")
        startForeground(NOTIFICATION_ID, notification)

        startMarketScannerLoop()

        return START_STICKY
    }

    private var eodReviewRunToday = false

    private fun runEndOfDayReviewTask() {
        serviceScope.launch {
            try {
                val budget = com.debtfreein.app.data.security.SecureStorageManager.getMonthlyBudget(applicationContext)
                val spend = ApiBudgetManager.getMonthlySpend(applicationContext)
                val remainingBudget = (budget - spend).coerceAtLeast(0.0)

                val balance = UpstoxExecutionService.virtualLedger.value.balance
                val recentLogs = UpstoxExecutionService.logs.value.takeLast(10).joinToString("; ")
                val ledgerState = "Ledger Balance: Rs.$balance. Recent Actions: $recentLogs"

                Log.i(TAG, "Running End-of-Day Review at 4:00 PM...")
                val reviewJsonStr = GeminiBrainService.runEndOfDayReview(
                    applicationContext,
                    ledgerState,
                    remainingBudget
                )
                
                UpstoxExecutionService.addLog("CIO EOD Review: $reviewJsonStr")
                eodReviewRunToday = true
            } catch (e: Exception) {
                Log.e(TAG, "Failed running EOD review", e)
            }
        }
    }

    private fun startMarketScannerLoop() {
        mainLoopJob?.cancel()
        mainLoopJob = serviceScope.launch {
            while (isActive) {
                val state = MarketClock.getCurrentState()
                updateNotificationState(state)

                // EOD review check at 4:00 PM IST (16:00)
                val nowKolkata = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Kolkata"))
                val hour = nowKolkata.hour
                val minute = nowKolkata.minute
                
                if (hour == 16 && minute in 0..15) {
                    if (!eodReviewRunToday) {
                        runEndOfDayReviewTask()
                    }
                } else {
                    eodReviewRunToday = false // Reset for next day
                }

                when (state) {
                    MarketState.PRE_MARKET -> {
                        Log.i(TAG, "Pre-market context gathering state (9:00 AM - 9:15 AM)")
                        FileLogger.log("TRADING_BOT_SERVICE", "MarketState: PRE_MARKET (9:00 AM - 9:15 AM)", applicationContext)
                    }
                    MarketState.ACTIVE_TRADING -> {
                        Log.i(TAG, "Active trading state. Scanning setups...")
                        FileLogger.log("TRADING_BOT_SERVICE", "MarketState: ACTIVE_TRADING. Executing market setup scan cycle...", applicationContext)
                        try {
                            if (ApiBudgetManager.isTradingAllowed(applicationContext)) {
                                AutonomousPaperTrader.executeLogicCycle(applicationContext)
                                EngineHeartbeatManager.writeHeartbeat(applicationContext)
                                writeTelemetryToFirestore(AutonomousPaperTrader.currentTelemetry)
                            }
                        } catch (e: HardStopException) {
                            Log.e(TAG, "API Monthly budget hard stop exceeded. Suspending scanning.", e)
                            FileLogger.log("TRADING_BOT_SERVICE", "API Hard Stop Exceeded: ${e.message}", applicationContext)
                            pushAlertToFirestore("Budget Alert", "Hard Stop: API monthly spend limit exceeded. ${e.message}")
                            val suspendedTelemetry = com.debtfreein.app.data.model.TelemetryMetrics(
                                botStatus = "Suspended",
                                lastDecisionReason = "Engine Suspended: API Monthly budget hard stop exceeded.",
                                activeTripwires = "ATR (14): Disabled due to budget limit",
                                liveSessionPnL = com.debtfreein.app.data.network.CapitalAllocator.currentNetPnL.value
                            )
                            writeTelemetryToFirestore(suspendedTelemetry)
                            stopMarketScannerLoop()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error running market scanner", e)
                            FileLogger.log("TRADING_BOT_SERVICE", "Error running market scanner: ${e.message}", applicationContext)
                        }
                    }
                    MarketState.MARKET_CLOSED -> {
                        Log.i(TAG, "Market Closed state. API scanning coroutines paused.")
                        FileLogger.log("TRADING_BOT_SERVICE", "MarketState: MARKET_CLOSED. Scanning paused.", applicationContext)
                        EngineHeartbeatManager.writeHeartbeat(applicationContext)
                        val closedTelemetry = com.debtfreein.app.data.model.TelemetryMetrics(
                            botStatus = "Suspended",
                            lastDecisionReason = "Market Closed. API scanning coroutines paused until market opens.",
                            activeTripwires = "ATR (14): Inactive",
                            liveSessionPnL = com.debtfreein.app.data.network.CapitalAllocator.currentNetPnL.value
                        )
                        writeTelemetryToFirestore(closedTelemetry)
                    }
                }
                
                // Retrieve dynamic scanner pacing delay
                val dynamicDelay = try {
                    AutonomousPaperTrader.getDynamicDelayMs(applicationContext)
                } catch (e: Exception) {
                    180 * 1000L // 3 minutes fallback
                }
                delay(dynamicDelay)
            }
        }
    }

    private fun stopMarketScannerLoop() {
        mainLoopJob?.cancel()
        mainLoopJob = null
    }

    private fun updateNotificationState(state: MarketState) {
        val text = when (state) {
            MarketState.PRE_MARKET -> "Gathering pre-market context..."
            MarketState.ACTIVE_TRADING -> "Scanning Market..."
            MarketState.MARKET_CLOSED -> "Sleeping: Market Closed"
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun buildNotification(contentText: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, flags
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Autonomous Trading Bot")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Trading Bot Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        stopMarketScannerLoop()
        serviceJob.cancel()
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        Log.i(TAG, "TradingBotService destroyed, WakeLock released.")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startLogsAlertListener() {
        var lastLogSize = com.debtfreein.app.data.network.UpstoxExecutionService.logs.value.size
        serviceScope.launch {
            com.debtfreein.app.data.network.UpstoxExecutionService.logs.collect { logs ->
                if (logs.size > lastLogSize) {
                    for (i in lastLogSize until logs.size) {
                        val rawLog = logs[i]
                        val message = if (rawLog.startsWith("[") && rawLog.contains("] ")) {
                            rawLog.substringAfter("] ")
                        } else {
                            rawLog
                        }
                        
                        if (message.contains("Executing", ignoreCase = true) || 
                            message.contains("APPROVED", ignoreCase = true) || 
                            message.contains("Trade executed", ignoreCase = true) || 
                            message.contains("exited", ignoreCase = true) ||
                            message.contains("VETOED", ignoreCase = true)) {
                            pushAlertToFirestore("Trade Execution", message)
                        } else if (message.contains("HARD STOP", ignoreCase = true) || 
                                   message.contains("WARNING", ignoreCase = true) || 
                                   message.contains("budget", ignoreCase = true)) {
                            pushAlertToFirestore("Budget Alert", message)
                        }
                    }
                }
                lastLogSize = logs.size
            }
        }
    }

    private fun pushAlertToFirestore(title: String, body: String) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val alert = mapOf(
            "title" to title,
            "body" to body,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "readStatus" to false
        )
        db.collection("users").document(uid).collection("notifications")
            .add(alert)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully wrote alert to Firestore: $title")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to write alert to Firestore", e)
            }
    }

    private fun writeTelemetryToFirestore(metrics: com.debtfreein.app.data.model.TelemetryMetrics) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val telemetryData = hashMapOf(
            "botStatus" to metrics.botStatus,
            "lastDecisionReason" to metrics.lastDecisionReason,
            "activeTripwires" to metrics.activeTripwires,
            "liveSessionPnL" to metrics.liveSessionPnL,
            "timestamp" to metrics.timestamp,
            "lastHeartbeatTimestamp" to com.google.firebase.Timestamp.now()
        )
        db.collection("users").document(uid).collection("config").document("bot_telemetry")
            .set(telemetryData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Successfully updated bot telemetry and heartbeat timestamp in Firestore: ${metrics.botStatus}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed writing bot telemetry to Firestore", e)
            }
    }
}
