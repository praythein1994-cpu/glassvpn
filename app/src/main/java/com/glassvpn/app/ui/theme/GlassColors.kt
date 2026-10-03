package com.glassvpn.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object GlassColors {
    // iOS 26 Liquid Glass dark palette
    val bgTop = Color(0xFF0B0B14)
    val bgBottom = Color(0xFF14141F)
    val auroraBlue = Color(0xFF1B2A6B)
    val auroraPurple = Color(0xFF3B1B6B)
    val auroraTeal = Color(0xFF0B4B4B)

    val glassWhite = Color.White.copy(alpha = 0.10f)
    val glassWhiteStrong = Color.White.copy(alpha = 0.16f)
    val glassBorder = Color.White.copy(alpha = 0.22f)
    val glassHighlight = Color.White.copy(alpha = 0.35f)

    val textPrimary = Color.White
    val textSecondary = Color.White.copy(alpha = 0.62f)
    val textTertiary = Color.White.copy(alpha = 0.42f)

    val iosBlue = Color(0xFF0A84FF)
    val iosGreen = Color(0xFF30D158)
    val iosRed = Color(0xFFFF453A)
    val iosOrange = Color(0xFFFF9F0A)
    val iosYellow = Color(0xFFFFD60A)

    val connectedGlow = Color(0xFF30D158)
    val disconnectedGray = Color.White.copy(alpha = 0.28f)

    fun backgroundBrush() = Brush.verticalGradient(listOf(bgTop, bgBottom))

    fun auroraBrush() = Brush.radialGradient(
        colors = listOf(auroraBlue.copy(alpha = 0.55f), Color.Transparent),
    )

    fun glassBrush() = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.16f),
            Color.White.copy(alpha = 0.06f)
        )
    )

    fun powerButtonRing(connected: Boolean) = Brush.sweepGradient(
        colors = if (connected)
            listOf(iosGreen, iosGreen.copy(alpha = 0.4f), iosGreen)
        else
            listOf(glassBorder, glassWhite, glassBorder)
    )
}

fun pingColor(pingMs: Int): Color = when {
    pingMs < 0 -> GlassColors.textTertiary
    pingMs < 80 -> GlassColors.iosGreen
    pingMs < 180 -> GlassColors.iosYellow
    else -> GlassColors.iosOrange
}
