package com.debtfreein.app.data.system

import com.debtfreein.app.data.network.CloudApiClient
import com.debtfreein.app.data.network.CloudStatusResponse
import com.debtfreein.app.data.network.UpstoxExecutionService
import com.debtfreein.app.data.security.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object SystemStatusManager {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pingJob: Job? = null

    private val _cloudStatus = MutableStateFlow<CloudStatusResponse?>(null)
    val cloudStatus: StateFlow<CloudStatusResponse?> = _cloudStatus.asStateFlow()

    private val _isUpstoxConnected = MutableStateFlow(false)
    val isUpstoxConnected: StateFlow<Boolean> = _isUpstoxConnected.asStateFlow()

    private val _isPaperTradingActive = MutableStateFlow(true)
    val isPaperTradingActive: StateFlow<Boolean> = _isPaperTradingActive.asStateFlow()

    private val _isAiScanningActive = MutableStateFlow(true)
    val isAiScanningActive: StateFlow<Boolean> = _isAiScanningActive.asStateFlow()

    private val _isLiveTradingActive = MutableStateFlow(false)
    val isLiveTradingActive: StateFlow<Boolean> = _isLiveTradingActive.asStateFlow()

    init {
        refreshStatus()
        startPeriodicPing()
        listenMasterSwitches()
    }

    fun listenMasterSwitches() {
        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("system_config")
                .document("master_switches")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        val isLive = snapshot.getBoolean("is_live_trading_active") ?: false
                        _isLiveTradingActive.value = isLive
                    }
                }
        } catch (e: Exception) {
            android.util.Log.e("SystemStatusManager", "Error listening to master_switches: ${e.localizedMessage}")
        }
    }

    fun startPeriodicPing() {
        if (pingJob?.isActive == true) return
        pingJob = scope.launch {
            while (isActive) {
                fetchCloudStatus()
                delay(30_000L) // 30s background ping
            }
        }
    }

    suspend fun fetchCloudStatus() {
        try {
            val response = CloudApiClient.service.getLiveBotStatus()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                _cloudStatus.value = data
                if (data.status.equals("online", ignoreCase = true)) {
                    _isUpstoxConnected.value = true
                } else {
                    _isUpstoxConnected.value = TokenManager.isTokenValid()
                }
                _isPaperTradingActive.value = data.isPaperTradingActive
                _isAiScanningActive.value = data.isAiScanningActive
                UpstoxExecutionService.setPaperTradingActive(data.isPaperTradingActive)
            } else {
                fallbackToLocalStatus()
            }
        } catch (e: Exception) {
            fallbackToLocalStatus()
        }
    }

    private fun fallbackToLocalStatus() {
        val connected = TokenManager.isTokenValid()
        _isUpstoxConnected.value = connected
        val paper = UpstoxExecutionService.isPaperTradingActive.value
        _isPaperTradingActive.value = paper
        _isLiveTradingActive.value = connected && !paper
    }

    fun refreshStatus() {
        scope.launch {
            fetchCloudStatus()
        }
    }

    fun setAiScanningActive(active: Boolean) {
        _isAiScanningActive.value = active
    }

    fun setLiveTradingActive(active: Boolean) {
        _isLiveTradingActive.value = active
    }

    fun setPaperTradingActive(active: Boolean) {
        _isPaperTradingActive.value = active
        UpstoxExecutionService.setPaperTradingActive(active)
    }
}
