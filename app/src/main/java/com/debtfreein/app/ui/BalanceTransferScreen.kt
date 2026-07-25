package com.debtfreein.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debtfreein.app.data.model.CreditCard
import com.debtfreein.app.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceTransferScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cards by viewModel.cards.collectAsState()

    var promoAprStr by remember { mutableStateOf("0.0") }
    var promoDurationStr by remember { mutableStateOf("12") }
    var transferFeeStr by remember { mutableStateOf("3.0") }
    
    val selectedCards = remember { mutableStateMapOf<Long, Boolean>() }
    
    LaunchedEffect(cards) {
        cards.forEach { card ->
            if (!selectedCards.containsKey(card.id)) {
                selectedCards[card.id] = card.currentBalance > 0
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Balance Transfer Optimizer", 
                        color = Color.White, 
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    ) 
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
            // Explanation Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color(0xFF6366F1))
                    Text(
                        text = "Consolidate high-interest credit card debt onto a lower promotional rate (often 0%) to freeze interest compounding.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Inputs
            Text("Promotional Offer Parameters", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = promoAprStr,
                    onValueChange = { promoAprStr = it },
                    label = { Text("Promo APR %") },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    )
                )
                OutlinedTextField(
                    value = transferFeeStr,
                    onValueChange = { transferFeeStr = it },
                    label = { Text("Fee %") },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    )
                )
                OutlinedTextField(
                    value = promoDurationStr,
                    onValueChange = { promoDurationStr = it },
                    label = { Text("Months") },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    )
                )
            }

            // Cards list
            Text("Select Cards to Include", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (cards.none { it.currentBalance > 0 }) {
                Text("No active liabilities logged. Financial peace active.", color = Color(0xFF94A3B8), fontSize = 13.sp)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cards.forEach { card ->
                        if (card.currentBalance > 0) {
                            val isChecked = selectedCards[card.id] ?: false
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCards[card.id] = !isChecked }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { selectedCards[card.id] = it }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(card.name, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Balance: ₹${card.currentBalance} (APR: ${card.apr}%)", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Calculator Math
            val activePromoApr = promoAprStr.toDoubleOrNull() ?: 0.0
            val activeFeePercent = transferFeeStr.toDoubleOrNull() ?: 3.0
            val activeDuration = promoDurationStr.toDoubleOrNull() ?: 12.0

            var totalConsolidatedBalance = 0.0
            var originalInterestAccrued = 0.0
            
            cards.forEach { card ->
                if (selectedCards[card.id] == true) {
                    totalConsolidatedBalance += card.currentBalance
                    originalInterestAccrued += card.currentBalance * (card.apr / 100.0) * (activeDuration / 12.0)
                }
            }

            val transferFee = totalConsolidatedBalance * (activeFeePercent / 100.0)
            val promotionalInterest = totalConsolidatedBalance * (activePromoApr / 100.0) * (activeDuration / 12.0)
            val totalNewCost = transferFee + promotionalInterest
            val netSavings = originalInterestAccrued - totalNewCost

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color(0xFF334155))

            // Savings Output Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Consolidated Amount:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Text("₹${String.format("%,.2f", totalConsolidatedBalance)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Transfer Fee:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Text("₹${String.format("%,.2f", transferFee)}", color = Color(0xFFEF4444), fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Interest Saved:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Text("₹${String.format("%,.2f", originalInterestAccrued)}", color = Color(0xFF10B981), fontSize = 13.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (netSavings >= 0) "NET SAVINGS" else "NET EXTRA COST",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = if (netSavings >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                        Text(
                            text = "₹${String.format("%,.2f", Math.abs(netSavings))}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = if (netSavings >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}
