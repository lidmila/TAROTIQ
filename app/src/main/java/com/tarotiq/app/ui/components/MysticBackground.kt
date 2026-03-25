package com.tarotiq.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.tarotiq.app.ui.theme.AstralPurple
import com.tarotiq.app.ui.theme.CelestialGold
import com.tarotiq.app.ui.theme.CosmicDeep
import com.tarotiq.app.ui.theme.CosmicMid
import com.tarotiq.app.ui.theme.NebulaGradientStart
import com.tarotiq.app.ui.theme.VoidBlack
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun MysticBackground(modifier: Modifier = Modifier, showFog: Boolean = true) {
    val transition = rememberInfiniteTransition(label = "ritual_bg")

    // Slow candlelight breathing
    val glowBreath by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_breath"
    )

    // Very slow mandala rotation
    val mandalaRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(120000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mandala_rot"
    )

    // Stitch: star drift animation (slow upward movement, 120s cycle)
    val starDrift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(120000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star_drift"
    )

    // Pre-generate star positions
    val stars = remember {
        List(60) {
            StarParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 2f + 0.5f,
                alpha = Random.nextFloat() * 0.4f + 0.1f,
                isPrimary = Random.nextFloat() < 0.08f // ~8% are primary-colored
            )
        }
    }

    val backgroundGradient = remember {
        Brush.verticalGradient(
            colors = listOf(
                VoidBlack,
                CosmicDeep,
                CosmicMid.copy(alpha = 0.6f),
                CosmicDeep,
                VoidBlack
            )
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Layer 1: Background gradient
            drawRect(brush = backgroundGradient)

            // Layer 1.5: Stitch star particles (slow upward drift)
            stars.forEach { star ->
                val yPos = ((star.y * h) - starDrift * star.size * 0.3f) % h
                val adjustedY = if (yPos < 0) yPos + h else yPos
                val color = if (star.isPrimary) AstralPurple.copy(alpha = star.alpha)
                    else Color.White.copy(alpha = star.alpha)
                drawCircle(
                    color = color,
                    radius = star.size,
                    center = Offset(star.x * w, adjustedY)
                )
            }

            // Layer 1.7: Stitch sacred geometry diamond pattern (3% opacity)
            drawSacredDiamondPattern(w, h, alpha = 0.03f)

            // Layer 2: Sacred geometry mandala (very faint)
            drawSacredGeometry(
                cx = w * 0.5f,
                cy = h * 0.35f,
                radius = w * 0.45f,
                rotation = mandalaRotation,
                alpha = 0.03f + glowBreath * 0.015f
            )

            // Layer 3: Second smaller mandala lower
            drawSacredGeometry(
                cx = w * 0.5f,
                cy = h * 0.75f,
                radius = w * 0.3f,
                rotation = -mandalaRotation * 0.7f,
                alpha = 0.02f + glowBreath * 0.01f
            )

            // Layer 4: Nebula glow spots (lavender + subtle purple)
            // Top center nebula glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AstralPurple.copy(alpha = 0.05f + glowBreath * 0.03f),
                        NebulaGradientStart.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.15f),
                    radius = w * 0.6f
                ),
                center = Offset(w * 0.5f, h * 0.15f),
                radius = w * 0.6f
            )

            // Bottom nebula glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NebulaGradientStart.copy(alpha = 0.06f + (1f - glowBreath) * 0.03f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.85f),
                    radius = w * 0.5f
                ),
                center = Offset(w * 0.5f, h * 0.85f),
                radius = w * 0.5f
            )

            // Side ethereal glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AstralPurple.copy(alpha = 0.02f + glowBreath * 0.015f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.15f, h * 0.5f),
                    radius = w * 0.35f
                ),
                center = Offset(w * 0.15f, h * 0.5f),
                radius = w * 0.35f
            )
        }

        // Layer 5: Warm fog overlay
        if (showFog) {
            MysticFogOverlay(modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * Draws a sacred geometry pattern -- concentric circles with radiating lines
 * and small celestial symbols. Very faint, like embossed parchment.
 */
private fun DrawScope.drawSacredGeometry(
    cx: Float, cy: Float, radius: Float,
    rotation: Float, alpha: Float
) {
    val color = AstralPurple.copy(alpha = alpha)
    val strokeW = 0.8f
    val rotRad = rotation * PI.toFloat() / 180f

    // Concentric circles (3 rings)
    for (i in 1..3) {
        val r = radius * (i.toFloat() / 3f)
        drawCircle(
            color = color,
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = strokeW)
        )
    }

    // Radiating lines (12, like clock hours)
    for (i in 0 until 12) {
        val angle = rotRad + (i * 30f) * PI.toFloat() / 180f
        val innerR = radius * 0.15f
        val outerR = radius * 0.95f
        drawLine(
            color = color.copy(alpha = alpha * 0.7f),
            start = Offset(cx + cos(angle) * innerR, cy + sin(angle) * innerR),
            end = Offset(cx + cos(angle) * outerR, cy + sin(angle) * outerR),
            strokeWidth = strokeW * 0.5f
        )
    }

    // Small dots at intersections (24 points on outer ring)
    for (i in 0 until 24) {
        val angle = rotRad + (i * 15f) * PI.toFloat() / 180f
        val r = radius * 0.98f
        drawCircle(
            color = color,
            radius = 1.5f,
            center = Offset(cx + cos(angle) * r, cy + sin(angle) * r)
        )
    }

    // Inner star pattern (6-pointed)
    for (i in 0 until 6) {
        val angle1 = rotRad + (i * 60f) * PI.toFloat() / 180f
        val angle2 = rotRad + ((i + 2) * 60f) * PI.toFloat() / 180f
        val r = radius * 0.5f
        drawLine(
            color = color.copy(alpha = alpha * 0.5f),
            start = Offset(cx + cos(angle1) * r, cy + sin(angle1) * r),
            end = Offset(cx + cos(angle2) * r, cy + sin(angle2) * r),
            strokeWidth = strokeW * 0.6f
        )
    }

    // Small sun/moon symbols at cardinal points
    for (i in 0 until 4) {
        val angle = rotRad + (i * 90f) * PI.toFloat() / 180f
        val r = radius * 0.75f
        val sx = cx + cos(angle) * r
        val sy = cy + sin(angle) * r
        // Small crescent
        drawCircle(color = color, radius = 3f, center = Offset(sx, sy), style = Stroke(strokeW))
        drawCircle(color = color, radius = 1.5f, center = Offset(sx + 1.5f, sy - 1f))
    }
}

/**
 * Stitch AI sacred geometry: diamond grid pattern at very low opacity.
 * Matches the SVG pattern: 60x60 diamonds rotated 45deg.
 */
private fun DrawScope.drawSacredDiamondPattern(w: Float, h: Float, alpha: Float) {
    val color = AstralPurple.copy(alpha = alpha)
    val cellSize = 60f
    val halfCell = cellSize / 2f

    var x = 0f
    while (x < w + cellSize) {
        var y = 0f
        while (y < h + cellSize) {
            val cx = x + halfCell
            val cy = y + halfCell
            // Diamond shape (rotated square)
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx, cy - halfCell)
                lineTo(cx + halfCell, cy)
                lineTo(cx, cy + halfCell)
                lineTo(cx - halfCell, cy)
                close()
            }
            drawPath(path, color)
            y += cellSize
        }
        x += cellSize
    }
}

private data class StarParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float,
    val isPrimary: Boolean
)
