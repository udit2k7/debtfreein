package com.debtfreein.app.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Manages encrypted storage of API credentials and keys natively using EncryptedSharedPreferences.
 * Gemini, DeepSeek, Groq, Upstox, FMP keys and budget limits are migrated to Cloud Firestore.
 */
object SecureStorageManager {
    private const val PREFS_FILE = "secure_settings_prefs"
    
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"
    private const val KEY_FMP_API_KEY = "fmp_api_key"
    private const val KEY_UPSTOX_API_KEY = "upstox_api_key"
    private const val KEY_UPSTOX_API_SECRET = "upstox_api_secret"
    private const val KEY_ACTIVE_MAKER_ENGINE = "active_maker_engine"

    @Volatile
    private var cachedGeminiApiKey: String? = null
    @Volatile
    private var cachedGroqApiKey: String? = null
    @Volatile
    private var cachedDeepSeekApiKey: String? = null
    @Volatile
    private var cachedMonthlyBudget: Double? = null
    @Volatile
    private var cachedFmpApiKey: String? = null
    @Volatile
    private var cachedUpstoxApiKey: String? = null
    @Volatile
    private var cachedUpstoxApiSecret: String? = null
    @Volatile
    private var cachedVirtualLedgerBalance: Double? = null

    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        return try {
            createPrefsInstance(context)
        } catch (e: Exception) {
            // Handle Keystore corruption or decryption exception
            try {
                // 1. Delete Keystore alias to clear corrupted master key
                val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
                val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                if (keyStore.containsAlias(masterKeyAlias)) {
                    keyStore.deleteEntry(masterKeyAlias)
                }

                // 2. Delete the SharedPreferences file
                context.deleteSharedPreferences(PREFS_FILE)

                // 3. Retry preferences initialization
                createPrefsInstance(context)
            } catch (retryException: Exception) {
                // Final fallback to unencrypted SharedPreferences if keystore is completely broken
                context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
            }
        }
    }

    private fun createPrefsInstance(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREFS_FILE,
            masterKeyAlias,
            context.applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getGeminiApiKey(context: Context): String {
        return cachedGeminiApiKey ?: ""
    }

    fun setGeminiApiKey(context: Context, key: String) {
        cachedGeminiApiKey = key
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            val data = mapOf("gemini_api_key" to key)
            db.collection("users").document(uid).collection("config").document("secure_keys")
                .set(data, SetOptions.merge())
        }
    }

    fun getFmpApiKey(context: Context): String {
        return cachedFmpApiKey ?: ""
    }

    fun setFmpApiKey(context: Context, key: String) {
        cachedFmpApiKey = key
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            val data = mapOf("fmp_api_key" to key)
            db.collection("users").document(uid).collection("config").document("secure_keys")
                .set(data, SetOptions.merge())
        }
    }

    fun getUpstoxApiKey(context: Context): String {
        return cachedUpstoxApiKey ?: ""
    }

    fun setUpstoxApiKey(context: Context, key: String) {
        cachedUpstoxApiKey = key
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            val data = mapOf("upstox_api_key_client" to key)
            db.collection("users").document(uid).collection("config").document("secure_keys")
                .set(data, SetOptions.merge())
        }
    }

    @Volatile
    private var cachedUpstoxAccessToken: String? = null

    fun getUpstoxAccessToken(context: Context): String {
        return cachedUpstoxAccessToken ?: ""
    }

    fun setUpstoxAccessToken(context: Context, token: String) {
        cachedUpstoxAccessToken = token
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            val data = mapOf("upstox_access_token" to token)
            db.collection("users").document(uid).collection("config").document("secure_keys")
                .set(data, SetOptions.merge())
        }
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
            android.util.Log.e("SecureStorageManager", "Failed syncing upstox_auth to Firestore: ${e.localizedMessage}")
        }
    }

    fun logoutAndClearTokens(context: Context) {
        cachedUpstoxApiKey = ""
        cachedUpstoxAccessToken = ""
        cachedUpstoxApiSecret = ""
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            val data = mapOf<String, Any?>(
                "upstox_api_key_client" to "",
                "upstox_api_key" to "",
                "upstox_access_token" to "",
                "upstox_api_secret" to ""
            )
            db.collection("users").document(uid).collection("config").document("secure_keys")
                .set(data, SetOptions.merge())
        }
    }

    fun getUpstoxApiSecret(context: Context): String {
        return cachedUpstoxApiSecret ?: ""
    }

    fun setUpstoxApiSecret(context: Context, key: String) {
        cachedUpstoxApiSecret = key
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            val data = mapOf("upstox_api_secret" to key)
            db.collection("users").document(uid).collection("config").document("secure_keys")
                .set(data, SetOptions.merge())
        }
    }

    @Volatile
    private var cachedOpenRouterApiKey: String? = null
    @Volatile
    private var cachedActiveAiModel: String? = null

    fun getOpenRouterApiKey(context: Context): String {
        return cachedOpenRouterApiKey ?: ""
    }

    fun setOpenRouterApiKey(context: Context, key: String) {
        cachedOpenRouterApiKey = key
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            val data = mapOf("openrouter_api_key" to key)
            db.collection("users").document(uid).collection("config").document("secure_keys")
                .set(data, SetOptions.merge())
        }
    }

    fun getActiveAiModel(context: Context): String {
        return cachedActiveAiModel ?: "deepseek/deepseek-r1"
    }

    fun setActiveAiModel(context: Context, model: String) {
        cachedActiveAiModel = model
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            val data = mapOf("active_ai_model" to model)
            db.collection("users").document(uid).collection("config").document("secure_keys")
                .set(data, SetOptions.merge())
        }
    }

    fun getGroqApiKey(context: Context): String {
        return cachedGroqApiKey ?: ""
    }

    fun setGroqApiKey(context: Context, key: String) {
        cachedGroqApiKey = key
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            val data = mapOf("groq_api_key" to key)
            db.collection("users").document(uid).collection("config").document("secure_keys")
                .set(data, SetOptions.merge())
        }
    }

    fun getMonthlyBudget(context: Context): Double {
        return cachedMonthlyBudget ?: 500.0
    }

    fun setMonthlyBudget(context: Context, budget: Double) {
        cachedMonthlyBudget = budget
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            val data = mapOf("api_monthly_budget" to budget)
            db.collection("users").document(uid).collection("config").document("secure_keys")
                .set(data, SetOptions.merge())
        }
    }

    fun getDeepSeekApiKey(context: Context): String {
        return cachedDeepSeekApiKey ?: ""
    }

    fun setDeepSeekApiKey(context: Context, key: String) {
        cachedDeepSeekApiKey = key
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            val data = mapOf("deepseek_api_key" to key)
            db.collection("users").document(uid).collection("config").document("secure_keys")
                .set(data, SetOptions.merge())
        }
    }

    fun getActiveMakerEngine(context: Context): String {
        return getEncryptedPrefs(context).getString(KEY_ACTIVE_MAKER_ENGINE, "Groq (Llama 3)") ?: "Groq (Llama 3)"
    }

    fun setActiveMakerEngine(context: Context, engine: String) {
        getEncryptedPrefs(context).edit().putString(KEY_ACTIVE_MAKER_ENGINE, engine).apply()
    }

    fun getVirtualLedgerBalance(context: Context): Double {
        return cachedVirtualLedgerBalance ?: 100000.0
    }

    fun setVirtualLedgerBalance(context: Context, balance: Double) {
        cachedVirtualLedgerBalance = balance
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            val data = mapOf("virtual_ledger_balance" to balance)
            db.collection("users").document(uid).collection("config").document("secure_keys")
                .set(data, SetOptions.merge())
        }
    }

    fun fetchSecureKeysFromFirestore(onComplete: (Boolean) -> Unit = {}) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid).collection("config").document("secure_keys")
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        cachedGeminiApiKey = document.getString("gemini_api_key") ?: ""
                        cachedGroqApiKey = document.getString("groq_api_key") ?: ""
                        cachedDeepSeekApiKey = document.getString("deepseek_api_key") ?: ""
                        cachedMonthlyBudget = document.getDouble("api_monthly_budget") ?: 500.0
                        cachedFmpApiKey = document.getString("fmp_api_key") ?: ""
                        cachedUpstoxApiKey = document.getString("upstox_api_key_client") ?: document.getString("upstox_api_key") ?: ""
                        cachedUpstoxAccessToken = document.getString("upstox_access_token") ?: ""
                        cachedUpstoxApiSecret = document.getString("upstox_api_secret") ?: ""
                        cachedOpenRouterApiKey = document.getString("openrouter_api_key") ?: ""
                        cachedActiveAiModel = document.getString("active_ai_model") ?: "deepseek/deepseek-r1"
                        cachedVirtualLedgerBalance = document.getDouble("virtual_ledger_balance") ?: 100000.0
                    } else {
                        cachedGeminiApiKey = ""
                        cachedGroqApiKey = ""
                        cachedDeepSeekApiKey = ""
                        cachedMonthlyBudget = 500.0
                        cachedFmpApiKey = ""
                        cachedUpstoxApiKey = ""
                        cachedUpstoxAccessToken = ""
                        cachedUpstoxApiSecret = ""
                        cachedOpenRouterApiKey = ""
                        cachedActiveAiModel = "deepseek/deepseek-r1"
                        cachedVirtualLedgerBalance = 100000.0
                    }
                    com.debtfreein.app.data.network.UpstoxExecutionService.setVirtualLedgerBalance(cachedVirtualLedgerBalance ?: 100000.0)
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        } else {
            onComplete(false)
        }
    }
}
