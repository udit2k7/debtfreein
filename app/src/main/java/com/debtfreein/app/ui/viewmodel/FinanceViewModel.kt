package com.debtfreein.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.debtfreein.app.data.ai.GeminiBrainService
import com.debtfreein.app.data.db.AppDatabase
import com.debtfreein.app.data.model.CreditCard
import com.debtfreein.app.data.model.Expense
import com.debtfreein.app.data.model.Investment
import com.debtfreein.app.data.model.SystemLog
import com.debtfreein.app.data.model.VirtualLedger
import com.debtfreein.app.data.network.MarketService
import com.debtfreein.app.data.network.UpstoxExecutionService
import com.debtfreein.app.data.sms.SmsParser
import com.debtfreein.app.data.repository.TradePostMortem
import com.debtfreein.app.data.security.SecureStorageManager
import com.debtfreein.app.data.model.TelemetryMetrics
import com.debtfreein.app.data.model.PaperTradeItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val prefs = application.getSharedPreferences("debtfreein_prefs", Context.MODE_PRIVATE)

    // Flow states from DB
    val cards: StateFlow<List<CreditCard>> = db.cardDao().getAllCardsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val expenses: StateFlow<List<Expense>> = db.expenseDao().getAllExpensesFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val investments: StateFlow<List<Investment>> = db.investmentDao().getAllInvestmentsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val logs: StateFlow<List<SystemLog>> = db.systemLogDao().getRecentLogsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val postMortemRepo = com.debtfreein.app.data.repository.TradePostMortemRepository()
    val postMortems: StateFlow<List<TradePostMortem>> = postMortemRepo.getAllPostMortemsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _telemetryMetrics = MutableStateFlow(TelemetryMetrics())
    val telemetryMetrics: StateFlow<TelemetryMetrics> = _telemetryMetrics.asStateFlow()

    private var paperTradesListener: ListenerRegistration? = null
    private val _liveCloudTrades = MutableStateFlow<List<PaperTradeItem>>(emptyList())
    val liveCloudTrades: StateFlow<List<PaperTradeItem>> = _liveCloudTrades.asStateFlow()

    private val _tradeNotification = MutableSharedFlow<PaperTradeItem>(extraBufferCapacity = 64)
    val tradeNotification: SharedFlow<PaperTradeItem> = _tradeNotification.asSharedFlow()

    init {
        startTelemetryListener()
        startPaperTradesListener()
    }

    fun startTelemetryListener() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .collection("config").document("bot_telemetry")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    val botStatus = snapshot.getString("botStatus") ?: "Idling"
                    val lastReason = snapshot.getString("lastDecisionReason") ?: "System initialized."
                    val activeTripwires = snapshot.getString("activeTripwires") ?: "ATR (14): Dynamic threshold nominal"
                    val pnl = snapshot.getDouble("liveSessionPnL") ?: 0.0
                    _telemetryMetrics.value = TelemetryMetrics(
                        botStatus = botStatus,
                        lastDecisionReason = lastReason,
                        activeTripwires = activeTripwires,
                        liveSessionPnL = pnl,
                        timestamp = snapshot.getLong("timestamp") ?: System.currentTimeMillis()
                    )
                }
            }
    }

    fun startPaperTradesListener() {
        if (paperTradesListener != null) return
        var isInitialLoad = true
        paperTradesListener = FirebaseFirestore.getInstance()
            .collection("paper_trades")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val items = snapshot.documents.map { doc ->
                    PaperTradeItem(
                        id = doc.id,
                        symbol = doc.getString("symbol") ?: doc.getString("ticker") ?: "",
                        action = doc.getString("action") ?: doc.getString("side") ?: "",
                        status = doc.getString("status") ?: "OPEN",
                        quantity = doc.getLong("quantity")?.toInt() ?: 100,
                        conviction = doc.get("conviction")?.toString() ?: "",
                        entryPrice = doc.getDouble("entry_price") ?: 0.0,
                        targetPrice = doc.getDouble("target_price") ?: 0.0,
                        stopLoss = doc.getDouble("stop_loss") ?: 0.0,
                        reason = doc.getString("reasoning") ?: doc.getString("reason") ?: doc.getString("rationale") ?: "",
                        riskAnalysis = doc.getString("risk_analysis") ?: "",
                        patternName = doc.getString("pattern_name") ?: "",
                        visionConfidence = doc.getLong("vision_confidence")?.toInt() ?: doc.getDouble("vision_confidence")?.toInt() ?: 0,
                        visionStatus = doc.getString("vision_status") ?: "",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    )
                }
                _liveCloudTrades.value = items

                if (!isInitialLoad) {
                    for (change in snapshot.documentChanges) {
                        if (change.type == DocumentChange.Type.ADDED) {
                            val doc = change.document
                            val item = PaperTradeItem(
                                id = doc.id,
                                symbol = doc.getString("symbol") ?: doc.getString("ticker") ?: "",
                                action = doc.getString("action") ?: doc.getString("side") ?: "",
                                status = doc.getString("status") ?: "OPEN",
                                quantity = doc.getLong("quantity")?.toInt() ?: 100,
                                conviction = doc.get("conviction")?.toString() ?: "",
                                entryPrice = doc.getDouble("entry_price") ?: 0.0,
                                targetPrice = doc.getDouble("target_price") ?: 0.0,
                                stopLoss = doc.getDouble("stop_loss") ?: 0.0,
                                reason = doc.getString("reasoning") ?: doc.getString("reason") ?: doc.getString("rationale") ?: "",
                                riskAnalysis = doc.getString("risk_analysis") ?: "",
                                patternName = doc.getString("pattern_name") ?: "",
                                visionConfidence = doc.getLong("vision_confidence")?.toInt() ?: doc.getDouble("vision_confidence")?.toInt() ?: 0,
                                visionStatus = doc.getString("vision_status") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            )
                            _tradeNotification.tryEmit(item)
                        }
                    }
                }
                isInitialLoad = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        paperTradesListener?.remove()
    }

    // API Keys (secure preferences)
    private val _geminiApiKey = MutableStateFlow(SecureStorageManager.getGeminiApiKey(application))
    val geminiApiKey = _geminiApiKey.asStateFlow()

    private val _marketApiKey = MutableStateFlow(SecureStorageManager.getFmpApiKey(application))
    val marketApiKey = _marketApiKey.asStateFlow()

    // Gemini Advice States
    private val _geminiAdvice = MutableStateFlow<String?>(null)
    val geminiAdvice = _geminiAdvice.asStateFlow()

    private val _isGeneratingAdvice = MutableStateFlow(false)
    val isGeneratingAdvice = _isGeneratingAdvice.asStateFlow()

    // Market Refresh State
    private val _isRefreshingMarket = MutableStateFlow(false)
    val isRefreshingMarket = _isRefreshingMarket.asStateFlow()

    init {
        // Retrieve initial advice if present from previous runs (cache in memory)
        _geminiAdvice.value = prefs.getString("cached_advice", null)
    }

    fun saveGeminiApiKey(key: String) {
        SecureStorageManager.setGeminiApiKey(getApplication(), key)
        _geminiApiKey.value = key
    }

    fun saveMarketApiKey(key: String) {
        SecureStorageManager.setFmpApiKey(getApplication(), key)
        _marketApiKey.value = key
    }

    // --- Card Actions ---
    fun addCard(
        name: String,
        issuer: String,
        currentBalance: Double,
        creditLimit: Double,
        apr: Double,
        dueDay: Int,
        nextDueDate: String?,
        minimumPayment: Double,
        cardLastFour: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        db.cardDao().insertCard(
            CreditCard(
                name = name,
                issuer = issuer,
                currentBalance = currentBalance,
                creditLimit = creditLimit,
                apr = apr,
                dueDay = dueDay,
                nextDueDate = nextDueDate,
                minimumPayment = minimumPayment,
                cardLastFour = cardLastFour
            )
        )
    }

    fun deleteCard(card: CreditCard) = viewModelScope.launch(Dispatchers.IO) {
        db.cardDao().deleteCard(card)
    }

    // --- Expense Actions ---
    fun addExpense(
        amount: Double,
        merchant: String,
        category: String,
        cardId: Long?,
        isReimbursableClaim: Boolean = false,
        expenseCategory: String = "Other"
    ) = viewModelScope.launch(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        
        // If linked to a card, increment the card's current balance
        if (cardId != null) {
            db.cardDao().getCardById(cardId)?.let { card ->
                db.cardDao().updateCard(card.copy(currentBalance = card.currentBalance + amount))
            }
        }
        
        db.expenseDao().insertExpense(
            Expense(
                amount = amount,
                merchant = merchant,
                timestamp = timestamp,
                category = category,
                cardId = cardId,
                rawSmsText = null,
                isReimbursableClaim = isReimbursableClaim,
                expenseCategory = expenseCategory
            )
        )
    }

    fun toggleExpenseReimbursable(expense: Expense) = viewModelScope.launch(Dispatchers.IO) {
        db.expenseDao().updateExpense(expense.copy(isReimbursableClaim = !expense.isReimbursableClaim))
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch(Dispatchers.IO) {
        // If linked to a card, deduct the amount from the card balance
        if (expense.cardId != null) {
            db.cardDao().getCardById(expense.cardId)?.let { card ->
                db.cardDao().updateCard(
                    card.copy(currentBalance = (card.currentBalance - expense.amount).coerceAtLeast(0.0))
                )
            }
        }
        db.expenseDao().deleteExpense(expense)
    }

    // --- Investment Actions ---
    fun addInvestment(
        symbol: String,
        name: String,
        quantity: Double,
        purchasePrice: Double,
        currentPrice: Double,
        assetType: String,
        expectedReturnApr: Double,
        brokerName: String = "",
        monthlySipAmount: Double = 0.0
    ) = viewModelScope.launch(Dispatchers.IO) {
        db.investmentDao().insertInvestment(
            Investment(
                symbol = symbol.uppercase(),
                name = name,
                quantity = quantity,
                purchasePrice = purchasePrice,
                currentPrice = currentPrice,
                assetType = assetType,
                expectedReturnApr = expectedReturnApr,
                brokerName = brokerName,
                monthlySipAmount = monthlySipAmount
            )
        )
    }

    fun deleteInvestment(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        db.investmentDao().deleteInvestmentById(id)
    }

    fun updateInvestmentPrice(investment: Investment, newPrice: Double) = viewModelScope.launch(Dispatchers.IO) {
        db.investmentDao().updateInvestment(investment.copy(currentPrice = newPrice))
    }

    // --- Sync Market Prices ---
    fun syncMarketPrices() = viewModelScope.launch(Dispatchers.IO) {
        val currentInvestments = db.investmentDao().getAllInvestments()
        if (currentInvestments.isEmpty()) return@launch

        _isRefreshingMarket.value = true
        
        try {
            db.systemLogDao().insertLog(
                SystemLog(
                    message = "Starting market sync for ${currentInvestments.size} assets...",
                    timestamp = System.currentTimeMillis(),
                    level = "INFO"
                )
            )

            for (investment in currentInvestments) {
                val newPrice = MarketService.fetchLatestPrice(getApplication(), investment.symbol)
                db.investmentDao().updateInvestment(investment.copy(currentPrice = newPrice))
            }

            db.systemLogDao().insertLog(
                SystemLog(
                    message = "Market price synchronization completed successfully.",
                    timestamp = System.currentTimeMillis(),
                    level = "INFO"
                )
            )
        } catch (e: Exception) {
            db.systemLogDao().insertLog(
                SystemLog(
                    message = "Market sync failed: ${e.localizedMessage}",
                    timestamp = System.currentTimeMillis(),
                    level = "ERROR"
                )
            )
        } finally {
            _isRefreshingMarket.value = false
        }
    }

    // --- Gemini Advice Generation ---
    fun generateGeminiAdvice() = viewModelScope.launch(Dispatchers.IO) {
        val activeCards = db.cardDao().getAllCards()
        val activeInvestments = db.investmentDao().getAllInvestments()
        val apiKey = SecureStorageManager.getGeminiApiKey(getApplication())

        if (apiKey.isBlank()) {
            _geminiAdvice.value = "Please configure your Gemini API Key in the Settings page."
            return@launch
        }

        _isGeneratingAdvice.value = true
        
        try {
            db.systemLogDao().insertLog(
                SystemLog(
                    message = "Requesting expert advice from Gemini API...",
                    timestamp = System.currentTimeMillis(),
                    level = "INFO"
                )
            )

            val activeExpenses = db.expenseDao().getAllExpenses()
            val advice = GeminiBrainService.generateDebtAdvice(getApplication(), activeCards, activeInvestments, activeExpenses)
            
            _geminiAdvice.value = advice
            prefs.edit().putString("cached_advice", advice).apply()

            db.systemLogDao().insertLog(
                SystemLog(
                    message = "Received optimized advice from Gemini.",
                    timestamp = System.currentTimeMillis(),
                    level = "INFO"
                )
            )
        } catch (e: Exception) {
            val errorMsg = "Gemini advice failure: ${e.localizedMessage}"
            _geminiAdvice.value = errorMsg
            db.systemLogDao().insertLog(
                SystemLog(
                    message = errorMsg,
                    timestamp = System.currentTimeMillis(),
                    level = "ERROR"
                )
            )
        } finally {
            _isGeneratingAdvice.value = false
        }
    }

    // --- Developer Action: Inject Mock SMS ---
    fun injectMockSms(smsBody: String, sender: String = "MD-BANKIN") = viewModelScope.launch(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        db.systemLogDao().insertLog(
            SystemLog(
                message = "Simulating SMS receive: '$smsBody' from $sender",
                timestamp = timestamp,
                level = "INFO"
            )
        )
        
        try {
            val parsed = SmsParser.parseSms(smsBody)
            if (parsed != null) {
                val existingMatches = db.expenseDao().findMatchingExpenses(parsed.amount, timestamp)
                if (existingMatches.isNotEmpty()) {
                    val existing = existingMatches.first()
                    val newNotes = if (existing.notes.isNullOrEmpty()) {
                        parsed.merchant
                    } else {
                        "${existing.notes}, ${parsed.merchant}"
                    }
                    val updated = existing.copy(
                        status = "Confirmed",
                        notes = newNotes
                    )
                    db.expenseDao().updateExpense(updated)

                    db.systemLogDao().insertLog(
                        SystemLog(
                            message = "Mock SMS Merged: Updated existing transaction ID ${existing.id} to Confirmed and appended merchant '${parsed.merchant}' to notes.",
                            timestamp = timestamp,
                            level = "INFO"
                        )
                    )
                } else {
                    var matchedCardId: Long? = null
                    if (parsed.cardLastFour != null) {
                        val card = db.cardDao().getCardByLastFour(parsed.cardLastFour)
                        if (card != null) {
                            matchedCardId = card.id
                            // Update card balance
                            db.cardDao().updateCard(
                                card.copy(currentBalance = card.currentBalance + parsed.amount)
                            )
                        }
                    }

                    db.expenseDao().insertExpense(
                        Expense(
                            amount = parsed.amount,
                            merchant = parsed.merchant,
                            timestamp = timestamp,
                            category = if (matchedCardId != null) "Auto-Card" else "Auto-SMS",
                            cardId = matchedCardId,
                            rawSmsText = smsBody
                        )
                    )

                    db.systemLogDao().insertLog(
                        SystemLog(
                            message = "Mock SMS Parsed: Logged \$${parsed.amount} at ${parsed.merchant} (Card matching: ${parsed.cardLastFour ?: "None"})",
                            timestamp = timestamp,
                            level = "INFO"
                        )
                    )
                }
            } else {
                db.systemLogDao().insertLog(
                    SystemLog(
                        message = "Mock SMS was ignored (does not appear to be a transaction).",
                        timestamp = timestamp,
                        level = "WARN"
                    )
                )
            }
        } catch (e: Exception) {
            db.systemLogDao().insertLog(
                SystemLog(
                    message = "Failed parsing mock SMS: ${e.localizedMessage}",
                    timestamp = timestamp,
                    level = "ERROR"
                )
            )
        }
    }

    // --- Upstox OAuth & Execution State & Kill Switch ---
    private val _executionState = MutableStateFlow("IDLE")
    val executionState: StateFlow<String> = _executionState.asStateFlow()

    val isPaperTradingActive: StateFlow<Boolean> = UpstoxExecutionService.isPaperTradingActive
    val virtualLedger: StateFlow<VirtualLedger> = UpstoxExecutionService.virtualLedger

    fun setPaperTradingActive(active: Boolean) {
        UpstoxExecutionService.setPaperTradingActive(active)
    }

    fun resetVirtualLedger() {
        UpstoxExecutionService.resetVirtualLedger()
    }

    fun updateVirtualLedgerBalance(context: Context, balance: Double) {
        SecureStorageManager.setVirtualLedgerBalance(context, balance)
        UpstoxExecutionService.setVirtualLedgerBalance(balance)
    }

    private val activeExecutionJobs = java.util.concurrent.CopyOnWriteArrayList<kotlinx.coroutines.Job>()
    private val activeWebSockets = java.util.concurrent.CopyOnWriteArrayList<okhttp3.WebSocket>()

    fun registerExecutionJob(job: kotlinx.coroutines.Job) {
        activeExecutionJobs.add(job)
        job.invokeOnCompletion { activeExecutionJobs.remove(job) }
    }

    fun registerWebSocket(webSocket: okhttp3.WebSocket) {
        activeWebSockets.add(webSocket)
    }

    fun startUpstoxAuthentication(context: Context) {
        // Obsolete external browser launcher. Authentication is now handled in-app via UpstoxAuthScreen route.
    }

    fun handleUpstoxAuthCode(context: Context, code: String) {
        com.debtfreein.app.data.network.UpstoxExecutionService.handleAuthCode(context, code)
    }

    fun syncUpstoxTokenToFirestore(token: String) {
        try {
            FirebaseFirestore.getInstance()
                .collection("system_config")
                .document("upstox_auth")
                .set(
                    mapOf(
                        "access_token" to token,
                        "updated_at" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                )
        } catch (e: Exception) {
            android.util.Log.e("FinanceViewModel", "Failed syncing upstox_auth token: ${e.localizedMessage}")
        }
    }

    sealed interface HoldingsUiState {
        object Loading : HoldingsUiState
        data class Success(val holdings: List<com.debtfreein.app.data.network.UpstoxHolding>) : HoldingsUiState
        data class Error(val message: String) : HoldingsUiState
    }

    private val _holdingsState = MutableStateFlow<HoldingsUiState>(HoldingsUiState.Loading)
    val holdingsState: StateFlow<HoldingsUiState> = _holdingsState.asStateFlow()

    fun fetchHoldings(context: Context) {
        _holdingsState.value = HoldingsUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val holdings = com.debtfreein.app.data.network.UpstoxExecutionService.fetchHoldings(context)
                _holdingsState.value = HoldingsUiState.Success(holdings)
            } catch (e: Exception) {
                val errMsg = e.localizedMessage ?: e.message ?: "Unknown error"
                android.util.Log.e("FinanceViewModel", "Failed to fetch holdings: $errMsg")
                _holdingsState.value = HoldingsUiState.Error(errMsg)
            }
        }
    }

    fun fetchUpstoxHoldings(context: Context) {
        fetchHoldings(context)
    }

    fun emergencyStop() {
        // Cancel all registered active Coroutine Jobs
        activeExecutionJobs.forEach { it.cancel() }
        activeExecutionJobs.clear()

        // Disconnect any active Upstox WebSockets
        activeWebSockets.forEach { ws ->
            try {
                ws.close(1001, "Emergency Stop Triggered")
            } catch (e: Exception) {
                // Ignore close errors
            }
        }
        activeWebSockets.clear()

        // Cancel all child coroutines in viewModelScope to be absolutely thorough
        viewModelScope.coroutineContext[kotlinx.coroutines.Job]?.cancelChildren()

        // Reset execution state to IDLE
        _executionState.value = "IDLE"
    }
}
