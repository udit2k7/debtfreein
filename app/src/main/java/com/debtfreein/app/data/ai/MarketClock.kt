package com.debtfreein.app.data.ai

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.LocalTime

enum class MarketState {
    PRE_MARKET,      // 9:00 AM - 9:15 AM
    ACTIVE_TRADING,  // 9:15 AM - 3:30 PM
    MARKET_CLOSED    // 3:30 PM - 9:00 AM
}

object MarketClock {
    private val zoneId = ZoneId.of("Asia/Kolkata")

    fun getCurrentState(): MarketState {
        val now = ZonedDateTime.now(zoneId)
        val time = now.toLocalTime()
        val preMarketStart = LocalTime.of(9, 0)
        val activeStart = LocalTime.of(9, 15)
        val activeEnd = LocalTime.of(15, 30)

        return when {
            (time.equals(preMarketStart) || time.isAfter(preMarketStart)) && time.isBefore(activeStart) -> MarketState.PRE_MARKET
            (time.equals(activeStart) || time.isAfter(activeStart)) && time.isBefore(activeEnd) -> MarketState.ACTIVE_TRADING
            else -> MarketState.MARKET_CLOSED
        }
    }
}
