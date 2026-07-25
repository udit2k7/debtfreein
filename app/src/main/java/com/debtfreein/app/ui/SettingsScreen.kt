package com.debtfreein.app.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debtfreein.app.data.security.SecureStorageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    var geminiKey by remember { mutableStateOf(SecureStorageManager.getGeminiApiKey(context)) }
    var fmpKey by remember { mutableStateOf(SecureStorageManager.getFmpApiKey(context)) }
    var upstoxKey by remember { mutableStateOf(SecureStorageManager.getUpstoxApiKey(context)) }
    var upstoxSecret by remember { mutableStateOf(SecureStorageManager.getUpstoxApiSecret(context)) }

    var geminiVisible by remember { mutableStateOf(false) }
    var fmpVisible by remember { mutableStateOf(false) }
    var upstoxVisible by remember { mutableStateOf(false) }
    var upstoxSecretVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "System Configuration", 
                        color = Color.White, 
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Credentials & API Tokens",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Info Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Hardware Encryption Active",
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "All credentials entered here are encrypted via AES-256 natively using Android Keystore hardware keys. They never leave the device.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Gemini API Key Field
            CredentialInputField(
                label = "Gemini API Key",
                value = geminiKey,
                onValueChange = { geminiKey = it },
                visible = geminiVisible,
                onVisibilityToggle = { geminiVisible = !geminiVisible },
                placeholder = "AI Advisor (e.g. AIzaSy...)"
            )

            // FMP API Key Field
            CredentialInputField(
                label = "FMP (Financial Modeling Prep) API Key",
                value = fmpKey,
                onValueChange = { fmpKey = it },
                visible = fmpVisible,
                onVisibilityToggle = { fmpVisible = !fmpVisible },
                placeholder = "Stock/Market Data Token"
            )

            // Upstox API Key Field
            CredentialInputField(
                label = "Upstox API Key",
                value = upstoxKey,
                onValueChange = { upstoxKey = it },
                visible = upstoxVisible,
                onVisibilityToggle = { upstoxVisible = !upstoxVisible },
                placeholder = "Upstox Developer Client ID"
            )

            // Upstox API Secret Field
            CredentialInputField(
                label = "Upstox API Secret",
                value = upstoxSecret,
                onValueChange = { upstoxSecret = it },
                visible = upstoxSecretVisible,
                onVisibilityToggle = { upstoxSecretVisible = !upstoxSecretVisible },
                placeholder = "Upstox Client Secret Key"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    SecureStorageManager.setGeminiApiKey(context, geminiKey)
                    SecureStorageManager.setFmpApiKey(context, fmpKey)
                    SecureStorageManager.setUpstoxApiKey(context, upstoxKey)
                    SecureStorageManager.setUpstoxApiSecret(context, upstoxSecret)
                    Toast.makeText(context, "Credentials Saved Successfully", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1), // Indigo accent
                    contentColor = Color.White
                )
            ) {
                Text(
                    "SAVE SECURELY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun CredentialInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onVisibilityToggle: () -> Unit,
    placeholder: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            placeholder = { Text(placeholder, color = Color(0xFF64748B)) },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onVisibilityToggle) {
                    Icon(
                        imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (visible) "Hide" else "Show",
                        tint = Color(0xFF64748B)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6366F1),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}
