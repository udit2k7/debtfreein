package com.debtfreein.app.data.ai

import android.content.Context
import com.debtfreein.app.data.security.SecureStorageManager
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONObject
import com.debtfreein.app.data.model.CreditCard
import com.debtfreein.app.data.model.Investment
import com.debtfreein.app.data.model.Expense
import com.debtfreein.app.data.repository.TradePostMortem
import com.debtfreein.app.data.repository.TradePostMortemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Kotlin Data Models representing the structured JSON advice
data class GeminiAdviceReport(
    val summary: String,
    val liquidations: List<LiquidationAdvice>,
    val personalLoan: PersonalLoanAdvice,
    val repaymentPlan: List<RepaymentPlanItem>,
    val reimbursableReceivables: List<ReimbursableReceivableAdvice>?,
    val sipAnalysis: SipAnalysisAdvice?
)

data class LiquidationAdvice(
    val assetSymbol: String,
    val quantityToSell: Double,
    val estimatedValue: Double,
    val targetCardName: String,
    val reason: String
)

data class PersonalLoanAdvice(
    val makesSense: Boolean,
    val recommendedAmount: Double,
    val targetCardNames: List<String>,
    val maxViableApr: Double,
    val interestSavings: Double,
    val explanation: String
)

data class RepaymentPlanItem(
    val cardName: String,
    val priority: Int,
    val recommendedMonthlyPayment: Double,
    val strategy: String
)

data class ReimbursableReceivableAdvice(
    val cardName: String,
    val merchant: String,
    val amount: Double,
    val interestAccrued30Days: Double,
    val employerClaimRecommendation: String
)

data class SipAnalysisAdvice(
    val currentExposure: String,
    val rebalancingRecommendation: String,
    val wealthGenerationAdvice: String
)

object GeminiBrainService {
    private var tradePostMortemRepository: TradePostMortemRepository = TradePostMortemRepository()

