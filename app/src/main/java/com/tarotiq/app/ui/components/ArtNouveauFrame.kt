package com.tarotiq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.tarotiq.app.ui.theme.AstralPurple
import com.tarotiq.app.ui.theme.CosmicMid
import com.tarotiq.app.ui.theme.VoidBlack

enum class FrameStyle {
    SIMPLE,   // Subtle glass card
    ORNATE,   // Glass card with gold glow border
    ARCH      // Glass card with top arch shape + glow
}

private val simpleShape = RoundedCornerShape(12.dp)    // rounded-xl
private val ornateShape = RoundedCornerShape(16.dp)    // rounded-2xl
private val archRoundedShape = RoundedCornerShape(20.dp) // arch fallback for border

private val subtleBorderColor = Color.White.copy(alpha = 0.05f)

@Composable
fun ArtNouveauFrame(
    modifier: Modifier = Modifier,
    frameStyle: FrameStyle = FrameStyle.SIMPLE,
    backgroundColor: Color = CosmicMid.copy(alpha = 0.3f),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = when (frameStyle) {
        FrameStyle.SIMPLE -> simpleShape
        FrameStyle.ORNATE -> ornateShape
        FrameStyle.ARCH -> ArchShape
    }
    val borderShape = when (frameStyle) {
        FrameStyle.SIMPLE -> simpleShape
        FrameStyle.ORNATE -> ornateShape
        FrameStyle.ARCH -> archRoundedShape
    }
    val glowColor = when (frameStyle) {
        FrameStyle.SIMPLE -> AstralPurple.copy(alpha = 0.06f)
        FrameStyle.ORNATE -> AstralPurple.copy(alpha = 0.08f)
        FrameStyle.ARCH -> AstralPurple.copy(alpha = 0.10f)
    }
    val shadowElevation = when (frameStyle) {
        FrameStyle.SIMPLE -> 8.dp
        FrameStyle.ORNATE -> 16.dp
        FrameStyle.ARCH -> 20.dp
    }
    val cornerRadiusPx = when (frameStyle) {
        FrameStyle.SIMPLE -> 12f
        FrameStyle.ORNATE -> 16f
        FrameStyle.ARCH -> 20f
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = AstralPurple.copy(alpha = 0.05f),
                spotColor = VoidBlack.copy(alpha = 0.5f)
            )
            .clip(shape)
            .border(width = 1.dp, color = subtleBorderColor, shape = borderShape)
            .background(color = backgroundColor, shape = shape)
            .drawBehind {
                // Inner glow — ambient celestial light
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(glowColor, Color.Transparent),
                        startY = 0f,
                        endY = size.height * 0.4f
                    ),
                    cornerRadius = CornerRadius(cornerRadiusPx)
                )
            },
        content = content
    )
}

@Composable
fun ArtNouveauFrame(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    frameStyle: FrameStyle = FrameStyle.SIMPLE,
    backgroundColor: Color = CosmicMid.copy(alpha = 0.3f),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = when (frameStyle) {
        FrameStyle.SIMPLE -> simpleShape
        FrameStyle.ORNATE -> ornateShape
        FrameStyle.ARCH -> ArchShape
    }
    val borderShape = when (frameStyle) {
        FrameStyle.SIMPLE -> simpleShape
        FrameStyle.ORNATE -> ornateShape
        FrameStyle.ARCH -> archRoundedShape
    }
    val glowColor = when (frameStyle) {
        FrameStyle.SIMPLE -> AstralPurple.copy(alpha = 0.06f)
        FrameStyle.ORNATE -> AstralPurple.copy(alpha = 0.08f)
        FrameStyle.ARCH -> AstralPurple.copy(alpha = 0.10f)
    }
    val shadowElevation = when (frameStyle) {
        FrameStyle.SIMPLE -> 8.dp
        FrameStyle.ORNATE -> 16.dp
        FrameStyle.ARCH -> 20.dp
    }
    val cornerRadiusPx = when (frameStyle) {
        FrameStyle.SIMPLE -> 12f
        FrameStyle.ORNATE -> 16f
        FrameStyle.ARCH -> 20f
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = AstralPurple.copy(alpha = 0.05f),
                spotColor = VoidBlack.copy(alpha = 0.5f)
            )
            .clip(shape)
            .clickable(onClick = onClick)
            .border(width = 1.dp, color = subtleBorderColor, shape = borderShape)
            .background(color = backgroundColor, shape = shape)
            .drawBehind {
                // Inner glow — ambient celestial light
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(glowColor, Color.Transparent),
                        startY = 0f,
                        endY = size.height * 0.4f
                    ),
                    cornerRadius = CornerRadius(cornerRadiusPx)
                )
            },
        content = content
    )
}

private object ArchShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val archHeight = size.width * 0.08f
        val r = with(density) { 20.dp.toPx() }
        val path = Path().apply {
            moveTo(0f, size.height - r)
            quadraticBezierTo(0f, size.height, r, size.height)
            lineTo(size.width - r, size.height)
            quadraticBezierTo(size.width, size.height, size.width, size.height - r)
            lineTo(size.width, archHeight)
            quadraticBezierTo(size.width * 0.5f, -archHeight * 0.5f, 0f, archHeight)
            close()
        }
        return Outline.Generic(path)
    }
}
