package com.debtfreein.app.data.security

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "token_manager_prefs"
    
    private const val KEY_UPSTOX_API_KEY = "upstox_api_key"
    private const val KEY_UPSTOX_API_SECRET = "upstox_api_secret"
    private const val KEY_UPSTOX_REDIRECT_URI = "upstox_redirect_uri"
    private const val KEY_UPSTOX_ACCESS_TOKEN = "upstox_access_token"
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"
    private const val KEY_GROQ_API_KEY = "groq_api_key"
    private const val KEY_OPENROUTER_API_KEY = "openrouter_api_key"
    private const val KEY_ACTIVE_AI_MODEL = "active_ai_model"

    private var appContext: Context? = null

    private fun getPrefs(): SharedPreferences {
        val ctx = appContext ?: throw IllegalStateException("TokenManager not initialized")
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var upstoxApiKey: String = ""
        private set
    var upstoxApiSecret: String = ""
        private set
    var upstoxRedirectUri: String = "https://127.0.0.1"
        private set
    var upstoxAccessToken: String = ""
        private set
    var geminiApiKey: String = ""
        private set
    var groqApiKey: String = ""
        private set
    var openRouterApiKey: String = ""
        private set
    var activeAiModel: String = "deepseek/deepseek-r1"
        private set

    fun initialize(context: Context) {
        appContext = context.applicationContext
        val prefs = getPrefs()
        upstoxApiKey = prefs.getString(KEY_UPSTOX_API_KEY, "") ?: ""
        upstoxApiSecret = prefs.getString(KEY_UPSTOX_API_SECRET, "") ?: ""
        upstoxRedirectUri = prefs.getString(KEY_UPSTOX_REDIRECT_URI, "https://127.0.0.1") ?: "https://127.0.0.1"
        upstoxAccessToken = prefs.getString(KEY_UPSTOX_ACCESS_TOKEN, "") ?: ""
        geminiApiKey = prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
        groqApiKey = prefs.getString(KEY_GROQ_API_KEY, "") ?: ""
        openRouterApiKey = prefs.getString(KEY_OPENROUTER_API_KEY, "") ?: ""
        activeAiModel = prefs.getString(KEY_ACTIVE_AI_MODEL, "deepseek/deepseek-r1") ?: "deepseek/deepseek-r1"
    }

    fun saveOpenRouterConfig(apiKey: String, model: String) {
        openRouterApiKey = apiKey
        activeAiModel = model
        getPrefs().edit().apply {
            putString(KEY_OPENROUTER_API_KEY, apiKey)
            putString(KEY_ACTIVE_AI_MODEL, model)
            apply()
        }
    }

    fun saveCredentials(
        upstoxKey: String,
        upstoxSecret: String,
        upstoxRedirect: String,
        geminiKey: String,
        groqKey: String
    ) {
        upstoxApiKey = upstoxKey
        upstoxApiSecret = upstoxSecret
        upstoxRedirectUri = upstoxRedirect
        geminiApiKey = geminiKey
        groqApiKey = groqKey

        getPrefs().edit().apply {
            putString(KEY_UPSTOX_API_KEY, upstoxKey)
            putString(KEY_UPSTOX_API_SECRET, upstoxSecret)
            putString(KEY_UPSTOX_REDIRECT_URI, upstoxRedirect)
            putString(KEY_GEMINI_API_KEY, geminiKey)
            putString(KEY_GROQ_API_KEY, groqKey)
            apply()
        }
    }

    fun saveAccessToken(token: String) {
        upstoxAccessToken = token
        getPrefs().edit().putString(KEY_UPSTOX_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? {
        return upstoxAccessToken.ifBlank { null }
    }

    fun isTokenValid(): Boolean {
        return upstoxAccessToken.isNotBlank() && upstoxAccessToken != "UPSTOX_TOKEN_null"
    }

    fun logoutAndClearTokens(context: Context) {
        upstoxAccessToken = ""
        upstoxApiKey = ""
        upstoxApiSecret = ""
        getPrefs().edit().apply {
            remove(KEY_UPSTOX_ACCESS_TOKEN)
            remove(KEY_UPSTOX_API_KEY)
            remove(KEY_UPSTOX_API_SECRET)
            apply()
        }
        SecureStorageManager.logoutAndClearTokens(context)
    }
}
