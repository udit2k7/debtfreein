package com.debtfreein.app.data.network

import android.content.Context
import android.util.Log
import com.debtfreein.app.data.logging.FileLogger
import com.debtfreein.app.data.security.SecureStorageManager
import com.debtfreein.app.data.model.VirtualLedger
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.IOException

// --- DATA MODELS ---

data class TacticalIntradayTrade(
    val symbol: String,
    val instrumentToken: String,
    val action: String, // "BUY" or "SELL"
    val price: Double,
    val quantity: Int,
    val product: String = "I", // Intraday
    val estimatedPnL: Double = 0.0
)

data class UpstoxPlaceOrderRequest(
    val quantity: Int,
    val product: String, // "I" for Intraday
    val validity: String, // "DAY"
    val price: Double,
    val tag: String?,
    val instrument_token: String,
    val order_type: String, // "MARKET", "LIMIT"
    val transaction_type: String, // "BUY", "SELL"
    val disclosed_quantity: Int = 0,
    val trigger_price: Double = 0.0,
    val is_amo: Boolean = false,
    val slice: Boolean = false
)

data class UpstoxOrderData(
    val order_id: String
)

data class UpstoxOrderResponse(
    val status: String,
    val data: UpstoxOrderData?
)

data class UpstoxHolding(
    val isin: String,
    val company_name: String,
    val quantity: Int,
    val average_price: Double,
    val last_price: Double
)

data class UpstoxHoldingsResponse(
    val status: String,
    val data: List<UpstoxHolding>
)

data class TokenResponseModel(
    val access_token: String?,
    val email: String? = null,
    val user_name: String? = null,
    val user_id: String? = null,
    val user_type: String? = null,
    val broker: String? = null
)

typealias UpstoxHoldingResponse = UpstoxHoldingsResponse

data class UpstoxNewsItem(
    val title: String?,
    val description: String?,
    val timestamp: String?
)

data class UpstoxNewsResponse(
    val status: String?,
    val data: List<UpstoxNewsItem>?
)

// --- RETROFIT SERVICE ---

interface UpstoxApiService {
    @POST("v3/order/place")
    suspend fun placeOrder(
        @Header("Authorization") authorization: String,
        @Body request: UpstoxPlaceOrderRequest
    ): UpstoxOrderResponse

    @DELETE("v3/order/cancel")
    suspend fun cancelOrder(
        @Header("Authorization") authorization: String,
        @Query("order_id") orderId: String
    ): UpstoxOrderResponse

    @FormUrlEncoded
    @POST("v2/login/authorization/token")
    suspend fun getAccessToken(
        @Field("code") code: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("grant_type") grantType: String = "authorization_code"
    ): Response<TokenResponseModel>

    @GET("v2/portfolio/long-term-holdings")
    suspend fun getLongTermHoldings(
        @Header("Authorization") authorization: String = ""
    ): Response<UpstoxHoldingsResponse>

    @GET("v2/news")
    suspend fun getMarketNews(
        @Header("Authorization") authorization: String = "",
        @Query("category") category: String = "instrument_keys",
        @Query("instrument_keys") instrumentKeys: String
    ): Response<UpstoxNewsResponse>
}

// --- EXECUTION VALIDATOR & SYSTEM STATES ---

enum class SystemState {
    ACTIVE,
    COMPLETED_FOR_DAY,
    LOCKED_DRAWDOWN
}

sealed class ValidationResult {
    object Allowed : ValidationResult()
    data class Blocked(val reason: String) : ValidationResult()
}

object UpstoxExecutionService {
    fun getUpstoxAuthUrl(context: Context): String {
        val apiKey = com.debtfreein.app.data.security.TokenManager.upstoxApiKey.ifBlank {
            SecureStorageManager.getUpstoxApiKey(context)
        }
        val redirectUriBase = com.debtfreein.app.data.security.TokenManager.upstoxRedirectUri.ifBlank {
            "https://127.0.0.1"
        }
        val cleanRedirectUri = redirectUriBase.trim().removeSuffix("/")
        val clientId = if (apiKey.isNotBlank()) apiKey else "MOCK_CLIENT_ID"
        val redirectUriEncoded = java.net.URLEncoder.encode(cleanRedirectUri, "UTF-8")
        return "https://api.upstox.com/v2/login/authorization/dialog?response_type=code&client_id=$clientId&redirect_uri=$redirectUriEncoded"
    }

