package com.debtfreein.app.data.model

data class TelemetryMetrics(
    val botStatus: String = "Idling", // "Scanning", "Evaluating", "Idling", "Suspended"
    val lastDecisionReason: String = "System initialized. Standing by for market signals.",
    val activeTripwires: String = "ATR (14): Dynamic threshold nominal",
    val liveSessionPnL: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
