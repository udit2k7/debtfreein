package com.debtfreein.app.data.ai

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.debtfreein.app.data.model.EngineHeartbeat
import com.debtfreein.app.data.network.UpstoxExecutionService
import com.debtfreein.app.data.logging.FileLogger
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object EngineHeartbeatManager {
    private const val TAG = "EngineHeartbeatManager"
    const val COLLECTION_NAME = "EngineHeartbeat"
    const val DOCUMENT_ID = "status"

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_device"
    }

    fun writeHeartbeat(context: Context, onComplete: (Boolean) -> Unit = {}) {
        val db = FirebaseFirestore.getInstance()
        val deviceId = getDeviceId(context)
        val openPositions = UpstoxExecutionService.getOpenPositions()
        val timestamp = System.currentTimeMillis()

        val heartbeatData = hashMapOf(
            "lastActiveTimestamp" to timestamp,
            "activeDeviceId" to deviceId,
            "currentOpenPositions" to openPositions
        )

        db.collection(COLLECTION_NAME).document(DOCUMENT_ID)
            .set(heartbeatData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Heartbeat updated successfully for device: $deviceId")
                FileLogger.log("FIRESTORE_HEARTBEAT", "Heartbeat write success | Device: $deviceId | Timestamp: $timestamp | OpenPositions: ${openPositions.size}", context)
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to write heartbeat to Firestore", e)
                FileLogger.log("FIRESTORE_HEARTBEAT", "Heartbeat write failed | Error: ${e.message}", context)
                onComplete(false)
            }
    }

    fun updateActiveDeviceAndTakeover(context: Context, onComplete: (Boolean) -> Unit = {}) {
        val db = FirebaseFirestore.getInstance()
        val deviceId = getDeviceId(context)
        val openPositions = UpstoxExecutionService.getOpenPositions()
        val timestamp = System.currentTimeMillis()

        val heartbeatData = hashMapOf(
            "lastActiveTimestamp" to timestamp,
            "activeDeviceId" to deviceId,
            "currentOpenPositions" to openPositions
        )

        db.collection(COLLECTION_NAME).document(DOCUMENT_ID)
            .set(heartbeatData, SetOptions.merge())
            .addOnSuccessListener {
                Log.i(TAG, "Engine ownership successfully transferred to device: $deviceId")
                FileLogger.log("FIRESTORE_HEARTBEAT", "Engine Ownership Transfer Success | ActiveDevice: $deviceId | Timestamp: $timestamp", context)
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update active device ID for takeover", e)
                FileLogger.log("FIRESTORE_HEARTBEAT", "Engine Ownership Transfer Failed | Error: ${e.message}", context)
                onComplete(false)
            }
    }
}