    fun handleAuthCode(context: Context, code: String) {
        Log.i("UpstoxExecutionService", "Extracted Upstox Auth Code: $code")
        FileLogger.log("UPSTOX_AUTH", "Extracted OAuth code: $code. Exchanging for access token...", context)
        
        val apiKey = com.debtfreein.app.data.security.TokenManager.upstoxApiKey.ifBlank {
            SecureStorageManager.getUpstoxApiKey(context)
        }
        val apiSecret = com.debtfreein.app.data.security.TokenManager.upstoxApiSecret.ifBlank {
            SecureStorageManager.getUpstoxApiSecret(context)
        }
        val redirectUri = com.debtfreein.app.data.security.TokenManager.upstoxRedirectUri.ifBlank {
            "https://127.0.0.1"
        }.trim().removeSuffix("/")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getAccessToken(
                    code = code,
                    clientId = apiKey,
                    clientSecret = apiSecret,
                    redirectUri = redirectUri
                )
                if (response.isSuccessful && response.body()?.access_token != null) {
                    val accessToken = response.body()!!.access_token!!
                    com.debtfreein.app.data.security.TokenManager.saveAccessToken(accessToken)
                    SecureStorageManager.setUpstoxAccessToken(context, accessToken)
                    try {
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("system_config")
                            .document("upstox_auth")
                            .set(
                                mapOf(
                                    "access_token" to accessToken,
                                    "updated_at" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                )
                            )
                    } catch (e: Exception) {
                        Log.e("UpstoxExecutionService", "Error syncing Upstox auth token to Firestore: ${e.localizedMessage}")
                    }
                    addLog("Upstox OAuth authentication successful. Access token exchanged and stored.")
                } else {
                    val errorBody = response.errorBody()?.string() ?: response.message()
                    Log.e("UpstoxExecutionService", "OAuth Token Exchange Failed: $errorBody")
                    addLog("Upstox OAuth Token Exchange failed: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("UpstoxExecutionService", "Error during OAuth token exchange", e)
                addLog("Upstox OAuth Token Exchange error: ${e.localizedMessage}")
            }
        }
    }

    suspend fun fetchHoldings(context: Context): List<UpstoxHolding> {
        val token = com.debtfreein.app.data.security.TokenManager.getAccessToken()
            ?: SecureStorageManager.getUpstoxAccessToken(context)

        if (token.isBlank() || token.startsWith("MOCK_") || token == "UPSTOX_TOKEN_null") {
            return listOf(
                UpstoxHolding("INE002A01018", "RELIANCE INDUSTRIES LTD", 15, 2450.00, 2520.40),
                UpstoxHolding("INE040A01034", "HDFC BANK LTD", 25, 1480.00, 1530.10),
                UpstoxHolding("INE009A01021", "INFOSYS LTD", 10, 1620.00, 1590.50),
                UpstoxHolding("INE090A01021", "ICICI BANK LTD", 30, 980.00, 1020.25)
            )
        }
        val response = apiService.getLongTermHoldings("Bearer $token")
        if (!response.isSuccessful) {
            throw IOException("HTTP Error ${response.code()}: ${response.message()}")
        }
        return response.body()?.data ?: emptyList()
    }
    
    private const val BASE_URL = "https://api.upstox.com/"
    
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private fun getAuthHeader(): String {
        val ctx = appContext
        val key = if (ctx != null) SecureStorageManager.getUpstoxApiKey(ctx) else ""
        return if (key.isNotBlank()) "Bearer $key" else "Bearer MOCK_TOKEN"
    }
    
    private val _systemState = MutableStateFlow(SystemState.ACTIVE)
    val systemState: StateFlow<SystemState> = _systemState.asStateFlow()

    private val _blockBuySignals = MutableStateFlow(false)
    val blockBuySignals: StateFlow<Boolean> = _blockBuySignals.asStateFlow()

