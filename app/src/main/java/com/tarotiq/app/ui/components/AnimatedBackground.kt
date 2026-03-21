package com.tarotiq.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.tarotiq.app.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

private data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val baseAlpha: Float,
    val pulseSpeed: Int,
    val pulseOffset: Float
)

private data class ShootingStar(
    val startX: Float,
    val startY: Float,
    val angle: Float,
    val length: Float,
    val speed: Int,
    val delay: Int
)

/**
 * Animated starfield background with pulsing stars, nebula gradients, and shooting stars.
 * Adapted for TAROTIQ mystic purple/gold theme.
 */
@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    starCount: Int = 65,
    showNebula: Boolean = true,
    showShootingStars: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "starfield")

    // Master time for star pulsing
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Nebula color shift
    val nebulaPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "nebula"
    )

    // Shooting star cycle (8-15 second intervals)
    val shootingStarPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shooting"
    )

    // Generate stars once
    val stars = remember {
        List(starCount) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 1.8f + 0.3f,
                baseAlpha = Random.nextFloat() * 0.5f + 0.3f,
                pulseSpeed = Random.nextInt(3000, 8000),
                pulseOffset = Random.nextFloat() * 2f * PI.toFloat()
            )
        }
    }

    // Generate shooting star paths
    val shootingStars = remember {
        List(3) {
            ShootingStar(
                startX = Random.nextFloat() * 0.8f + 0.1f,
                startY = Random.nextFloat() * 0.3f,
                angle = Random.nextFloat() * 30f + 15f,
                length = Random.nextFloat() * 0.15f + 0.1f,
                speed = Random.nextInt(600, 1200),
                delay = it * 4000 + Random.nextInt(0, 3000)
            )
        }
    }

    // TAROTIQ color palette
    val bgTop = MidnightBg1
    val bgMid = MidnightBg2
    val bgBot = MidnightBg3
    val starColor = StarWhite
    val nebBlue = NebulaBlue
    val nebPurple = NebulaPurple
    val purpleG = PurpleGlow
    val goldG = GoldGlow
    val candleG = CandleFlicker

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Deep midnight gradient background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(bgTop, bgMid, bgBot),
                startY = 0f,
                endY = h
            )
        )

        // Nebula layers
        if (showNebula) {
            drawNebula(w, h, nebulaPhase, purpleG, goldG, candleG, nebBlue, nebPurple)
        }

        // Stars with pulsing
        stars.forEach { star ->
            val pulse = sin(time * 2f * PI.toFloat() * (10000f / star.pulseSpeed) + star.pulseOffset)
            val alpha = (star.baseAlpha + pulse * 0.25f).coerceIn(0.05f, 1f)

            // Star core
            drawCircle(
                color = starColor.copy(alpha = alpha),
                radius = star.radius * density,
                center = Offset(star.x * w, star.y * h)
            )

            // Star glow (larger, more transparent)
            if (star.radius > 1f) {
                drawCircle(
                    color = starColor.copy(alpha = alpha * 0.3f),
                    radius = star.radius * density * 2.5f,
                    center = Offset(star.x * w, star.y * h)
                )
            }
        }

        // Shooting stars
        if (showShootingStars) {
            shootingStars.forEach { ss ->
                drawShootingStar(ss, shootingStarPhase, w, h, starColor)
            }
        }
    }
}

private fun DrawScope.drawNebula(
    w: Float, h: Float, phase: Float,
    purple: Color, gold: Color, candle: Color,
    nebBlue: Color, nebPurple: Color
) {
    val rad = phase * PI.toFloat() / 180f

    // Large nebula blobs - very subtle
    val cx1 = w * (0.3f + sin(rad * 0.5f) * 0.15f)
    val cy1 = h * (0.25f + cos(rad * 0.3f) * 0.1f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                purple.copy(alpha = 0.06f),
                nebBlue.copy(alpha = 0.03f),
                Color.Transparent
            ),
            center = Offset(cx1, cy1),
            radius = w * 0.5f
        ),
        radius = w * 0.5f,
        center = Offset(cx1, cy1)
    )

    val cx2 = w * (0.7f + cos(rad * 0.4f) * 0.15f)
    val cy2 = h * (0.6f + sin(rad * 0.35f) * 0.1f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                gold.copy(alpha = 0.05f),
                nebPurple.copy(alpha = 0.025f),
                Color.Transparent
            ),
            center = Offset(cx2, cy2),
            radius = w * 0.45f
        ),
        radius = w * 0.45f,
        center = Offset(cx2, cy2)
    )

    val cx3 = w * (0.5f + sin(rad * 0.6f + 2f) * 0.2f)
    val cy3 = h * (0.8f + cos(rad * 0.25f) * 0.1f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                candle.copy(alpha = 0.035f),
                Color.Transparent
            ),
            center = Offset(cx3, cy3),
            radius = w * 0.35f
        ),
        radius = w * 0.35f,
        center = Offset(cx3, cy3)
    )
}

private fun DrawScope.drawShootingStar(
    ss: ShootingStar,
    phase: Float,
    w: Float,
    h: Float,
    color: Color
) {
    // Calculate shooting star progress within its cycle
    val cycleMs = 12000f
    val starDuration = ss.speed.toFloat()
    val startFraction = ss.delay / cycleMs
    val endFraction = (ss.delay + starDuration) / cycleMs

    if (phase < startFraction || phase > endFraction) return

    val progress = ((phase - startFraction) / (endFraction - startFraction)).coerceIn(0f, 1f)

    val angleRad = ss.angle * PI.toFloat() / 180f
    val trailLength = ss.length * w

    val headX = ss.startX * w + cos(angleRad) * trailLength * progress
    val headY = ss.startY * h + sin(angleRad) * trailLength * progress

    val tailProgress = (progress - 0.3f).coerceAtLeast(0f) / 0.7f
    val tailX = ss.startX * w + cos(angleRad) * trailLength * tailProgress
    val tailY = ss.startY * h + sin(angleRad) * trailLength * tailProgress

    val alpha = if (progress > 0.7f) (1f - progress) / 0.3f else 1f

    // Draw trail
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                color.copy(alpha = alpha * 0.6f)
            ),
            start = Offset(tailX, tailY),
            end = Offset(headX, headY)
        ),
        start = Offset(tailX, tailY),
        end = Offset(headX, headY),
        strokeWidth = 1.5f * density
    )

    // Draw head
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = 1.5f * density,
        center = Offset(headX, headY)
    )
}
