package com.debtfreein.app.data.model

import kotlin.math.abs

data class VirtualLedger(
    val balance: Double = 10000.0
) {
    /**
     * Dynamically calculates position sizing based on Stop-Loss risk constraint.
     * The bot may never allocate more than 2% of the total ledger balance to a single trade's Stop-Loss risk.
     */
    fun calculateSizedQuantity(entryPrice: Double, stopLossPrice: Double): Int {
        val maxRisk = balance * 0.02
        val riskPerShare = abs(entryPrice - stopLossPrice).coerceAtLeast(0.01)
        return (maxRisk / riskPerShare).toInt().coerceAtLeast(1)
    }
}
