package com.debtfreein.app.data.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqResponseFormat(
    val type: String = "json_object"
)

data class GroqChatRequest(
    val model: String = "llama3-70b-8192",
    val messages: List<GroqMessage>,
    val response_format: GroqResponseFormat? = GroqResponseFormat()
)

data class GroqChoiceMessage(
    val role: String,
    val content: String?
)

data class GroqChoice(
    val message: GroqChoiceMessage
)

data class GroqChatResponse(
    val choices: List<GroqChoice>
)

interface GroqApiService {
    @POST("openai/v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: GroqChatRequest
    ): GroqChatResponse
}
