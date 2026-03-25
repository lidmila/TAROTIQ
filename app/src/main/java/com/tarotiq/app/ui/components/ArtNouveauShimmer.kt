package com.tarotiq.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tarotiq.app.ui.theme.AstralPurple
import com.tarotiq.app.ui.theme.SurfaceContainerCA

/**
 * Art Nouveau shimmer -- radial gold glow that pulses from center
 * instead of the standard diagonal sweep.
 */
@Composable
fun ArtNouveauShimmer(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val glowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_progress"
    )

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(SurfaceContainerCA, shape)
            .drawBehind {
                val radius = size.minDimension * (0.3f + glowProgress * 0.7f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AstralPurple.copy(alpha = 0.08f + glowProgress * 0.06f),
                            AstralPurple.copy(alpha = 0.02f),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = radius
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = radius
                )
            }
    )
}

/**
 * Shimmer placeholder that mimics a list of loading items.
 */
@Composable
fun ShimmerList(
    itemCount: Int = 3,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 80.dp
) {
    Column(modifier = modifier.padding(16.dp)) {
        repeat(itemCount) {
            ArtNouveauShimmer(
                modifier = Modifier.fillMaxWidth(),
                height = itemHeight
            )
            if (it < itemCount - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
