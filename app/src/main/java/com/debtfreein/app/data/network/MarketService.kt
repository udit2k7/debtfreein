package com.debtfreein.app.data.network

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class LocalQuotaExceededException(message: String) : IOException(message)

object MarketService {
    private const val PREFS_NAME = "api_health_prefs"
    
    private var appContext: Context? = null
    
    private val _apiCallsFlow = MutableStateFlow(0)
    val apiCallsFlow: StateFlow<Int> = _apiCallsFlow.asStateFlow()

    private val _bandwidthBytesFlow = MutableStateFlow(0L)
    val bandwidthBytesFlow: StateFlow<Long> = _bandwidthBytesFlow.asStateFlow()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        val prefs = getPrefs()
        if (prefs != null) {
            checkDateReset(prefs)
            _apiCallsFlow.value = prefs.getInt("api_calls_count", 0)
            _bandwidthBytesFlow.value = prefs.getLong("bandwidth_bytes", 0L)
        }
    }

    private fun getPrefs(): SharedPreferences? {
        return appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Synchronized
    fun getApiCalls(): Int {
        val prefs = getPrefs() ?: return 0
        checkDateReset(prefs)
        return prefs.getInt("api_calls_count", 0)
    }

    @Synchronized
    fun getBandwidthBytes(): Long {
        val prefs = getPrefs() ?: return 0L
        checkDateReset(prefs)
        return prefs.getLong("bandwidth_bytes", 0L)
    }

    private fun checkDateReset(prefs: SharedPreferences) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val storedDate = prefs.getString("current_date", "")
        if (todayStr != storedDate) {
            prefs.edit()
                .putString("current_date", todayStr)
                .putInt("api_calls_count", 0)
                .putLong("bandwidth_bytes", 0L)
                .apply()
            _apiCallsFlow.value = 0
            _bandwidthBytesFlow.value = 0L
        }
    }

    @Synchronized
    private fun trackRequest(bytes: Long) {
        val prefs = getPrefs() ?: return
        checkDateReset(prefs)
        val currentCalls = prefs.getInt("api_calls_count", 0) + 1
        val currentBytes = prefs.getLong("bandwidth_bytes", 0L) + bytes
        prefs.edit()
            .putInt("api_calls_count", currentCalls)
            .putLong("bandwidth_bytes", currentBytes)
            .apply()
        _apiCallsFlow.value = currentCalls
        _bandwidthBytesFlow.value = currentBytes
    }

    /**
     * Updates an investment's current price using a realistic simulated price engine.
     */
    suspend fun fetchLatestPrice(context: Context, symbol: String): Double = withContext(Dispatchers.IO) {
        getSimulatedPrice(symbol)
    }

    /**
     * Generates a realistic simulated price based on tickers, ensuring stability.
     */
    private fun getSimulatedPrice(symbol: String): Double {
        val upperSymbol = symbol.uppercase()
        
        // Return realistic values based on typical stock levels
        val basePrice = when {
            upperSymbol == "AAPL" -> 180.0
            upperSymbol == "GOOG" -> 150.0
            upperSymbol == "TSLA" -> 220.0
            upperSymbol == "RELIANCE" -> 2800.0
            upperSymbol == "TCS" -> 3900.0
            upperSymbol == "NIFTY26JUL22000CE" -> 150.0 // F&O Option
            upperSymbol.contains("CE") || upperSymbol.contains("PE") -> 120.0 // Generic Call/Put Option
            else -> 100.0
        }
        
        // Add minor fluctuations (-2.5% to +2.5%) to make it look alive
        val fluctuation = basePrice * (Random.nextDouble(-0.025, 0.025))
        return Math.round((basePrice + fluctuation) * 100.0) / 100.0
    }
}
