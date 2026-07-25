package com.debtfreein.app.data.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class DeepSeekMessage(
    val role: String,
    val content: String
)

data class DeepSeekChatRequest(
    val model: String = "deepseek-chat",
    val messages: List<DeepSeekMessage>,
    val temperature: Double = 0.2
)

data class DeepSeekChoiceMessage(
    val role: String,
    val content: String?
)

data class DeepSeekChoice(
    val message: DeepSeekChoiceMessage
)

data class DeepSeekChatResponse(
    val choices: List<DeepSeekChoice>
)

interface DeepSeekApiService {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: DeepSeekChatRequest
    ): DeepSeekChatResponse
}
