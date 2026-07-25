package com.debtfreein.app.ui

import android.widget.Toast
import com.debtfreein.app.ui.viewmodel.FinanceViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.debtfreein.app.R
import com.debtfreein.app.data.security.SecureStorageManager
import com.debtfreein.app.data.network.CapitalAllocator
import com.debtfreein.app.data.network.UpstoxExecutionService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntradayExecutionScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onNavigateToAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    var inputCapital by remember { mutableStateOf(CapitalAllocator.tradeableCapital.toString()) }
    var inputTarget by remember { mutableStateOf(CapitalAllocator.targetNetProfit.toString()) }
    var inputBalance by remember { mutableStateOf(CapitalAllocator.totalBalance.toString()) }
    
    val netPnL by CapitalAllocator.currentNetPnL.collectAsState()
    val sysState by UpstoxExecutionService.systemState.collectAsState()
    val logs by UpstoxExecutionService.logs.collectAsState()
    val executionState by viewModel.executionState.collectAsState()
    val isPaperTradingActive by viewModel.isPaperTradingActive.collectAsState()
    val virtualLedger by viewModel.virtualLedger.collectAsState()
    var inputVirtualBalance by remember(virtualLedger.balance) { mutableStateOf(virtualLedger.balance.toString()) }
    var showAuthErrorDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_phone_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Intraday Trading Hub", 
                            color = Color.White, 
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ) 
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
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
            // Paper Trading Mode Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isPaperTradingActive) Color(0xFF0F172A) else Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(
                    width = 1.dp,
                    color = if (isPaperTradingActive) Color(0xFF6366F1) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Cognitive Paper Trading",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Bypasses Upstox and runs in local simulation",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = isPaperTradingActive,
                            onCheckedChange = { viewModel.setPaperTradingActive(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF6366F1),
                                checkedTrackColor = Color(0xFF312E81)
                            )
                        )
                    }

                    if (isPaperTradingActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF334155))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputVirtualBalance,
                                onValueChange = { newValue ->
                                    inputVirtualBalance = newValue
                                    val parsed = newValue.toDoubleOrNull()
                                    if (parsed != null && parsed >= 0.0) {
                                        viewModel.updateVirtualLedgerBalance(context, parsed)
                                    }
                                },
                                label = { Text("Virtual Ledger Balance (INR)") },
                                textStyle = LocalTextStyle.current.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.resetVirtualLedger()
                                    inputVirtualBalance = UpstoxExecutionService.virtualLedger.value.balance.toString()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("RESET", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Configuration Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Risk Parameters Configuration", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    // Max Daily Allocation
                    OutlinedTextField(
                        value = inputCapital,
                        onValueChange = { inputCapital = it },
                        label = { Text("Max Daily Allocation (INR)") },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        )
                    )

                    // Target Net Profit
                    OutlinedTextField(
                        value = inputTarget,
                        onValueChange = { inputTarget = it },
                        label = { Text("Target Net Profit Limit (INR)") },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        )
                    )

                    // Total Virtual Pool
                    OutlinedTextField(
                        value = inputBalance,
                        onValueChange = { inputBalance = it },
                        label = { Text("Total Virtual Pool (INR)") },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        )
                    )

                    Button(
                        onClick = {
                            val cap = inputCapital.toDoubleOrNull() ?: 0.0
                            val target = inputTarget.toDoubleOrNull() ?: 0.0
                            val bal = inputBalance.toDoubleOrNull() ?: 0.0
                            
                            CapitalAllocator.tradeableCapital = cap
                            CapitalAllocator.targetNetProfit = target
                            CapitalAllocator.totalBalance = bal
                            
                            UpstoxExecutionService.clearLogs()
                            
                            if (com.debtfreein.app.data.security.TokenManager.isTokenValid()) {
                                Toast.makeText(context, "Parameters Synced Successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                showAuthErrorDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Sync")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SYNC RISK PARAMETERS", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            UpstoxExecutionService.clearLogs()
                            onNavigateToAuth()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))
                    ) {
                        Text("AUTHENTICATE UPSTOX", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Real-time status card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Real-time Risk Metrics", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Current Day PnL:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        val pnlColor = if (netPnL >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                        Text(
                            text = (if (netPnL >= 0) "+" else "") + "₹${String.format("%.2f", netPnL)}",
                            color = pnlColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("System State Guard:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        val stateColor = when (sysState) {
                            com.debtfreein.app.data.network.SystemState.ACTIVE -> Color(0xFF10B981)
                            com.debtfreein.app.data.network.SystemState.COMPLETED_FOR_DAY -> Color(0xFF3B82F6)
                            com.debtfreein.app.data.network.SystemState.LOCKED_DRAWDOWN -> Color(0xFFEF4444)
                        }
                        Text(sysState.name, color = stateColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Execution State:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        val execColor = if (executionState == "IDLE") Color(0xFF94A3B8) else Color(0xFF10B981)
                        Text(executionState, color = execColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // The Kill Switch
            Button(
                onClick = {
                    viewModel.emergencyStop()
                    Toast.makeText(context, "EMERGENCY STOP EXECUTION TRIGGERED!", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text(
                    text = "STOP EXECUTION",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            // execution logs
            Text("Execution Engine Logs", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                if (logs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No logs received yet. System Idle.", color = Color(0xFF64748B), fontSize = 12.sp)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        logs.forEach { log ->
                            Text(
                                text = log,
                                color = Color(0xFF34D399),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAuthErrorDialog) {
        AlertDialog(
            onDismissRequest = { showAuthErrorDialog = false },
            title = { Text("Upstox Token Required") },
            text = { Text("Upstox token is missing or invalid. Please go to the Profile tab to save your API credentials and complete log in.") },
            confirmButton = {
                Button(
                    onClick = { showAuthErrorDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
}
