package com.glassvpn.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glassvpn.app.ui.theme.GlassColors

/** Frosted glass card — the core Liquid Glass surface. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val m = if (onClick != null) {
        modifier.clip(RoundedCornerShape(cornerRadius))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    } else modifier.clip(RoundedCornerShape(cornerRadius))

    Column(
        modifier = m
            .background(GlassColors.glassBrush(), RoundedCornerShape(cornerRadius))
            .border(1.dp, GlassColors.glassBorder, RoundedCornerShape(cornerRadius))
            .padding(18.dp),
        content = content
    )
}

/** iOS-style switch. */
@Composable
fun IOSToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val anim by animateFloatAsState(targetValue = if (checked) 1f else 0f, label = "toggle")
    val trackColor = Color(0xFF30D158).copy(alpha = 0.35f + 0.65f * anim)
    val trackOff = Color.White.copy(alpha = 0.22f)

    Box(
        modifier = modifier
            .size(52.dp, 32.dp)
            .clip(CircleShape)
            .background(if (checked) trackColor else trackOff)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = (20.dp * anim))
                .size(26.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/** iOS grouped-list row with optional chevron. */
@Composable
fun IOSListRow(
    title: String,
    subtitle: String? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val m = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Row(
        modifier = m
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading?.invoke()
        if (leading != null) Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = GlassColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            subtitle?.let {
                Text(it, color = GlassColors.textSecondary, fontSize = 13.sp)
            }
        }
        trailing?.invoke()
        if (showChevron) {
            Text("›", color = GlassColors.textTertiary, fontSize = 22.sp, fontWeight = FontWeight.Light)
        }
    }
}

/** iOS segmented control. */
@Composable
fun IOSSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(3.dp)
    ) {
        options.forEachIndexed { i, label ->
            val selected = i == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (selected) Color.White.copy(alpha = 0.28f) else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(i) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (selected) Color.White else GlassColors.textSecondary,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

/** iOS-style translucent tab bar. */
@Composable
fun IOSTabBar(
    tabs: List<String>,
    icons: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                )
            )
            .padding(top = 10.dp, bottom = 22.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { i, label ->
            val selected = i == selectedIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(i) }
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Text(
                    icons[i],
                    fontSize = 23.sp,
                    modifier = Modifier.scale(if (selected) 1.12f else 1f)
                )
                Text(
                    label,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) GlassColors.iosBlue else GlassColors.textTertiary
                )
            }
        }
    }
}

/** Ping badge with iOS-style pill. */
@Composable
fun PingBadge(pingMs: Int, modifier: Modifier = Modifier) {
    val color = com.glassvpn.app.ui.theme.pingColor(pingMs)
    val text = if (pingMs < 0) "--" else "${pingMs}ms"
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.45f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Large circular power/connect button — Liquid Glass style. */
@Composable
fun PowerButton(
    connected: Boolean,
    connecting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp
) {
    val glowColor = if (connected) GlassColors.iosGreen else Color.Transparent
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (connected) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor.copy(alpha = 0.35f), Color.Transparent),
                        center = center,
                        radius = size.toPx() / 2
                    ),
                    radius = size.toPx() / 2,
                    center = center
                )
            }
            // Ring
            drawCircle(
                brush = GlassColors.powerButtonRing(connected),
                radius = size.toPx() / 2 - 4.dp.toPx(),
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
        }
        // Glass inner
        Box(
            modifier = Modifier
                .size(size - 28.dp)
                .clip(CircleShape)
                .background(GlassColors.glassBrush())
                .border(1.dp, GlassColors.glassHighlight.copy(alpha = 0.5f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "⏻",
                    fontSize = 52.sp,
                    color = if (connected) GlassColors.iosGreen else GlassColors.textPrimary
                )
                Text(
                    when {
                        connecting -> "···"
                        connected -> "ON"
                        else -> "OFF"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (connected) GlassColors.iosGreen else GlassColors.textSecondary
                )
            }
        }
    }
}
