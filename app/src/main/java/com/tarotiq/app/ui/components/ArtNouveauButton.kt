package com.tarotiq.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarotiq.app.ui.theme.AstralPurple
import com.tarotiq.app.ui.theme.CelestialGold
import com.tarotiq.app.ui.theme.CosmicDeep
import com.tarotiq.app.ui.theme.GlassBorder
import com.tarotiq.app.ui.theme.OnPrimaryContainerCA
import com.tarotiq.app.ui.theme.OnPrimaryFixedVariantCA
import com.tarotiq.app.ui.theme.PrimaryContainerCA
import com.tarotiq.app.ui.theme.SpaceGroteskFamily
import com.tarotiq.app.ui.theme.StarWhite
import kotlin.math.cos
import kotlin.math.sin

enum class ButtonVariant {
    PRIMARY,
    SECONDARY
}

/**
 * Clickable ArtNouveauButton with onClick handler.
 *
 * PRIMARY  = btn-magical  (gradient purple, glow shadow)
 * SECONDARY = btn-grimoire (transparent, gold border)
 */
@Composable
fun ArtNouveauButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    textStyle: TextStyle? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
) {
    var isPressed by remember { mutableStateOf(false) }

    // -- Charge energy: glow roste pri drzeni --
    val chargeLevel = remember { Animatable(0f) }

    LaunchedEffect(isPressed) {
        if (isPressed && enabled) {
            chargeLevel.animateTo(
                targetValue = 1f,
                animationSpec = tween(800, easing = LinearEasing)
            )
        } else {
            chargeLevel.animateTo(
                targetValue = 0f,
                animationSpec = tween(200)
            )
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = tween(80),
        label = "scale"
    )

    val charge = chargeLevel.value
    val shape = RoundedCornerShape(50) // pill shape

    val bgBrush = when {
        !enabled -> Brush.linearGradient(listOf(CosmicDeep, CosmicDeep))
        variant == ButtonVariant.PRIMARY -> Brush.horizontalGradient(
            listOf(PrimaryContainerCA, OnPrimaryFixedVariantCA)
        )
        else -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }

    val borderColor = when {
        !enabled -> GlassBorder.copy(alpha = 0.3f)
        variant == ButtonVariant.PRIMARY -> AstralPurple.copy(alpha = 0.3f + charge * 0.2f)
        else -> CelestialGold.copy(alpha = 1f)
    }

    val textColor = when {
        !enabled -> StarWhite.copy(alpha = 0.3f)
        variant == ButtonVariant.PRIMARY -> OnPrimaryContainerCA
        else -> AstralPurple
    }

    val glowColor = when (variant) {
        ButtonVariant.PRIMARY -> PrimaryContainerCA
        ButtonVariant.SECONDARY -> CelestialGold
    }
    val baseGlowAlpha = when (variant) {
        ButtonVariant.PRIMARY -> 0.4f
        ButtonVariant.SECONDARY -> 0.2f
    }
    val glowElevation = if (enabled) (8f + charge * 20f).dp else 0.dp

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                glowElevation,
                shape,
                ambientColor = glowColor.copy(alpha = baseGlowAlpha + charge * 0.2f)
            )
            .clip(shape)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
            .defaultMinSize(minHeight = 48.dp)
            .drawWithContent {
                val cornerRadius = CornerRadius(size.height / 2f)

                // 1. Background gradient
                drawRoundRect(
                    brush = bgBrush,
                    cornerRadius = cornerRadius
                )

                // 2. Border
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = cornerRadius,
                    style = Stroke(
                        width = if (enabled) (1f + charge * 0.5f).dp.toPx() else 1.dp.toPx()
                    )
                )

                // 3. Inner shine (PRIMARY only)
                if (enabled && variant == ButtonVariant.PRIMARY) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = size.height * 0.45f
                        ),
                        cornerRadius = cornerRadius
                    )
                }

                // 4. Energy glow (while pressing)
                if (charge > 0.01f) {
                    drawRoundRect(
                        color = glowColor.copy(alpha = charge * 0.35f),
                        cornerRadius = cornerRadius
                    )
                    val particleCount = 6
                    val radius = size.minDimension * 0.6f * charge
                    for (i in 0 until particleCount) {
                        val angle =
                            (i.toFloat() / particleCount) * 6.2831f + charge * 12f
                        val px = center.x + cos(angle) * radius
                        val py = center.y + sin(angle) * radius * 0.4f
                        drawCircle(
                            color = glowColor.copy(alpha = charge * 0.6f),
                            radius = 2f + charge * 3f,
                            center = Offset(px, py)
                        )
                    }
                    drawRoundRect(
                        color = glowColor.copy(alpha = charge * 0.2f),
                        cornerRadius = cornerRadius,
                        style = Stroke(width = charge * 3f)
                    )
                }

                // 5. Content (Text) — drawn LAST = on top
                drawContent()
            }
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = textStyle ?: TextStyle(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            ),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Non-clickable ArtNouveauButton (display-only / externally-handled click).
 */
@Composable
fun ArtNouveauButton(
    text: String,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    textStyle: TextStyle? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
) {
    val shape = RoundedCornerShape(50)

    val bgBrush = when {
        !enabled -> Brush.linearGradient(listOf(CosmicDeep, CosmicDeep))
        variant == ButtonVariant.PRIMARY -> Brush.horizontalGradient(
            listOf(PrimaryContainerCA, OnPrimaryFixedVariantCA)
        )
        else -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }

    val borderColor = when {
        !enabled -> GlassBorder.copy(alpha = 0.3f)
        variant == ButtonVariant.PRIMARY -> AstralPurple.copy(alpha = 0.3f)
        else -> CelestialGold.copy(alpha = 1f)
    }

    val textColor = when {
        !enabled -> StarWhite.copy(alpha = 0.3f)
        variant == ButtonVariant.PRIMARY -> OnPrimaryContainerCA
        else -> AstralPurple
    }

    val glowColor = when (variant) {
        ButtonVariant.PRIMARY -> PrimaryContainerCA
        ButtonVariant.SECONDARY -> CelestialGold
    }
    val baseGlowAlpha = when (variant) {
        ButtonVariant.PRIMARY -> 0.4f
        ButtonVariant.SECONDARY -> 0.2f
    }
    val glowElevation = if (enabled) 8.dp else 0.dp

    Box(
        modifier = modifier
            .shadow(
                glowElevation,
                shape,
                ambientColor = glowColor.copy(alpha = baseGlowAlpha)
            )
            .clip(shape)
            .defaultMinSize(minHeight = 48.dp)
            .drawWithContent {
                val cornerRadius = CornerRadius(size.height / 2f)

                // 1. Background gradient
                drawRoundRect(
                    brush = bgBrush,
                    cornerRadius = cornerRadius
                )

                // 2. Border
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = cornerRadius,
                    style = Stroke(width = 1.dp.toPx())
                )

                // 3. Inner shine (PRIMARY only)
                if (enabled && variant == ButtonVariant.PRIMARY) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = size.height * 0.45f
                        ),
                        cornerRadius = cornerRadius
                    )
                }

                // 4. Content (Text) — drawn LAST = on top
                drawContent()
            }
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = textStyle ?: TextStyle(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            ),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
