package com.tarotiq.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.tarotiq.app.ui.theme.AstralPurple
import com.tarotiq.app.ui.theme.NightBg1
import kotlinx.coroutines.delay

/**
 * Content emerges from total darkness with a warm gold glow.
 * Phase 1 (0-600ms): Gold glow appears behind content area
 * Phase 2 (300-1200ms): Content fades in
 * Phase 3 (900-1500ms): Glow settles to subtle level
 */
@Composable
fun EmergenceFromDarkness(
    visible: Boolean,
    modifier: Modifier = Modifier,
    durationMs: Int = 1500,
    content: @Composable BoxScope.() -> Unit
) {
    val overlayAlpha = remember { Animatable(1f) }
    val glowRadius = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            // Phase 1: glow grows
            glowRadius.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = (durationMs * 0.4f).toInt(),
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    LaunchedEffect(visible) {
        if (visible) {
            delay((durationMs * 0.2f).toLong())
            // Phase 2: content appears
            contentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = (durationMs * 0.6f).toInt(),
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    LaunchedEffect(visible) {
        if (visible) {
            delay((durationMs * 0.1f).toLong())
            // Overlay fades
            overlayAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = (durationMs * 0.8f).toInt(),
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    Box(modifier = modifier) {
        // Content with animated alpha
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        Modifier.graphicsLayerAlpha(contentAlpha.value)
                    ),
                content = content
            )
        }

        // Gold glow layer
        if (glowRadius.value > 0f && overlayAlpha.value > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension * glowRadius.value
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AstralPurple.copy(alpha = 0.15f * overlayAlpha.value),
                            AstralPurple.copy(alpha = 0.05f * overlayAlpha.value),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = radius
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = radius
                )
            }
        }

        // Dark overlay
        if (overlayAlpha.value > 0.01f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = NightBg1.copy(alpha = overlayAlpha.value)
                )
            }
        }
    }
}

/**
 * Items appear one by one with a gold glow effect.
 */
@Composable
fun SequentialLightUp(
    itemCount: Int,
    delayBetween: Int = 300,
    modifier: Modifier = Modifier,
    content: @Composable (index: Int, alpha: Float) -> Unit
) {
    val alphas = remember(itemCount) {
        List(itemCount) { Animatable(0f) }
    }

    LaunchedEffect(itemCount) {
        alphas.forEachIndexed { index, animatable ->
            delay(delayBetween.toLong())
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(modifier = modifier) {
        alphas.forEachIndexed { index, animatable ->
            content(index, animatable.value)
        }
    }
}

/**
 * A gold glow reveal -- content fades in with a gold aura behind it.
 */
@Composable
fun RitualReveal(
    visible: Boolean,
    modifier: Modifier = Modifier,
    durationMs: Int = 1500,
    content: @Composable BoxScope.() -> Unit
) {
    val contentAlpha = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            // Glow appears first
            glowAlpha.animateTo(
                targetValue = 0.3f,
                animationSpec = tween((durationMs * 0.4f).toInt(), easing = FastOutSlowInEasing)
            )
            // Then settles
            glowAlpha.animateTo(
                targetValue = 0.05f,
                animationSpec = tween((durationMs * 0.4f).toInt())
            )
        }
    }

    LaunchedEffect(visible) {
        if (visible) {
            delay((durationMs * 0.2f).toLong())
            contentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween((durationMs * 0.6f).toInt(), easing = FastOutSlowInEasing)
            )
        }
    }

    Box(modifier = modifier) {
        // Glow behind
        if (glowAlpha.value > 0.01f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AstralPurple.copy(alpha = glowAlpha.value),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.minDimension * 0.6f
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.minDimension * 0.6f
                )
            }
        }

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(Modifier.graphicsLayerAlpha(contentAlpha.value)),
            content = content
        )
    }
}

// Helper extension: apply graphicsLayer alpha
private fun Modifier.graphicsLayerAlpha(a: Float): Modifier =
    graphicsLayer { alpha = a }
