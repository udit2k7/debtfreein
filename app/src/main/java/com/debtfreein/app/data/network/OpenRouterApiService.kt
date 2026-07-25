package com.debtfreein.app.data.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class OpenRouterMessage(
    val role: String,
    val content: String
)

data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    val temperature: Double = 0.2
)

data class OpenRouterMessageContent(
    val content: String?
)

data class OpenRouterChoice(
    val message: OpenRouterMessageContent?
)

data class OpenRouterResponse(
    val choices: List<OpenRouterChoice>?
)

interface OpenRouterApiService {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://debtfreein.app",
        @Header("X-Title") title: String = "DebtFreeIn AI Quant",
        @Body request: OpenRouterRequest
    ): Response<OpenRouterResponse>
}

object OpenRouterClient {
    private const val BASE_URL = "https://openrouter.ai/api/"

    val service: OpenRouterApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenRouterApiService::class.java)
    }
}
