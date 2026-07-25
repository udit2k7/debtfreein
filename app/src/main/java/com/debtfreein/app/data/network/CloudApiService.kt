package com.debtfreein.app.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class CloudStatusResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("botMicroState", alternate = ["bot_micro_state"]) val botMicroState: String = "",
    @SerializedName("lastScanUtc", alternate = ["last_scan_utc"]) val lastScanUtc: String = "",
    @SerializedName("isPaperTradingActive", alternate = ["is_paper_trading_active"]) val isPaperTradingActive: Boolean = false,
    @SerializedName("isAiScanningActive", alternate = ["is_ai_scanning_active"]) val isAiScanningActive: Boolean = false
)

interface CloudApiService {
    @GET("https://udit2k7--debtfreein-quant-engine-get-live-bot-status.modal.run")
    suspend fun getLiveBotStatus(): Response<CloudStatusResponse>
}

object CloudApiClient {
    private const val BASE_URL = "https://udit2k7--debtfreein-quant-engine-get-live-bot-status.modal.run/"

    val service: CloudApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudApiService::class.java)
    }
}