    suspend fun generateDebtAdvice(
        context: Context,
        cards: List<CreditCard>,
        investments: List<Investment>,
        expenses: List<Expense>
    ): String = withContext(Dispatchers.IO) {
        val finalKey = SecureStorageManager.getGeminiApiKey(context)

        if (finalKey.isBlank() || finalKey == "YOUR_KEY_HERE") {
            return@withContext "Please enter a valid Gemini API Key in the Settings to activate the Expert Brain."
        }

        if (cards.isEmpty()) {
            return@withContext "Congratulations! You have no outstanding credit card debt. The Gemini Expert Brain has nothing to optimize."
        }

        // Define the JSON schema for structured responses
        val responseSchema = Schema.obj(
            "response",
            "Structured advisory report including paydown strategies, asset liquidations, personal loan viability, reimbursable claim tracking, and SIP analysis. All currency calculations and text figures must strictly represent Indian Rupees (INR/Rs./₹), with USD/$ explicitly forbidden.",
            Schema.str(
                "summary",
                "Executive summary of the recommendations. Must describe all monetary calculations in Indian Rupees (INR) using Rs. or ₹, never USD or $."
            ),
            Schema.arr(
                "liquidations",
                "Assets to sell to pay off card balances.",
                Schema.obj(
                    "liquidationItem",
                    "A single asset liquidation plan.",
                    Schema.str("assetSymbol", "Symbol of the holding, e.g. RELIANCE"),
                    Schema.double("quantityToSell", "Amount of holding to liquidate."),
                    Schema.double("estimatedValue", "Expected cash generated in Indian Rupees (INR) value."),
                    Schema.str("targetCardName", "Credit card to pay off using the cash."),
                    Schema.str("reason", "Description of why selling this asset makes financial sense. Text descriptions must use INR/Rs. and never USD/$ symbols.")
                )
            ),
            Schema.obj(
                "personalLoan",
                "Personal loan consolidation options.",
                Schema.bool("makesSense", "True if consolidation is mathematically optimal."),
                Schema.double("recommendedAmount", "Recommended consolidation loan principal in Indian Rupees (INR) value."),
                Schema.arr(
                    "targetCardNames",
                    "List of credit card names to pay off using the loan.",
                    Schema.str("cardName", "Name of the card.")
                ),
                Schema.double("maxViableApr", "Maximum viable loan APR (%) to accept."),
                Schema.double("interestSavings", "Expected interest savings in Indian Rupees (INR) over 12 months."),
                Schema.str("explanation", "Detailed explanation of interest rates comparison. Text descriptions must use INR/Rs. and never USD/$ symbols.")
            ),
            Schema.arr(
                "repaymentPlan",
                "Prioritized repayment schedule.",
                Schema.obj(
                    "repaymentPlanItem",
                    "Card payoff contribution details.",
                    Schema.str("cardName", "Name of the card."),
                    Schema.int("priority", "Payment order priority (1 is highest)."),
                    Schema.double("recommendedMonthlyPayment", "Extra monthly payment towards this card in Indian Rupees (INR) value."),
                    Schema.str("strategy", "Paydown strategy used, e.g. Avalanche")
                )
            ),
            Schema.arr(
                "reimbursableReceivables",
                "Reimbursable claims linked to credit card balances. These must NOT be paid via liquidations, but tracked for employer reimbursement.",
                Schema.obj(
                    "reimbursableItem",
                    "A single reimbursable receivable claim.",
                    Schema.str("cardName", "Name of the credit card holding the debt."),
                    Schema.str("merchant", "Merchant for the reimbursable transaction, e.g. Starbucks, Office Depot."),
                    Schema.double("amount", "Reimbursable amount in Indian Rupees (INR)."),
                    Schema.double("interestAccrued30Days", "Calculated interest accruing over a 30-day window: amount * (Card APR / 1200) in INR."),
                    Schema.str("employerClaimRecommendation", "Advisory note for claiming both principal and interest accrued from the employer.")
                )
            ),
            Schema.obj(
                "sipAnalysis",
                "Analysis of mutual fund SIPs and equity holdings.",
                Schema.str("currentExposure", "Summary of current sector exposures across UTI, JioBlackRock, Axis SIPs, and Upstox equities."),
                Schema.str("rebalancingRecommendation", "Rebalancing and diversification advice to avoid over-exposure to a single sector."),
                Schema.str("wealthGenerationAdvice", "Strategy to build long-term debt-free wealth.")
            )
        )

        // Setup config for JSON Response Mode
        val config = generationConfig {
            responseMimeType = "application/json"
            this.responseSchema = responseSchema
        }

        val model = GenerativeModel(
            modelName = "gemini-2.5-pro",
            apiKey = finalKey,
            generationConfig = config
        )

        val cardsData = cards.joinToString("\n") { card ->
            "- Card: ${card.name} (${card.issuer}) | Balance: Rs.${card.currentBalance} / Limit: Rs.${card.creditLimit} | APR: ${card.apr}% | Due Day: ${card.dueDay} | Min Payment: Rs.${card.minimumPayment}"
        }

        val investmentsData = if (investments.isEmpty()) {
            "No active investments recorded."
        } else {
            investments.joinToString("\n") { inv ->
                val cost = inv.purchasePrice * inv.quantity
                val currentVal = inv.currentPrice * inv.quantity
                val profitLoss = currentVal - cost
                "- Asset: ${inv.symbol} (${inv.name}) | Type: ${inv.assetType} | Qty: ${inv.quantity} | Current Value: Rs.${currentVal} | Expected Return: ${inv.expectedReturnApr}% | Broker: ${inv.brokerName} | Monthly SIP: Rs.${inv.monthlySipAmount} | PnL: Rs.${profitLoss}"
            }
        }

        val expensesData = if (expenses.isEmpty()) {
            "No active expenses recorded."
        } else {
            expenses.joinToString("\n") { exp ->
                "- Expense: Rs.${exp.amount} at ${exp.merchant} | Cat: ${exp.category} | Reimbursable: ${exp.isReimbursableClaim} | Detail Cat: ${exp.expenseCategory}"
            }
        }

        val recentLosses = try {
            tradePostMortemRepository.getRecentLosses(5)
        } catch (e: Exception) {
            emptyList()
        }

        val systemMemoryBlock = if (recentLosses.isNotEmpty()) {
            val mistakes = recentLosses.joinToString("\n") { pm ->
                "- Trade ${pm.tradeId} (${pm.netProfit} PnL): Mistakes: ${pm.mistakesMade} | Lessons: ${pm.lessonsLearned}"
            }
            "SYSTEM MEMORY - PREVIOUS FAILURES TO AVOID:\n$mistakes\n\n"
        } else {
            ""
        }

        val prompt = """
            ${systemMemoryBlock}You must review the previous technical analysis failures logged in the SYSTEM MEMORY. Do not suggest a trade if the current market setup mirrors these past failure patterns.
            
            You are the Expert Financial Brain of the DebtFreeIn app. Your sole purpose is to make the user debt-free as fast as possible using rigorous mathematical analysis. All monetary calculations must be calculated and displayed in Indian Rupees (INR).
            
            Here is the user's local financial data (in INR/Rs.):
            
            CREDIT CARD LIABILITIES:
            $cardsData
            
            INVESTMENT PORTFOLIO (Equities, SIPs, MCX Commodities):
            $investmentsData
            
            RECENT EXPENSES & TRANSACTIONS:
            $expensesData
            
            Perform a mathematical comparison of liabilities vs assets, and provide a clear, step-by-step action plan in strict JSON.
            
            CRITICAL SYSTEM RULES & LOGIC CONSTRAINTS:
            1. MARKET SCOPE: You are strictly an Indian financial expert. Analyze NSE/BSE equities, Mutual Fund SIPs, and MCX commodities. EXPLICITLY IGNORE and reject any cryptocurrency or foreign trade data. If any such assets are present, ignore them or mark them as invalid.
            2. REIMBURSABLE CLAIMS LOGIC: If a transaction or credit card expense is tagged with 'isReimbursableClaim' = true (e.g. Office Expense), treat it as an 'Account Receivable'.
               - DO NOT recommend liquidating the user's SIPs (UTI, JioBlackRock, Axis) or Upstox equities to pay this specific debt.
               - Instead, calculate the exact interest that will accrue on that card during a standard 30-day reimbursement window (Interest Accrued = Reimbursable Amount * (Card APR / 100) / 12) so the user can claim this accrued interest from their employer. Detail this in the 'reimbursableReceivables' object.
            3. FIXED VS VARIABLE: Treat 'School Fees' as an untouchable fixed liability. Do not suggest cutting or reducing it.
            4. WEALTH GENERATION & REBALANCING: Evaluate the user's UTI, JioBlackRock, and Axis SIPs alongside their Upstox equity holdings. Suggest rebalancing or diversification advice inside 'sipAnalysis' if they are over-exposed to one sector, keeping the ultimate goal of long-term debt-free wealth generation.
            5. CRITICAL CURRENCY INSTRUCTION:
               - All monetary properties (e.g. estimatedValue, recommendedAmount, interestSavings, recommendedMonthlyPayment, interestAccrued30Days, amount) must represent values in Indian Rupees (INR).
               - For textual fields such as 'summary' and 'explanation' in the output JSON, you MUST calculate and present all values strictly in Indian Rupees format (indicated by 'Rs.' or '₹').
               - You are EXPLICITLY FORBIDDEN from defaulting to USD or using the '$' symbol anywhere in the output. Any reference to dollars or the '$' symbol will break the app parser. Use 'Rs.' or '₹' exclusively.
            
            Your analysis MUST address:
            1. **Asset Liquidation Decision**: Compare expected returns of NSE/BSE investments against the card APRs. Recommend which equities or commodities (if any) to liquidate to pay down high-interest cards, keeping SIPs and reimbursables untouched.
            2. **Personal Loan Consolidation Analysis**: Calculate if a consolidation personal loan at 11-13% APR is optimal. Compute how much interest they will save in INR.
            3. **Prioritized Payoff Plan**: Create the payoff list using the Avalanche method (highest APR first).
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            val result = response.text ?: "{}"
            logGeminiSpend(context, prompt, result)
            result
        } catch (e: Exception) {
            "Error running Gemini analysis: ${e.localizedMessage}\n\nPlease check your internet connection and API key configuration."
        }
    }

    suspend fun generateTradePostMortem(
        context: Context,
        tradeDetails: String,
        netProfit: Double
    ): TradePostMortem = withContext(Dispatchers.IO) {
        val finalKey = SecureStorageManager.getGeminiApiKey(context)

        if (finalKey.isBlank() || finalKey == "YOUR_KEY_HERE") {
            return@withContext TradePostMortem(
                tradeId = "PM_${System.currentTimeMillis()}",
                isPaperTrade = true,
                rationale = "No valid Gemini API key configured.",
                netProfit = netProfit,
                mistakesMade = "API key configuration missing.",
                lessonsLearned = "Configure Gemini API key."
            )
        }

        val responseSchema = Schema.obj(
            "postMortem",
            "Technical analysis post-mortem report for a completed trade.",
            Schema.str("rationale", "Detailed technical explanation of the trade entry/exit setup."),
            Schema.str("mistakesMade", "Specific indicator failures, volume anomalies, or structural mistakes made during execution."),
            Schema.str("lessonsLearned", "Actionable rule or lesson learned to prevent repeating this failure.")
        )

        val config = generationConfig {
            responseMimeType = "application/json"
            this.responseSchema = responseSchema
        }

        val model = GenerativeModel(
            modelName = "gemini-2.5-pro",
            apiKey = finalKey,
            generationConfig = config
        )

        val prompt = """
            Analyze this closed trade. Identify technical failures. Did the setup ignore volume divergence? Was the stop-loss too tight? Output JSON: {"rationale": "why it happened", "mistakesMade": "specific technical error", "lessonsLearned": "rule for next time"}.
            
            Trade Details:
            $tradeDetails
            
            Net Profit/Loss:
            Rs. $netProfit
        """.trimIndent()

        val responseText = try {
            val res = model.generateContent(prompt).text ?: "{}"
            logGeminiSpend(context, prompt, res)
            res
        } catch (e: Exception) {
            "{}"
        }

        val json = try {
            JSONObject(responseText)
        } catch (e: Exception) {
            JSONObject()
        }

        val rationale = json.optString("rationale", "No technical analysis rationale provided.")
        val mistakesMade = json.optString("mistakesMade", "No specific indicator failures identified.")
        val lessonsLearned = json.optString("lessonsLearned", "No lessons logged.")

        val postMortem = TradePostMortem(
            tradeId = "PM_${System.currentTimeMillis()}",
            isPaperTrade = true,
            rationale = rationale,
            netProfit = netProfit,
            mistakesMade = mistakesMade,
            lessonsLearned = lessonsLearned
        )

        try {
            tradePostMortemRepository.savePostMortem(postMortem)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        postMortem
    }

    suspend fun generateTradingSignal(
        context: Context,
        marketData: String
    ): String = withContext(Dispatchers.IO) {
        val finalKey = SecureStorageManager.getGeminiApiKey(context)

        if (finalKey.isBlank() || finalKey == "YOUR_KEY_HERE") {
            return@withContext """{"action": "HOLD", "confidenceScore": 0, "rationale": "No valid Gemini API key."}"""
        }

        val recentLosses = try {
            tradePostMortemRepository.getRecentLosses(5)
        } catch (e: Exception) {
            emptyList()
        }

        val systemMemoryBlock = if (recentLosses.isNotEmpty()) {
            val mistakes = recentLosses.joinToString("\n") { pm ->
                "- Trade ${pm.tradeId} (${pm.netProfit} PnL): Mistakes: ${pm.mistakesMade} | Lessons: ${pm.lessonsLearned}"
            }
            "SYSTEM MEMORY - PREVIOUS FAILURES TO AVOID:\n$mistakes\n\n"
        } else {
            ""
        }

        val responseSchema = Schema.obj(
            "signal",
            "Trading signal response",
            Schema.str("action", "BUY, SELL, or HOLD"),
            Schema.int("confidenceScore", "Confidence score from 1-100"),
            Schema.str("rationale", "Technical justification"),
            Schema.double("stopLoss", "Recommended stop loss value"),
            Schema.double("takeProfit", "Recommended take profit value")
        )

        val config = generationConfig {
            responseMimeType = "application/json"
            this.responseSchema = responseSchema
        }

        val model = GenerativeModel(
            modelName = "gemini-2.5-pro",
            apiKey = finalKey,
            generationConfig = config
        )

        val prompt = """
            You are an institutional algorithmic trading engine. Analyze the provided OHLCV data, RSI, MACD, and Volume profiles. You must strictly avoid the failure patterns listed in the SYSTEM MEMORY below. Output your decision strictly as a JSON object containing {"action": "BUY" | "SELL" | "HOLD", "confidenceScore": 1-100, "rationale": "technical justification", "stopLoss": value, "takeProfit": value}. Do not execute if confidence is below 85.
            
            SYSTEM MEMORY:
            $systemMemoryBlock
            
            Market Data:
            $marketData
        """.trimIndent()

        try {
            val res = model.generateContent(prompt).text ?: "{}"
            logGeminiSpend(context, prompt, res)
            res
        } catch (e: Exception) {
            """{"action": "HOLD", "confidenceScore": 0, "rationale": "Gemini API call failed."}"""
        }
    }

    suspend fun evaluateExitSignal(
        context: Context,
        positionDetails: String,
        currentMarketData: String
    ): String = withContext(Dispatchers.IO) {
        val finalKey = SecureStorageManager.getGeminiApiKey(context)

        if (finalKey.isBlank() || finalKey == "YOUR_KEY_HERE") {
            return@withContext """{"action": "HOLD", "rationale": "No API key."}"""
        }

        val responseSchema = Schema.obj(
            "exitSignal",
            "Exit evaluation for a trading position",
            Schema.str("action", "CLOSE or HOLD"),
            Schema.str("rationale", "Exit evaluation rationale")
        )

        val config = generationConfig {
            responseMimeType = "application/json"
            this.responseSchema = responseSchema
        }

        val model = GenerativeModel(
            modelName = "gemini-2.5-pro",
            apiKey = finalKey,
            generationConfig = config
        )

        val prompt = """
            You are an institutional algorithmic trading engine. Evaluate if the following open position should be closed or held based on the current market data.
            
            Open Position Details:
            $positionDetails
            
            Current Market Data:
            $currentMarketData
            
            Output JSON: {"action": "CLOSE" | "HOLD", "rationale": "Exit evaluation rationale"}
        """.trimIndent()

        try {
            val res = model.generateContent(prompt).text ?: "{}"
            logGeminiSpend(context, prompt, res)
            res
        } catch (e: Exception) {
            """{"action": "HOLD", "rationale": "Gemini API call failed."}"""
        }
    }

    suspend fun validateTradeProposal(
        context: Context,
        groqProposal: String,
        firebaseMemory: String
    ): String = withContext(Dispatchers.IO) {
        val finalKey = SecureStorageManager.getGeminiApiKey(context)

        if (finalKey.isBlank() || finalKey == "YOUR_KEY_HERE") {
            return@withContext """{"decision": "VETO", "riskAnalysis": "No valid Gemini API key configured."}"""
        }

        val responseSchema = Schema.obj(
            "validation",
            "Chief Risk Officer evaluation of a proposed trade.",
            Schema.str("decision", "APPROVE or VETO"),
            Schema.str("riskAnalysis", "Detailed risk analysis justifying the approval or veto.")
        )

        val config = generationConfig {
            responseMimeType = "application/json"
            this.responseSchema = responseSchema
        }

        val model = GenerativeModel(
            modelName = "gemini-2.5-pro",
            apiKey = finalKey,
            generationConfig = config
        )

        val prompt = """
            You are the Chief Risk Officer. The Groq quantitative scanner has proposed the following trade:
            $groqProposal
            
            Review this against our SYSTEM MEMORY of past failures:
            $firebaseMemory
            
            Your job is to protect capital. Does this setup mirror past mistakes? Is the risk/reward justified? Output strict JSON: {"decision": "APPROVE" | "VETO", "riskAnalysis": "rationale for approval or veto"}.
        """.trimIndent()

        try {
            val res = model.generateContent(prompt).text ?: "{}"
            logGeminiSpend(context, prompt, res)
            res
        } catch (e: Exception) {
            """{"decision": "VETO", "riskAnalysis": "Gemini validation failed to execute: ${e.localizedMessage}"}"""
        }
    }

    suspend fun runEndOfDayReview(
        context: Context,
        ledgerState: String,
        remainingApiBudget: Double
    ): String = withContext(Dispatchers.IO) {
        val finalKey = SecureStorageManager.getGeminiApiKey(context)

        if (finalKey.isBlank() || finalKey == "YOUR_KEY_HERE") {
            return@withContext """{"dailyPnL": 0.0, "criticalMistakes": "No valid Gemini API key configured.", "budgetRecommendation": "Configure Gemini API key."}"""
        }

        val responseSchema = Schema.obj(
            "endOfDayReview",
            "CIO End of Day portfolio review",
            Schema.double("dailyPnL", "Net daily profit or loss in INR"),
            Schema.str("criticalMistakes", "Pattern to avoid tomorrow based on executions"),
            Schema.str("budgetRecommendation", "Advice on increasing/decreasing API budget to maximize breakout capture")
        )

        val config = generationConfig {
            responseMimeType = "application/json"
            this.responseSchema = responseSchema
        }

        val model = GenerativeModel(
            modelName = "gemini-2.5-pro",
            apiKey = finalKey,
            generationConfig = config
        )

        val prompt = """
            You are the Chief Investment Officer. Analyze today's trading ledger and the remaining API budget. Output a JSON payload containing: {"dailyPnL": value, "criticalMistakes": "pattern to avoid tomorrow", "budgetRecommendation": "advice on increasing/decreasing API budget to maximize breakout capture"}.
            
            Ledger State:
            $ledgerState
            
            Remaining API Budget:
            Rs. $remainingApiBudget
        """.trimIndent()

        try {
            val res = model.generateContent(prompt).text ?: "{}"
            logGeminiSpend(context, prompt, res)
            res
        } catch (e: Exception) {
            """{"dailyPnL": 0.0, "criticalMistakes": "Failed EOD execution: ${e.localizedMessage}", "budgetRecommendation": "N/A"}"""
        }
    }

    private suspend fun logGeminiSpend(context: Context, prompt: String, result: String) {
        try {
            val inputTokens = (prompt.length / 4L).coerceAtLeast(1L)
            val outputTokens = (result.length / 4L).coerceAtLeast(1L)
            ApiBudgetManager.logSpend(context, "gemini-2.5-pro", inputTokens, outputTokens)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

