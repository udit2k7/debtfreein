package com.debtfreein.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debtfreein.app.ui.viewmodel.FinanceViewModel
import com.debtfreein.app.data.repository.TradePostMortem

import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.debtfreein.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaperTradingDashboard(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isPaperTradingActive by viewModel.isPaperTradingActive.collectAsState()
    val virtualLedger by viewModel.virtualLedger.collectAsState()
    val postMortems by viewModel.postMortems.collectAsState()
    val telemetryMetrics by viewModel.telemetryMetrics.collectAsState()
    val telemetryLogs by com.debtfreein.app.data.ai.AutonomousPaperTrader.telemetryLogs.collectAsState()
    val localBotMicroState by com.debtfreein.app.data.ai.AutonomousPaperTrader.botMicroState.collectAsState()
    val cloudStatus by com.debtfreein.app.data.system.SystemStatusManager.cloudStatus.collectAsState()
    val liveCloudTrades by viewModel.liveCloudTrades.collectAsState()
    val isLiveTradingActive by com.debtfreein.app.data.system.SystemStatusManager.isLiveTradingActive.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.tradeNotification.collect { trade ->
            val actionText = if (trade.action.isNotBlank()) trade.action.uppercase() else "SIGNAL"
            val symbolText = if (trade.symbol.isNotBlank()) trade.symbol.uppercase() else "ASSET"
            android.widget.Toast.makeText(
                context,
                "New AI Cloud Trade Signal: $symbolText $actionText",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    val effectiveMicroState = remember(cloudStatus, localBotMicroState, telemetryMetrics.botStatus) {
        val cloudMicro = cloudStatus?.botMicroState
        if (!cloudMicro.isNullOrBlank()) {
            cloudMicro
        } else if (localBotMicroState.isNotBlank()) {
            localBotMicroState
        } else {
            telemetryMetrics.botStatus
        }
    }

    val formattedLastScanUtc = remember(cloudStatus?.lastScanUtc) {
        val raw = cloudStatus?.lastScanUtc
        if (raw.isNullOrBlank()) {
            "N/A"
        } else {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val parsed = java.time.LocalDateTime.parse(raw.take(19))
                    parsed.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss 'UTC'"))
                } else {
                    raw
                }
            } catch (e: Exception) {
                raw
            }
        }
    }

    // Breathing status dot animation
    val infiniteTransition = rememberInfiniteTransition()
    val breathingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val statusColor = when (effectiveMicroState.lowercase()) {
        "scanning", "evaluating", "active", "scanning nse", "evaluating ai" -> Color(0xFF10B981) // Green
        "standby", "idling" -> Color(0xFFF59E0B) // Amber
        else -> Color(0xFF10B981)
    }

    // Calculate metrics
    val totalNetProfit = postMortems.sumOf { it.netProfit }
    val wins = postMortems.count { it.netProfit > 0 }
    val losses = postMortems.count { it.netProfit < 0 }
    val totalTrades = postMortems.size
    val winRatio = if (totalTrades > 0) (wins.toDouble() / totalTrades.toDouble()) * 100.0 else 0.0

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
                            "Autonomous Command Center",
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
                actions = {
                    IconButton(onClick = { viewModel.resetVirtualLedger() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Ledger", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                containerColor = Color(0xFF1E293B),
                title = {
                    Text(
                        "SECURITY VERIFICATION",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Enter Security PIN to activate Live Upstox Broker Order Execution:",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = { if (it.length <= 4) enteredPin = it },
                            label = { Text("4-Digit PIN") },
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFEF4444),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedLabelColor = Color(0xFFEF4444),
                                cursorColor = Color(0xFFEF4444),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (pinError != null) {
                            Text(pinError!!, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (enteredPin == "0000") {
                                showPinDialog = false
                                try {
                                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                        .collection("system_config")
                                        .document("master_switches")
                                        .set(
                                            mapOf(
                                                "is_live_trading_active" to true,
                                                "updated_at" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                            )
                                        )
                                    com.debtfreein.app.data.system.SystemStatusManager.setLiveTradingActive(true)
                                    android.widget.Toast.makeText(context, "LIVE BROKER EXECUTION ACTIVATED!", android.widget.Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Firestore Error: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            } else {
                                pinError = "Invalid Security PIN. Access Denied."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("ACTIVATE LIVE TRADING", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) {
                        Text("CANCEL", color = Color(0xFF94A3B8))
                    }
                }
            )
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Master Execution Switch Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLiveTradingActive) Color(0xFF450A0A) else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isLiveTradingActive) Color(0xFFEF4444) else Color(0xFF334155),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            if (isLiveTradingActive) Color(0xFFEF4444) else Color(0xFF10B981),
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (isLiveTradingActive) "LIVE BROKER EXECUTION" else "PAPER SIMULATION MODE",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (isLiveTradingActive) "Routing AI Signals directly to Upstox Live Capital" else "Simulating Execution in Virtual Ledger",
                                fontSize = 11.sp,
                                color = if (isLiveTradingActive) Color(0xFFFCA5A5) else Color(0xFF94A3B8)
                            )
                        }

                        Switch(
                            checked = isLiveTradingActive,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    enteredPin = ""
                                    pinError = null
                                    showPinDialog = true
                                } else {
                                    try {
                                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            .collection("system_config")
                                            .document("master_switches")
                                            .set(
                                                mapOf(
                                                    "is_live_trading_active" to false,
                                                    "updated_at" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                                )
                                            )
                                        com.debtfreein.app.data.system.SystemStatusManager.setLiveTradingActive(false)
                                        android.widget.Toast.makeText(context, "Switched to Paper Trading Mode", android.widget.Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Error updating switch: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFEF4444),
                                uncheckedThumbColor = Color(0xFF94A3B8),
                                uncheckedTrackColor = Color(0xFF334155)
                            )
                        )
                    }
                }
            }
            // Live Stats Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "LIVE SIMULATOR METRICS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Virtual Balance", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                Text(
                                    "₹${String.format("%.2f", virtualLedger.balance)}",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Net Profit", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                val profitColor = if (totalNetProfit >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                Text(
                                    "${if (totalNetProfit >= 0) "+" else ""}₹${String.format("%.2f", totalNetProfit)}",
                                    color = profitColor,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF334155))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Win / Loss Record", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                Text(
                                    "$wins W - $losses L",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Win Ratio", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                Text(
                                    "${String.format("%.1f", winRatio)}%",
                                    color = Color(0xFF6366F1),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Live Bot Telemetry Panel Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "LIVE BOT TELEMETRY PANEL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                                if (formattedLastScanUtc != "N/A") {
                                    Text(
                                        "Last Scan: $formattedLastScanUtc",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(statusColor.copy(alpha = breathingAlpha), shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    effectiveMicroState.uppercase(),
                                    color = statusColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF334155))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text("Dynamic Tripwires", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                Text(
                                    telemetryMetrics.activeTripwires,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                Text("Live Session PnL", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                val pnl = telemetryMetrics.liveSessionPnL
                                val pnlColor = if (pnl >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                Text(
                                    "${if (pnl >= 0) "+" else ""}₹${String.format("%.2f", pnl)}",
                                    color = pnlColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                com.debtfreein.app.data.ai.AutonomousPaperTrader.executeScanCycle(context)
                                android.widget.Toast.makeText(context, "Force Live Scan Triggered!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Force Scan", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("FORCE LIVE SCAN NOW", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Dark AiTelemetryTerminal Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            "LIVE AI TELEMETRY STREAM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6366F1)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (telemetryLogs.isEmpty()) {
                                item {
                                    Text(
                                        "No telemetry logs yet. Tap FORCE LIVE SCAN NOW.",
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                items(telemetryLogs) { log ->
                                    Text(
                                        text = log,
                                        color = Color(0xFF38BDF8),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // AI Cognitive Stream Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "AI Cognitive Stream",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF312E81)
                            ) {
                                Text(
                                    "DeepSeek Real-time Reasoning",
                                    color = Color(0xFF818CF8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF334155))
                        )

                        Text(
                            text = telemetryMetrics.lastDecisionReason,
                            color = Color(0xFF34D399),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )
                    }
                }
            }

            // Live Cloud Signals & Positions Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "LIVE CLOUD SIGNALS & POSITIONS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "${liveCloudTrades.size} Signals",
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF334155))
                        )

                        if (liveCloudTrades.isEmpty()) {
                            Text(
                                "No real-time cloud signals received yet.",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                liveCloudTrades.forEach { trade ->
                                    val actionColor = when (trade.action.uppercase()) {
                                        "BUY" -> Color(0xFF10B981)
                                        "SELL" -> Color(0xFFEF4444)
                                        else -> Color(0xFFF59E0B)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0F172A),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    trade.symbol.ifBlank { "UNKNOWN" }.uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 13.sp
                                                )

                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = actionColor.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        trade.action.ifBlank { "SIGNAL" }.uppercase(),
                                                        color = actionColor,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 11.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            if (trade.conviction.isNotBlank()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        "Groq Conviction: ${trade.conviction}/100",
                                                        color = Color(0xFF818CF8),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    if (trade.stopLoss > 0.0) {
                                                        Text(
                                                            "SL: ₹${trade.stopLoss} | Target: ₹${trade.targetPrice}",
                                                            color = Color(0xFFF43F5E),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }
                                            }

                                            if (trade.patternName.isNotBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        "Pattern: ${trade.patternName} (${trade.visionConfidence}% Vision Conf)",
                                                        color = Color(0xFF34D399),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            if (trade.riskAnalysis.isNotBlank()) {
                                                Text(
                                                    "CRO Analysis: ${trade.riskAnalysis.take(120)}...",
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }

                                            if (trade.reason.isNotBlank()) {
                                                Text(
                                                    trade.reason,
                                                    color = Color(0xFFCBD5E1),
                                                    fontSize = 11.sp,
                                                    lineHeight = 15.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Auto-Bot Control Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPaperTradingActive) Color(0xFF1E1B4B) else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (isPaperTradingActive) Color(0xFF6366F1) else Color(0xFF334155),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "AUTONOMOUS TRADING BOT",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (isPaperTradingActive) "ACTIVE (Polling every 15m)" else "STANDBY / INACTIVE",
                                color = if (isPaperTradingActive) Color(0xFF818CF8) else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
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
                }
            }

            // Trade-Brain DB Header
            item {
                Text(
                    "Trade-Brain DB Cognitive Logs (${postMortems.size} trades)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (postMortems.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No logged trade reflections in Firestore.",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(postMortems) { pm ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Trade: ${pm.tradeId}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    fontFamily = FontFamily.Monospace
                                )

                                val profitColor = if (pm.netProfit >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                Text(
                                    "${if (pm.netProfit >= 0) "+" else ""}₹${String.format("%.2f", pm.netProfit)}",
                                    color = profitColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFF334155))
                            )

                            Column {
                                Text("Rationale:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(pm.rationale, color = Color.White, fontSize = 12.sp)
                            }

                            Column {
                                Text("Mistakes Made:", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(pm.mistakesMade, color = Color(0xFFFCA5A5), fontSize = 12.sp)
                            }

                            Column {
                                Text("Lessons Learned:", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(pm.lessonsLearned, color = Color(0xFFA7F3D0), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
