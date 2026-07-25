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
import android.util.Log
import androidx.core.app.NotificationCompat
import com.debtfreein.app.ui.MainActivity
import com.debtfreein.app.data.network.UpstoxExecutionService
import com.debtfreein.app.data.security.SecureStorageManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*

class FailoverObserverService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var heartbeatListener: ListenerRegistration? = null
    private var observerMonitorJob: Job? = null

    @Volatile
    private var lastActiveTimestamp: Long = 0L

    @Volatile
    private var activeDeviceId: String = ""

    private var hasAlertedFailover = false

    companion object {
        const val TAG = "FailoverObserverService"
        const val ACTION_TAKEOVER = "com.debtfreein.app.ACTION_TAKEOVER"
        const val OBSERVER_CHANNEL_ID = "FailoverObserverChannel"
        const val CRITICAL_CHANNEL_ID = "CriticalFailoverAlertChannel"
        const val OBSERVER_NOTIFICATION_ID = 7777
        const val CRITICAL_NOTIFICATION_ID = 9991
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        Log.i(TAG, "FailoverObserverService created. Starting background observer...")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val foregroundNotification = buildObserverNotification("Monitoring active engine heartbeat...")
        startForeground(OBSERVER_NOTIFICATION_ID, foregroundNotification)

        if (intent?.action == ACTION_TAKEOVER) {
            Log.w(TAG, "ACTION_TAKEOVER triggered via notification tap! Executing local engine takeover...")
            executeEngineTakeover()
        } else {
            startHeartbeatObserver()
        }

        return START_STICKY
    }

    private var telemetryListener: ListenerRegistration? = null

    private fun startHeartbeatObserver() {
        if (heartbeatListener != null) return

        val db = FirebaseFirestore.getInstance()
        heartbeatListener = db.collection(EngineHeartbeatManager.COLLECTION_NAME)
            .document(EngineHeartbeatManager.DOCUMENT_ID)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Heartbeat snapshot listen failed", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val ts = snapshot.getLong("lastActiveTimestamp") ?: 0L
                    if (ts > 0L) lastActiveTimestamp = ts
                    activeDeviceId = snapshot.getString("activeDeviceId") ?: ""

                    val myDeviceId = EngineHeartbeatManager.getDeviceId(applicationContext)
                    if (activeDeviceId == myDeviceId) {
                        hasAlertedFailover = false
                    }
                    Log.d(TAG, "Received Heartbeat update: activeDevice=$activeDeviceId, timestamp=$lastActiveTimestamp")
                }
            }

        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            telemetryListener = db.collection("users").document(uid).collection("config").document("bot_telemetry")
                .addSnapshotListener { snapshot, e ->
                    if (snapshot != null && snapshot.exists()) {
                        val tsObj = snapshot.get("lastHeartbeatTimestamp")
                        val tsMs = when (tsObj) {
                            is com.google.firebase.Timestamp -> tsObj.toDate().time
                            is Long -> tsObj
                            else -> 0L
                        }
                        if (tsMs > 0L) {
                            lastActiveTimestamp = tsMs
                        }
                    }
                }
        }

        observerMonitorJob?.cancel()
        observerMonitorJob = serviceScope.launch {
            while (isActive) {
                delay(30 * 1000L) // Check every 30 seconds
                checkHeartbeatFailover()
            }
        }
    }

    private suspend fun checkHeartbeatFailover() {
        val myDeviceId = EngineHeartbeatManager.getDeviceId(applicationContext)

        // If this phone is currently the active device, no failover needed
        if (activeDeviceId == myDeviceId) {
            return
        }

        if (lastActiveTimestamp == 0L) {
            // Heartbeat not initialized yet
            return
        }

        val dynamicDelayMs = try {
            AutonomousPaperTrader.getDynamicDelayMs(applicationContext)
        } catch (e: Exception) {
            180 * 1000L
        }

        val failoverThresholdMs = 3 * dynamicDelayMs
        val elapsedMs = System.currentTimeMillis() - lastActiveTimestamp

        Log.d(TAG, "Failover check: elapsed=${elapsedMs / 1000}s, threshold=${failoverThresholdMs / 1000}s, activeDevice=$activeDeviceId, myDevice=$myDeviceId")

        if (elapsedMs > failoverThresholdMs && !hasAlertedFailover) {
            Log.e(TAG, "CRITICAL: Engine heartbeat timeout! Active engine $activeDeviceId unresponsive for ${elapsedMs / 1000}s.")
            hasAlertedFailover = true
            triggerCriticalHandoffNotification()
        }
    }

    private fun triggerCriticalHandoffNotification() {
        val mainActivityIntent = Intent(applicationContext, MainActivity::class.java).apply {
            action = ACTION_TAKEOVER
            putExtra("trigger_handoff", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            9992,
            mainActivityIntent,
            pendingIntentFlags
        )

        val notificationText = "Alert: Trading Node Offline. Heartbeat missed. Tap to resume autonomous execution on this device."

        val notification = NotificationCompat.Builder(this, CRITICAL_CHANNEL_ID)
            .setContentTitle("Alert: Trading Node Offline")
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(CRITICAL_NOTIFICATION_ID, notification)
    }

    private fun executeEngineTakeover() {
        serviceScope.launch {
            try {
                // 1. Update activeDeviceId to this phone and refresh timestamp
                EngineHeartbeatManager.updateActiveDeviceAndTakeover(applicationContext)

                // 2. Fetch secure keys and API budget state from Firestore
                SecureStorageManager.fetchSecureKeysFromFirestore { success ->
                    Log.i(TAG, "API budget state and secure keys re-synced from Firestore: $success")
                }

                // 3. Enable paper trading active state
                UpstoxExecutionService.setPaperTradingActive(true)

                // 4. Boot up TradingBotService locally to take over scanning
                val botIntent = Intent(applicationContext, TradingBotService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    applicationContext.startForegroundService(botIntent)
                } else {
                    applicationContext.startService(botIntent)
                }

                // 5. Dismiss critical notification
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(CRITICAL_NOTIFICATION_ID)

                Log.i(TAG, "Local takeover complete. TradingBotService successfully booted on current device.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed during engine takeover execution", e)
            }
        }
    }

    private fun buildObserverNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)

        return NotificationCompat.Builder(this, OBSERVER_CHANNEL_ID)
            .setContentTitle("Failover Observer Engine")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val observerChannel = NotificationChannel(
                OBSERVER_CHANNEL_ID,
                "Failover Observer Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(observerChannel)

            val criticalChannel = NotificationChannel(
                CRITICAL_CHANNEL_ID,
                "Critical Failover Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            criticalChannel.enableVibration(true)
            manager.createNotificationChannel(criticalChannel)
        }
    }

    override fun onDestroy() {
        heartbeatListener?.remove()
        heartbeatListener = null
        telemetryListener?.remove()
        telemetryListener = null
        observerMonitorJob?.cancel()
        serviceJob.cancel()
        Log.i(TAG, "FailoverObserverService destroyed.")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
