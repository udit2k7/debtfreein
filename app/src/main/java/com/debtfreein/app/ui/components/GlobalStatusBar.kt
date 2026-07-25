package com.debtfreein.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debtfreein.app.data.system.SystemStatusManager

@Composable
fun GlobalStatusBar(
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        SystemStatusManager.refreshStatus()
    }

    val isApiConnected by SystemStatusManager.isUpstoxConnected.collectAsState()
    val isPaperTrading by SystemStatusManager.isPaperTradingActive.collectAsState()
    val isAiActive by SystemStatusManager.isAiScanningActive.collectAsState()
    val isLiveActive by SystemStatusManager.isLiveTradingActive.collectAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // [Dot 1] API Status
        StatusDotItem(
            label = "API",
            isActive = isApiConnected,
            activeColor = Color(0xFF10B981), // Green
            inactiveColor = Color(0xFFEF4444) // Red
        )

        // [Dot 2] PAPER/LIVE Execution Status
        StatusDotItem(
            label = if (isLiveActive) "LIVE" else "PAPER",
            isActive = isPaperTrading || isLiveActive,
            activeColor = if (isLiveActive) Color(0xFFEF4444) else Color(0xFF10B981), // Red if LIVE, Green if PAPER
            inactiveColor = Color(0xFF64748B), // Gray
            isGlowing = isLiveActive
        )

        // [Dot 3] AI QUANT Status
        StatusDotItem(
            label = "AI QUANT",
            isActive = isAiActive,
            activeColor = Color(0xFF6366F1), // Indigo
            inactiveColor = Color(0xFF64748B) // Gray
        )

        // [Dot 4] BROKER SWITCH Status
        StatusDotItem(
            label = if (isLiveActive) "BROKER LIVE" else "BROKER IDLE",
            isActive = isLiveActive,
            activeColor = Color(0xFFEF4444), // Glowing Red
            inactiveColor = Color(0xFF64748B), // Gray
            isGlowing = isLiveActive
        )
    }
}

@Composable
fun StatusDotItem(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    isGlowing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val targetColor = if (isActive) activeColor else inactiveColor
    val animatedColor by animateColorAsState(targetValue = targetColor, label = "dotColor")

    val alpha = if (isGlowing) {
        val transition = rememberInfiniteTransition(label = "glowTransition")
        val animatedAlpha by transition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha"
        )
        animatedAlpha
    } else {
        1.0f
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(alpha)
                .clip(CircleShape)
                .background(animatedColor)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = Color(0xFFE2E8F0),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
