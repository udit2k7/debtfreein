package com.debtfreein.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.debtfreein.app.data.security.TokenManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {
    
    data class ProfileUiState(
        val upstoxApiKey: String = "",
        val upstoxApiSecret: String = "",
        val upstoxRedirectUri: String = "https://127.0.0.1",
        val geminiApiKey: String = "",
        val groqApiKey: String = "",
        val isSaving: Boolean = false,
        val isFetching: Boolean = false
    )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        fetchKeysFromFirestore()
    }

    fun fetchKeysFromFirestore() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "YOUR_USER_ID"
        _uiState.value = _uiState.value.copy(isFetching = true)
        
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val upstoxKey = document.getString("upstox_api_key") ?: ""
                    val upstoxSecret = document.getString("upstox_api_secret") ?: ""
                    val upstoxRedirect = document.getString("upstox_redirect_uri") ?: "https://127.0.0.1"
                    val geminiKey = document.getString("gemini_api_key") ?: ""
                    val groqKey = document.getString("groq_api_key") ?: ""
                    
                    // Update UI state
                    _uiState.value = ProfileUiState(
                        upstoxApiKey = upstoxKey,
                        upstoxApiSecret = upstoxSecret,
                        upstoxRedirectUri = upstoxRedirect,
                        geminiApiKey = geminiKey,
                        groqApiKey = groqKey,
                        isFetching = false
                    )

                    // Save locally to TokenManager
                    TokenManager.saveCredentials(
                        upstoxKey = upstoxKey,
                        upstoxSecret = upstoxSecret,
                        upstoxRedirect = upstoxRedirect,
                        geminiKey = geminiKey,
                        groqKey = groqKey
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isFetching = false)
                }
            }
            .addOnFailureListener {
                _uiState.value = _uiState.value.copy(isFetching = false)
            }
    }

    fun saveCredentials(
        upstoxKey: String,
        upstoxSecret: String,
        upstoxRedirect: String,
        geminiKey: String,
        groqKey: String,
        onSuccess: () -> Unit = {}
    ) {
        _uiState.value = _uiState.value.copy(isSaving = true)
        
        // Save to TokenManager locally
        TokenManager.saveCredentials(
            upstoxKey = upstoxKey,
            upstoxSecret = upstoxSecret,
            upstoxRedirect = upstoxRedirect,
            geminiKey = geminiKey,
            groqKey = groqKey
        )

        // Sync local save to view model state immediately
        _uiState.value = ProfileUiState(
            upstoxApiKey = upstoxKey,
            upstoxApiSecret = upstoxSecret,
            upstoxRedirectUri = upstoxRedirect,
            geminiApiKey = geminiKey,
            groqApiKey = groqKey,
            isSaving = false
        )

        // Push to Firestore
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "YOUR_USER_ID"
        val data = mapOf(
            "upstox_api_key" to upstoxKey,
            "upstox_api_secret" to upstoxSecret,
            "upstox_redirect_uri" to upstoxRedirect,
            "gemini_api_key" to geminiKey,
            "groq_api_key" to groqKey
        )
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onSuccess()
            }
    }
}
