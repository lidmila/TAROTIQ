package com.tarotiq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tarotiq.app.ui.theme.GlassBorder
import com.tarotiq.app.ui.theme.GlassWhite
import com.tarotiq.app.ui.theme.SurfaceDark

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = GlassWhite,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color = SurfaceDark.copy(alpha = 0.75f), shape = shape)
            .background(color = backgroundColor, shape = shape)
            .border(width = borderWidth, color = borderColor, shape = shape),
        content = content
    )
}

@Composable
fun GlassCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = GlassWhite,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .background(color = SurfaceDark.copy(alpha = 0.75f), shape = shape)
            .background(color = backgroundColor, shape = shape)
            .border(width = borderWidth, color = borderColor, shape = shape),
        content = content
    )
}
