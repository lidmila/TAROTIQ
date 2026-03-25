package com.tarotiq.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tarotiq.app.ui.theme.AstralPurple
import com.tarotiq.app.ui.theme.AstralPurpleDim
import com.tarotiq.app.ui.theme.CelestialGold
import com.tarotiq.app.ui.theme.CelestialGoldLight

/**
 * Returns a 45-degree linear gradient brush with celestial gold tones.
 * Matches Stitch's gold foil effect.
 */
fun goldFoilBrush(): Brush = Brush.linearGradient(
    colors = listOf(CelestialGold, CelestialGoldLight, CelestialGold),
    start = Offset(0f, Float.POSITIVE_INFINITY),
    end = Offset(Float.POSITIVE_INFINITY, 0f)
)

/**
 * Animated gold gradient border that shimmers continuously.
 * Matches Stitch's shimmer-border effect.
 */
fun Modifier.shimmerBorder(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer_border")
    val animatedOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_border_offset"
    )

    val size = 2000f
    val offset = animatedOffset * size

    val brush = Brush.linearGradient(
        colors = listOf(
            CelestialGold,
            CelestialGoldLight,
            CelestialGold,
            CelestialGoldLight
        ),
        start = Offset(offset, offset),
        end = Offset(offset + size * 0.5f, offset + size * 0.5f)
    )

    this.border(2.dp, brush, RoundedCornerShape(12.dp))
}

/**
 * Animated shimmer text — gold-to-purple gradient that scrolls across the text.
 * Matches Stitch's `text-shimmer` CSS class.
 */
@Composable
fun ShimmerText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            CelestialGold,
            AstralPurple,
            CelestialGold
        ),
        start = Offset(shimmerOffset - 1000f, 0f),
        end = Offset(shimmerOffset, 0f)
    )

    Text(
        text = text,
        style = style.copy(brush = shimmerBrush),
        modifier = modifier,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

/**
 * Gold glow shadow modifier.
 * Matches Stitch's `card-glow` and `gold-glow` CSS classes.
 */
fun Modifier.goldGlow(
    elevation: Float = 25f,
    alpha: Float = 0.15f
): Modifier = this.shadow(
    elevation = elevation.dp,
    ambientColor = CelestialGold.copy(alpha = alpha),
    spotColor = Color.Transparent
)

/**
 * Primary (lavender) glow shadow modifier.
 * Matches Stitch's `card-glow` with primary color.
 */
fun Modifier.primaryGlow(
    elevation: Float = 20f,
    alpha: Float = 0.08f
): Modifier = this.shadow(
    elevation = elevation.dp,
    ambientColor = AstralPurple.copy(alpha = alpha),
    spotColor = Color.Transparent
)

/**
 * Float animation modifier — element slowly bobs up and down.
 * Matches Stitch's `animate-float` keyframes.
 */
@Composable
fun Modifier.floatAnimation(
    durationMs: Int = 6000,
    amplitude: Float = 10f
): Modifier {
    val transition = rememberInfiniteTransition(label = "float")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )
    return this.graphicsLayer {
        translationY = -amplitude * offset
    }
}

/**
 * Breathing scale animation — element gently pulses.
 * Matches Stitch's `animate-breathe` keyframes.
 */
@Composable
fun Modifier.breatheAnimation(
    durationMs: Int = 4000,
    scaleRange: Float = 0.03f
): Modifier {
    val transition = rememberInfiniteTransition(label = "breathe")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f + scaleRange,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe_scale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
