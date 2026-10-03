package com.glassvpn.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.glassvpn.app.ui.theme.GlassColors

/** Full-screen Liquid Glass background with aurora blobs. */
@Composable
fun GlassScaffold(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.backgroundBrush())
    ) {
        // Aurora blobs (iOS 26 style ambient light)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GlassColors.auroraBlue.copy(alpha = 0.5f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.15f, h * 0.12f),
                    radius = w * 0.7f
                ),
                radius = w * 0.7f,
                center = Offset(w * 0.15f, h * 0.12f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GlassColors.auroraPurple.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.9f, h * 0.35f),
                    radius = w * 0.6f
                ),
                radius = w * 0.6f,
                center = Offset(w * 0.9f, h * 0.35f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GlassColors.auroraTeal.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.95f),
                    radius = w * 0.8f
                ),
                radius = w * 0.8f,
                center = Offset(w * 0.5f, h * 0.95f)
            )
        }
        content()
    }
}
