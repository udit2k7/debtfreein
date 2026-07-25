package com.debtfreein.app.data.ai

import android.content.Context
import com.debtfreein.app.data.network.GroqApiService
import com.debtfreein.app.data.network.GroqChatRequest
import com.debtfreein.app.data.network.GroqMessage
import com.debtfreein.app.data.network.DeepSeekApiService
import com.debtfreein.app.data.network.DeepSeekChatRequest
import com.debtfreein.app.data.network.DeepSeekMessage
import com.debtfreein.app.data.security.SecureStorageManager
import com.debtfreein.app.data.logging.FileLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GroqScannerService {
    private const val GROQ_BASE_URL = "https://api.groq.com/"
    private const val DEEPSEEK_BASE_URL = "https://api.deepseek.com/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            val isDeepSeek = request.url.host.contains("deepseek")
            val response = try {
                chain.proceed(request)
            } catch (e: Exception) {
                if (isDeepSeek) {
                    FileLogger.log("DEEPSEEK_HTTP", "DeepSeek API HTTP Exception / Potential 502: ${e.message}")
                }
                throw e
            }
            if (isDeepSeek) {
                FileLogger.log("DEEPSEEK_HTTP", "DeepSeek API Response HTTP Code: ${response.code}")
            }
            response
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val groqApi: GroqApiService by lazy {
        Retrofit.Builder()
            .baseUrl(GROQ_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroqApiService::class.java)
    }

    private val deepseekApi: DeepSeekApiService by lazy {
        Retrofit.Builder()
            .baseUrl(DEEPSEEK_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeepSeekApiService::class.java)
    }

    suspend fun scanMarketForSetups(context: Context, marketData: String): String = withContext(Dispatchers.IO) {
        val activeEngine = SecureStorageManager.getActiveMakerEngine(context)
        val prompt = """
            You are a ruthless quantitative algorithmic execution engine. Analyze this OHLCV, RSI, MACD, and Volume profile. Seek only high-probability setups with strong volume confirmation. Ignore choppy consolidation. Calculate a strict Risk/Reward ratio of at least 1:2. Output strict JSON: {"action": "BUY" | "SELL" | "NONE", "entry": price, "stopLoss": price, "takeProfit": price, "convictionScore": 1-100}. Execute only if conviction > 88.
            
            Market Data:
            $marketData
        """.trimIndent()

        if (activeEngine == "DeepSeek V4-Pro") {
            val apiKey = SecureStorageManager.getDeepSeekApiKey(context)
            if (apiKey.isBlank()) {
                FileLogger.log("DEEPSEEK_HTTP", "DeepSeek API key is blank. Aborting request.", context)
                return@withContext """{"action": "NONE", "entry": 0.0, "stopLoss": 0.0, "takeProfit": 0.0, "convictionScore": 0}"""
            }
            val authHeader = "Bearer $apiKey"
            val request = DeepSeekChatRequest(
                messages = listOf(
                    DeepSeekMessage(role = "user", content = prompt)
                )
            )
            try {
                val response = deepseekApi.getChatCompletion(authHeader, request)
                val result = response.choices.firstOrNull()?.message?.content ?: """{"action": "NONE"}"""
                FileLogger.log("DEEPSEEK_HTTP", "DeepSeek API HTTP 200 OK | Content length: ${result.length}", context)
                
                val inputTokens = (prompt.length / 4L).coerceAtLeast(1L)
                val outputTokens = (result.length / 4L).coerceAtLeast(1L)
                ApiBudgetManager.logSpend(context, "deepseek-v4-pro", inputTokens, outputTokens)
                
                result
            } catch (e: HttpException) {
                val code = e.code()
                FileLogger.log("DEEPSEEK_HTTP", "DeepSeek API HTTP Code: $code (${e.message()})", context)
                """{"action": "NONE", "entry": 0.0, "stopLoss": 0.0, "takeProfit": 0.0, "convictionScore": 0}"""
            } catch (e: Exception) {
                FileLogger.log("DEEPSEEK_HTTP", "DeepSeek API HTTP Code (Error/502): ${e.message}", context)
                """{"action": "NONE", "entry": 0.0, "stopLoss": 0.0, "takeProfit": 0.0, "convictionScore": 0}"""
            }
        } else {
            val apiKey = SecureStorageManager.getGroqApiKey(context)
            if (apiKey.isBlank()) {
                return@withContext """{"action": "NONE", "entry": 0.0, "stopLoss": 0.0, "takeProfit": 0.0, "convictionScore": 0}"""
            }
            val authHeader = "Bearer $apiKey"
            val request = GroqChatRequest(
                messages = listOf(
                    GroqMessage(role = "user", content = prompt)
                )
            )
            try {
                val response = groqApi.getChatCompletion(authHeader, request)
                val result = response.choices.firstOrNull()?.message?.content ?: """{"action": "NONE"}"""
                
                val inputTokens = (prompt.length / 4L).coerceAtLeast(1L)
                val outputTokens = (result.length / 4L).coerceAtLeast(1L)
                ApiBudgetManager.logSpend(context, "llama3-70b-8192", inputTokens, outputTokens)
                
                result
            } catch (e: Exception) {
                e.printStackTrace()
                """{"action": "NONE", "entry": 0.0, "stopLoss": 0.0, "takeProfit": 0.0, "convictionScore": 0}"""
            }
        }
    }
}
