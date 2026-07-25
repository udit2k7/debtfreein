package com.debtfreein.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "investments")
data class Investment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String, // e.g., "AAPL", "RELIANCE"
    val name: String,
    val quantity: Double,
    val purchasePrice: Double,
    val currentPrice: Double,
    val assetType: String, // "EQUITY", "FUTURE", "OPTION", "MUTUAL_FUND_SIP", "MCX_COMMODITY"
    val expectedReturnApr: Double, // Estimated annual yield (%), e.g., 12.0
    val brokerName: String = "", // e.g., "Upstox", "Zerodha"
    val monthlySipAmount: Double = 0.0 // Value for active SIPs
)