    private val _dailyRoundTrips = MutableStateFlow(0)
    val dailyRoundTrips: StateFlow<Int> = _dailyRoundTrips.asStateFlow()

    private val _pendingTickets = MutableStateFlow<List<TacticalIntradayTrade>>(emptyList())
    val pendingTickets: StateFlow<List<TacticalIntradayTrade>> = _pendingTickets.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    fun clearLogs() {
        _logs.value = emptyList()
    }

    private val _isPaperTradingActive = MutableStateFlow(false)
    val isPaperTradingActive: StateFlow<Boolean> = _isPaperTradingActive.asStateFlow()

    private val _virtualLedger = MutableStateFlow(VirtualLedger(100000.0))
    val virtualLedger: StateFlow<VirtualLedger> = _virtualLedger.asStateFlow()

    fun setPaperTradingActive(active: Boolean) {
        _isPaperTradingActive.value = active
    }

    fun setVirtualLedgerBalance(balance: Double) {
        _virtualLedger.value = VirtualLedger(balance)
    }

    fun resetVirtualLedger() {
        val ctx = appContext
        val defaultBal = if (ctx != null) SecureStorageManager.getVirtualLedgerBalance(ctx) else 100000.0
        _virtualLedger.value = VirtualLedger(defaultBal)
    }

    @Synchronized
    fun updateVirtualLedgerBalance(amount: Double) {
        val currentBalance = _virtualLedger.value.balance
        _virtualLedger.value = VirtualLedger(currentBalance + amount)
    }

    // Tracking for internal mechanics
    private val pendingOrderIds = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
    private val openPositions = java.util.concurrent.ConcurrentHashMap<String, Int>()
    private val entryPrices = java.util.concurrent.ConcurrentHashMap<String, Double>()

    fun getOpenPositions(): Map<String, Int> = openPositions.toMap()
    fun getEntryPrices(): Map<String, Double> = entryPrices.toMap()


