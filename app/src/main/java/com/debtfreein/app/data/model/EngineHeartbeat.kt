package com.debtfreein.app.data.model

data class EngineHeartbeat(
    val lastActiveTimestamp: Long = 0L,
    val activeDeviceId: String = "",
    val currentOpenPositions: Map<String, Int> = emptyMap()
)
