package com.debtfreein.app.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debtfreein.app.data.security.SecureStorageManager
import com.debtfreein.app.data.security.TokenManager
import com.debtfreein.app.ui.viewmodel.FinanceViewModel
import com.debtfreein.app.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: FinanceViewModel,
    onNavigateToAuth: () -> Unit,
    profileViewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profileState by profileViewModel.uiState.collectAsState()
    
    // Load keys from state / local
    var upstoxKey by remember { mutableStateOf(TokenManager.upstoxApiKey) }
    var upstoxSecret by remember { mutableStateOf(TokenManager.upstoxApiSecret) }
    var upstoxRedirect by remember { mutableStateOf(TokenManager.upstoxRedirectUri) }
    var geminiKey by remember { mutableStateOf(TokenManager.geminiApiKey) }
    var groqKey by remember { mutableStateOf(TokenManager.groqApiKey) }

    // Sync input fields when Firestore data is loaded
    LaunchedEffect(profileState) {
        if (profileState.upstoxApiKey.isNotBlank()) upstoxKey = profileState.upstoxApiKey
        if (profileState.upstoxApiSecret.isNotBlank()) upstoxSecret = profileState.upstoxApiSecret
        if (profileState.upstoxRedirectUri.isNotBlank()) upstoxRedirect = profileState.upstoxRedirectUri
        if (profileState.geminiApiKey.isNotBlank()) geminiKey = profileState.geminiApiKey
        if (profileState.groqApiKey.isNotBlank()) groqKey = profileState.groqApiKey
    }

    // Other legacy key configurations
    var fmpKey by remember { mutableStateOf(SecureStorageManager.getFmpApiKey(context)) }
    var monthlyBudget by remember { mutableStateOf(SecureStorageManager.getMonthlyBudget(context).toString()) }
    var deepseekKey by remember { mutableStateOf(SecureStorageManager.getDeepSeekApiKey(context)) }
    var activeMakerEngine by remember { mutableStateOf(SecureStorageManager.getActiveMakerEngine(context)) }

    var openRouterKey by remember { mutableStateOf(TokenManager.openRouterApiKey.ifBlank { SecureStorageManager.getOpenRouterApiKey(context) }) }
    var activeAiModel by remember { mutableStateOf(TokenManager.activeAiModel.ifBlank { SecureStorageManager.getActiveAiModel(context) }) }
    var openRouterVisible by remember { mutableStateOf(false) }

    var geminiVisible by remember { mutableStateOf(false) }
    var groqVisible by remember { mutableStateOf(false) }
    var upstoxVisible by remember { mutableStateOf(false) }
    var upstoxSecretVisible by remember { mutableStateOf(false) }
    var deepseekVisible by remember { mutableStateOf(false) }
    var fmpVisible by remember { mutableStateOf(false) }

    // Dialog state for Add Card
    var showAddCardDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Profile & Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        // Upstox Connection Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Upstox Connection Status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                var isConnectedState by remember(profileState) { mutableStateOf(TokenManager.isTokenValid()) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Status",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isConnectedState) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isConnectedState) "Connected" else "Disconnected / Expired",
                            color = if (isConnectedState) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (isConnectedState) {
                    Button(
                        onClick = {
                            TokenManager.logoutAndClearTokens(context)
                            isConnectedState = false
                            Toast.makeText(context, "Upstox Session Cleared", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("DISCONNECT UPSTOX", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Button(
                    onClick = {
                        if (TokenManager.upstoxApiKey.isBlank() || TokenManager.upstoxRedirectUri.isBlank()) {
                            Toast.makeText(context, "Please save Upstox API Key & Redirect URI first", Toast.LENGTH_LONG).show()
                        } else {
                            onNavigateToAuth()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("RE-AUTHENTICATE WITH UPSTOX", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Key Vault Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Key Vault Section",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Upstox Key
                ProfileInputField(
                    label = "Upstox API Key",
                    value = upstoxKey,
                    onValueChange = { upstoxKey = it },
                    visible = upstoxVisible,
                    onVisibilityToggle = { upstoxVisible = !upstoxVisible },
                    placeholder = "Client Key"
                )

                // Upstox Secret
                ProfileInputField(
                    label = "Upstox API Secret",
                    value = upstoxSecret,
                    onValueChange = { upstoxSecret = it },
                    visible = upstoxSecretVisible,
                    onVisibilityToggle = { upstoxSecretVisible = !upstoxSecretVisible },
                    placeholder = "Client Secret"
                )

                // Upstox Redirect URI
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Upstox Redirect URI", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = upstoxRedirect,
                        onValueChange = { upstoxRedirect = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        placeholder = { Text("https://127.0.0.1", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                // Gemini API Key
                ProfileInputField(
                    label = "Gemini API Key",
                    value = geminiKey,
                    onValueChange = { geminiKey = it },
                    visible = geminiVisible,
                    onVisibilityToggle = { geminiVisible = !geminiVisible },
                    placeholder = "AIzaSy..."
                )

                // Groq API Key
                ProfileInputField(
                    label = "Groq API Key",
                    value = groqKey,
                    onValueChange = { groqKey = it },
                    visible = groqVisible,
                    onVisibilityToggle = { groqVisible = !groqVisible },
                    placeholder = "gsk_..."
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Additional Model Configurations",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )

                // DeepSeek API Key
                ProfileInputField(
                    label = "DeepSeek API Key",
                    value = deepseekKey,
                    onValueChange = { deepseekKey = it },
                    visible = deepseekVisible,
                    onVisibilityToggle = { deepseekVisible = !deepseekVisible },
                    placeholder = "sk-..."
                )

                // FMP API Key
                ProfileInputField(
                    label = "FMP API Key",
                    value = fmpKey,
                    onValueChange = { fmpKey = it },
                    visible = fmpVisible,
                    onVisibilityToggle = { fmpVisible = !fmpVisible },
                    placeholder = "FMP Market Token"
                )

                // Monthly Budget
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Monthly API Budget (INR)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = monthlyBudget,
                        onValueChange = { monthlyBudget = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        placeholder = { Text("500.0", color = Color(0xFF64748B)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                // Maker Selection Dropdown
                var dropdownExpanded by remember { mutableStateOf(false) }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Active 'Maker' Scanning Engine", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                        ) {
                            Text(activeMakerEngine, color = Color.White)
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(Color(0xFF1E293B))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Groq (Llama 3)", color = Color.White) },
                                onClick = {
                                    activeMakerEngine = "Groq (Llama 3)"
                                    dropdownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("DeepSeek V4-Pro", color = Color.White) },
                                onClick = {
                                    activeMakerEngine = "DeepSeek V4-Pro"
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        profileViewModel.saveCredentials(
                            upstoxKey = upstoxKey,
                            upstoxSecret = upstoxSecret,
                            upstoxRedirect = upstoxRedirect,
                            geminiKey = geminiKey,
                            groqKey = groqKey,
                            onSuccess = {
                                // Save in SecureStorageManager for backwards compatibility
                                SecureStorageManager.setGeminiApiKey(context, geminiKey)
                                SecureStorageManager.setFmpApiKey(context, fmpKey)
                                SecureStorageManager.setUpstoxApiKey(context, upstoxKey)
                                SecureStorageManager.setUpstoxApiSecret(context, upstoxSecret)
                                SecureStorageManager.setGroqApiKey(context, groqKey)
                                SecureStorageManager.setDeepSeekApiKey(context, deepseekKey)
                                SecureStorageManager.setActiveMakerEngine(context, activeMakerEngine)
                                
                                val budgetVal = monthlyBudget.toDoubleOrNull() ?: 500.0
                                SecureStorageManager.setMonthlyBudget(context, budgetVal)
                                
                                // Sync keys to view model flow
                                viewModel.saveGeminiApiKey(geminiKey)
                                viewModel.saveMarketApiKey(fmpKey)
                                
                                Toast.makeText(context, "API Credentials Saved & Cloud Synced", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    if (profileState.isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text("SAVE API CREDENTIALS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Credit Card addition card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Liabilities & Accounts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Add a new credit card account to track outstanding liabilities and analyze balance transfer opportunities.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 16.sp
                )
                Button(
                    onClick = { showAddCardDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Card")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ADD CREDIT CARD", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAddCardDialog) {
        ProfileAddCardDialog(
            onDismiss = { showAddCardDialog = false },
            onAdd = { name, issuer, balance, limit, apr, dueDay, minPay, lastFour ->
                viewModel.addCard(name, issuer, balance, limit, apr, dueDay, null, minPay, lastFour)
                showAddCardDialog = false
            }
        )
    }
}

@Composable
fun ProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onVisibilityToggle: () -> Unit,
    placeholder: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
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
                        contentDescription = "Toggle Key",
                        tint = Color(0xFF64748B)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6366F1),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A)
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

@Composable
fun ProfileAddCardDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Double, Double, Double, Int, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var issuer by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }
    var apr by remember { mutableStateOf("") }
    var dueDay by remember { mutableStateOf("") }
    var minPay by remember { mutableStateOf("") }
    var lastFour by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Card") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Card Name (e.g. Millennia)") })
                OutlinedTextField(value = issuer, onValueChange = { issuer = it }, label = { Text("Bank Issuer (e.g. HDFC)") })
                OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("Current Balance (INR)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = limit, onValueChange = { limit = it }, label = { Text("Credit Limit (INR)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = apr, onValueChange = { apr = it }, label = { Text("Annual APR %") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = dueDay, onValueChange = { dueDay = it }, label = { Text("Payment Due Day (1-31)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = minPay, onValueChange = { minPay = it }, label = { Text("Minimum Payment (INR)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = lastFour, onValueChange = { lastFour = it }, label = { Text("Last 4 Digits") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(onClick = {
                val balVal = balance.toDoubleOrNull() ?: 0.0
                val limVal = limit.toDoubleOrNull() ?: 0.0
                val aprVal = apr.toDoubleOrNull() ?: 0.0
                val dueVal = dueDay.toIntOrNull() ?: 15
                val minVal = minPay.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && issuer.isNotBlank() && lastFour.length == 4) {
                    onAdd(name, issuer, balVal, limVal, aprVal, dueVal, minVal, lastFour)
                }
            }) {
                Text("Log Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