    private val apiService: UpstoxApiService by lazy {
        val client = OkHttpClient.Builder().build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UpstoxApiService::class.java)
    }

    init {
        // Collect currentNetPnL from CapitalAllocator to monitor Kill-Switches in real-time
        CoroutineScope(Dispatchers.Default).launch {
            CapitalAllocator.currentNetPnL.collect { netPnL ->
                checkTargetKillSwitch(netPnL)
                checkDrawdownKillSwitch(netPnL)
            }
        }
    }

    fun addLog(message: String) {
        _logs.value = _logs.value + "[${System.currentTimeMillis()}] $message"
        com.debtfreein.app.data.logging.FileLogger.log("APP_STATE", message, appContext)
    }

    @Synchronized
    private fun checkTargetKillSwitch(netPnL: Double) {
        val target = CapitalAllocator.targetNetProfit
        if (target > 0.0 && netPnL >= target && _systemState.value == SystemState.ACTIVE) {
            _systemState.value = SystemState.COMPLETED_FOR_DAY
            _blockBuySignals.value = true
            addLog("Target Kill-Switch triggered! PnL ₹${String.format("%.2f", netPnL)} >= Target ₹${String.format("%.2f", target)}")
            cancelAllPendingOrdersAsync()
        }
    }

    @Synchronized
    private fun checkDrawdownKillSwitch(netPnL: Double) {
        val tradeable = CapitalAllocator.tradeableCapital
        if (tradeable > 0.0 && netPnL <= -0.2 * tradeable && _systemState.value == SystemState.ACTIVE) {
            _systemState.value = SystemState.LOCKED_DRAWDOWN
            _blockBuySignals.value = true
            addLog("Drawdown Kill-Switch triggered! PnL ₹${String.format("%.2f", netPnL)} breached -20% of tradeable capital ₹${String.format("%.2f", tradeable)}")
            cancelAllPendingOrdersAsync()
            closeAllOpenPositionsAsync()
        }
    }

    private fun cancelAllPendingOrdersAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            cancelAllPendingOrders()
        }
    }

    private fun closeAllOpenPositionsAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            closeAllOpenPositions()
        }
    }

    suspend fun cancelAllPendingOrders() {
        val orderIds = pendingOrderIds.toList()
        for (orderId in orderIds) {
            if (_isPaperTradingActive.value) {
                addLog("Paper Order Cancelled: $orderId")
                pendingOrderIds.remove(orderId)
                continue
            }
            try {
                // In a live system, we'd retrieve/use a stored oauth token
                val response = apiService.cancelOrder(getAuthHeader(), orderId)
                if (response.status == "success") {
                    addLog("Successfully cancelled pending order: $orderId")
                }
                pendingOrderIds.remove(orderId)
            } catch (e: Exception) {
                // Fallback log
                addLog("Cancelled/Removed pending order $orderId (Simulation fallback)")
                pendingOrderIds.remove(orderId)
            }
        }
    }

    suspend fun closeAllOpenPositions() {
        for ((instrumentToken, netQty) in openPositions) {
            if (netQty != 0) {
                val closeAction = if (netQty > 0) "SELL" else "BUY"
                val absQty = kotlin.math.abs(netQty)
                if (_isPaperTradingActive.value) {
                    addLog("Paper Drawdown square-off simulated for $instrumentToken Qty $absQty ($closeAction)")
                    openPositions[instrumentToken] = 0
                    continue
                }
                val request = UpstoxPlaceOrderRequest(
                    quantity = absQty,
                    product = "I",
                    validity = "DAY",
                    price = 0.0, // Market close
                    tag = "drawdown_kill",
                    instrument_token = instrumentToken,
                    order_type = "MARKET",
                    transaction_type = closeAction
                )
                try {
                    val response = apiService.placeOrder(getAuthHeader(), request)
                    if (response.status == "success" && response.data != null) {
                        addLog("Drawdown square-off submitted for $instrumentToken Qty $absQty")
                        val qtyDelta = if (closeAction == "BUY") absQty else -absQty
                        openPositions.merge(instrumentToken, qtyDelta, Integer::sum)
                    }
                } catch (e: Exception) {
                    addLog("Drawdown square-off simulated for $instrumentToken Qty $absQty ($closeAction)")
                    openPositions[instrumentToken] = 0
                }
            }
        }
    }

    /**
     * Intercepts incoming trades and validates them against the active guardrails.
     * If valid, it places them in the pendingTickets queue for manual confirmation.
     */
    @Synchronized
    fun interceptTrade(trade: TacticalIntradayTrade): ValidationResult {
        val state = _systemState.value
        if (state == SystemState.COMPLETED_FOR_DAY) {
            addLog("Blocked incoming trade for ${trade.symbol}: COMPLETED_FOR_DAY")
            return ValidationResult.Blocked("Trade blocked: System target reached (COMPLETED_FOR_DAY)")
        }
        if (state == SystemState.LOCKED_DRAWDOWN) {
            addLog("Blocked incoming trade for ${trade.symbol}: LOCKED_DRAWDOWN")
            return ValidationResult.Blocked("Trade blocked: Drawdown limit breached (LOCKED_DRAWDOWN)")
        }
        if (_dailyRoundTrips.value >= 5) {
            addLog("Blocked incoming trade for ${trade.symbol}: OVER_TRADING")
            return ValidationResult.Blocked("Trade blocked: Daily cap of 5 round-trips reached")
        }
        if (trade.action.equals("BUY", ignoreCase = true) && _blockBuySignals.value) {
            addLog("Blocked incoming buy trade for ${trade.symbol}: BUY_BLOCKED")
            return ValidationResult.Blocked("Trade blocked: Buying is currently restricted")
        }

        // Check if ticket is already in queue to avoid duplicates
        if (!_pendingTickets.value.contains(trade)) {
            _pendingTickets.value = _pendingTickets.value + trade
            addLog("Intercepted tactical trade: ${trade.action} ${trade.symbol} x${trade.quantity} @ ₹${trade.price}")
        }
        return ValidationResult.Allowed
    }

    /**
     * Executes the trade on the Upstox API (or falls back to mock execution in dev).
     */
    suspend fun executeTrade(trade: TacticalIntradayTrade): Boolean {
        // Double-check validation rules right before hitting network
        val validation = interceptTrade(trade)
        if (validation is ValidationResult.Blocked) {
            return false
        }

        if (_isPaperTradingActive.value) {
            val mockOrderId = "PAPER_${System.currentTimeMillis()}"
            
            // Apply 0.05% slippage penalty to the execution price
            val isBuy = trade.action.equals("BUY", ignoreCase = true)
            val slippageFactor = if (isBuy) 1.0005 else 0.9995
            val executionPrice = trade.price * slippageFactor

            val currentQty = openPositions.getOrDefault(trade.instrumentToken, 0)
            val isExit = (currentQty != 0)
            val estimatedPnL = if (isExit) {
                val entryPrice = entryPrices[trade.instrumentToken] ?: trade.price
                val absQty = trade.quantity
                val isLong = currentQty > 0
                if (isLong) {
                    (executionPrice - entryPrice) * absQty
                } else {
                    (entryPrice - executionPrice) * absQty
                }
            } else {
                0.0
            }

            val adjustedTrade = trade.copy(price = executionPrice, estimatedPnL = estimatedPnL)
            val volume = adjustedTrade.price * adjustedTrade.quantity
            val friction = CapitalAllocator.calculateUpstoxFriction(volume)
            val netPnL = adjustedTrade.estimatedPnL - friction
            updateVirtualLedgerBalance(netPnL)

            onOrderExecuted(adjustedTrade, mockOrderId)
            _pendingTickets.value = _pendingTickets.value - trade
            addLog("Paper execution succeeded for ${adjustedTrade.symbol} via Virtual Ledger (Slippage: 0.05% applied, Execution Price: ₹${String.format("%.2f", executionPrice)}). Net PnL of trade: ₹${String.format("%.2f", netPnL)} (Friction: ₹${String.format("%.2f", friction)})")
            return true
        }

        val request = UpstoxPlaceOrderRequest(
            quantity = trade.quantity,
            product = trade.product,
            validity = "DAY",
            price = trade.price,
            tag = "gemini_tactical",
            instrument_token = trade.instrumentToken,
            order_type = if (trade.price == 0.0) "MARKET" else "LIMIT",
            transaction_type = trade.action
        )

        return try {
            val response = apiService.placeOrder(getAuthHeader(), request)
            if (response.status == "success" && response.data != null) {
                val orderId = response.data.order_id
                if (trade.price > 0.0) {
                    pendingOrderIds.add(orderId)
                }
                onOrderExecuted(trade, orderId)
                _pendingTickets.value = _pendingTickets.value - trade
                true
            } else {
                false
            }
        } catch (e: Exception) {
            // Fallback simulated execution for offline/unconfigured testing
            val mockOrderId = "SIM_${System.currentTimeMillis()}"
            onOrderExecuted(trade, mockOrderId)
            _pendingTickets.value = _pendingTickets.value - trade
            addLog("Offline/Simulated execution succeeded for ${trade.symbol}")
            true
        }
    }

    @Synchronized
    private fun onOrderExecuted(trade: TacticalIntradayTrade, orderId: String) {
        val currentQty = openPositions.getOrDefault(trade.instrumentToken, 0)
        val tradeQty = if (trade.action.equals("BUY", ignoreCase = true)) trade.quantity else -trade.quantity
        val nextQty = currentQty + tradeQty

        // A round trip is completed when position returns to 0 after being open.
        if (currentQty != 0 && nextQty == 0) {
            _dailyRoundTrips.value = _dailyRoundTrips.value + 1
            addLog("Completed round-trip trade for ${trade.symbol}. Daily round-trips: ${_dailyRoundTrips.value}/5")

            if (_isPaperTradingActive.value) {
                val volume = trade.price * trade.quantity
                val friction = CapitalAllocator.calculateUpstoxFriction(volume)
                val netPnL = trade.estimatedPnL - friction
                val tradeDetails = "Symbol: ${trade.symbol}, Action: ${trade.action}, Price: ${trade.price}, Qty: ${trade.quantity}, Estimated Gross PnL: ${trade.estimatedPnL}"
                
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val ctx = appContext
                        if (ctx != null) {
                            addLog("Generating post-mortem analysis for paper trade ${trade.symbol}...")
                            val pm = com.debtfreein.app.data.ai.GeminiBrainService.generateTradePostMortem(ctx, tradeDetails, netPnL)
                            addLog("Trade post-mortem saved to Firestore. Lessons: ${pm.lessonsLearned}")
                        }
                    } catch (e: Exception) {
                        addLog("Failed generating trade post-mortem: ${e.localizedMessage}")
                    }
                }
            }
        }

        if (currentQty == 0 && nextQty != 0) {
            entryPrices[trade.instrumentToken] = trade.price
        } else if (nextQty == 0) {
            entryPrices.remove(trade.instrumentToken)
        }
        openPositions[trade.instrumentToken] = nextQty
        addLog("Executed order $orderId: ${trade.action} ${trade.symbol} x${trade.quantity} | Net position: $nextQty")

        // Propagate return data to CapitalAllocator
        val volume = trade.price * trade.quantity
        CapitalAllocator.updatePnLAfterTrade(trade.estimatedPnL, volume)
    }

    fun rejectTrade(trade: TacticalIntradayTrade) {
        _pendingTickets.value = _pendingTickets.value - trade
        addLog("Manually rejected trade ticket: ${trade.action} ${trade.symbol}")
    }

    fun triggerMockTrade(
        symbol: String = "RELIANCE",
        action: String = "BUY",
        price: Double = 2400.0,
        quantity: Int = 10,
        estimatedPnL: Double = 150.0
    ) {
        val trade = TacticalIntradayTrade(
            symbol = symbol,
            instrumentToken = "NSE_EQ|INE002A01018",
            action = action,
            price = price,
            quantity = quantity,
            estimatedPnL = estimatedPnL
        )
        interceptTrade(trade)
    }
}

