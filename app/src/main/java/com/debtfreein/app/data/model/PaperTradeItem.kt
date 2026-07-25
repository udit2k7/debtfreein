package com.debtfreein.app.data.model

data class PaperTradeItem(
    val id: String = "",
    val symbol: String = "",
    val action: String = "",
    val status: String = "OPEN",
    val quantity: Int = 100,
    val conviction: String = "",
    val entryPrice: Double = 0.0,
    val targetPrice: Double = 0.0,
    val stopLoss: Double = 0.0,
    val reason: String = "",
    val riskAnalysis: String = "",
    val patternName: String = "",
    val visionConfidence: Int = 0,
    val visionStatus: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
