package com.debtfreein.app.data.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Thread-safe singleton object managing day-trading capital constraints natively
 * and calculating standard Indian market intraday friction (Upstox model).
 */
object CapitalAllocator {
    @Volatile
    private var _totalBalance: Double = 0.0

    @Volatile
    private var _tradeableCapital: Double = 0.0

    @Volatile
    private var _lockedCapital: Double = 0.0

    @Volatile
    private var _targetNetProfit: Double = 0.0

    private val _currentNetPnL = MutableStateFlow<Double>(0.0)
    val currentNetPnL: StateFlow<Double> = _currentNetPnL.asStateFlow()

    var totalBalance: Double
        @Synchronized get() = _totalBalance
        @Synchronized set(value) {
            _totalBalance = value
        }

    var tradeableCapital: Double
        @Synchronized get() = _tradeableCapital
        @Synchronized set(value) {
            _tradeableCapital = value
        }

    var lockedCapital: Double
        @Synchronized get() = _lockedCapital
        @Synchronized set(value) {
            _lockedCapital = value
        }

    var targetNetProfit: Double
        @Synchronized get() = _targetNetProfit
        @Synchronized set(value) {
            _targetNetProfit = value
        }

    /**
     * Models standard Indian market intraday friction for Upstox:
     * - Flat ₹20 brokerage per executed order leg (2 legs: buy + sell = ₹40).
     * - 18% GST on brokerage (18% of ₹40 = ₹7.20).
     * - STT (Securities Transaction Tax) of 0.025% on the sell leg.
     * - SEBI turnover fees: ₹10 per crore (0.0001% of total trade turnover).
     *
     * @param tradeValue The total volume/turnover of the trade (buy value + sell value).
     * @return The total calculated friction/charges in Indian Rupees (INR).
     */
    @Synchronized
    fun calculateUpstoxFriction(tradeValue: Double): Double {
        val brokerage = 2 * 20.0 // Buy leg + sell leg brokerage
        val gst = brokerage * 0.18 // 18% GST on brokerage
        val stt = (tradeValue / 2.0) * 0.00025 // 0.025% STT on the sell side leg
        val sebiTurnoverFee = tradeValue * 0.000001 // SEBI turnover fee of ₹10 per crore (0.0001% of turnover)
        return brokerage + gst + stt + sebiTurnoverFee
    }

    /**
     * Subtracts the calculated dynamic friction from the gross returns and updates the net PnL StateFlow.
     *
     * @param grossPnL The gross profit/loss of the trade before charges.
     * @param volume The total trade volume/turnover (buy value + sell value).
     */
    @Synchronized
    fun updatePnLAfterTrade(grossPnL: Double, volume: Double) {
        val friction = calculateUpstoxFriction(volume)
        val netPnL = grossPnL - friction
        _currentNetPnL.value = _currentNetPnL.value + netPnL
    }
}