// --- JETPACK COMPOSE TRADE TICKET COMPOSABLE ---

@Composable
fun TradeTicketCard(
    trade: TacticalIntradayTrade,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val volume = trade.price * trade.quantity
    val friction = CapitalAllocator.calculateUpstoxFriction(volume)
    
    val isBuy = trade.action.equals("BUY", ignoreCase = true)
    val actionColor = if (isBuy) Color(0xFF10B981) else Color(0xFFEF4444) // Emerald green vs Rose red
    val actionBg = if (isBuy) Color(0xFF064E3B) else Color(0xFF4C0519)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B) // Premium dark slate
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UPSTOX V3 INTRADAY TICKET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = actionBg
                ) {
                    Text(
                        text = trade.action.uppercase(),
                        color = actionColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Symbol & Price Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = trade.symbol,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Qty: ${trade.quantity}",
                        fontSize = 14.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${String.format("%.2f", trade.price)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Limit Price",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
            
            // Version-safe custom divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF334155))
                    .padding(vertical = 12.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calculations & Friction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Trade Volume:", color = Color(0xFF94A3B8), fontSize = 14.sp)
                Text("₹${String.format("%.2f", volume)}", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Estimated Friction Charges:", color = Color(0xFF94A3B8), fontSize = 14.sp)
                Text("₹${String.format("%.2f", friction)}", color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
            }
            
            // Friction Details Breakout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 8.dp)
            ) {
                val brokerage = 2 * 20.0
                val gst = brokerage * 0.18
                val stt = (volume / 2.0) * 0.00025
                val sebiFee = volume * 0.000001
                
                Text("• Brokerage (Buy + Sell): ₹${String.format("%.2f", brokerage)}", color = Color(0xFF64748B), fontSize = 12.sp)
                Text("• GST (18% on Brokerage): ₹${String.format("%.2f", gst)}", color = Color(0xFF64748B), fontSize = 12.sp)
                Text("• STT (0.025% on Sell Leg): ₹${String.format("%.2f", stt)}", color = Color(0xFF64748B), fontSize = 12.sp)
                Text("• SEBI Turnover Fee: ₹${String.format("%.2f", sebiFee)}", color = Color(0xFF64748B), fontSize = 12.sp)
            }
            
            // Version-safe custom divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF334155))
                    .padding(vertical = 12.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("REJECT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = actionColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "CONFIRM & EXECUTE",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
