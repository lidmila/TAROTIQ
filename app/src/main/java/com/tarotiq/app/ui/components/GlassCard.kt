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
import androidx.compose.ui.unit.dp
import com.tarotiq.app.ui.theme.AstralPurple
import com.tarotiq.app.ui.theme.CosmicMid

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = CosmicMid.copy(alpha = 0.6f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color = backgroundColor, shape = shape)
            .border(1.dp, AstralPurple.copy(alpha = 0.1f), shape),
        content = content
    )
}

@Composable
fun GlassCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = CosmicMid.copy(alpha = 0.6f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .background(color = backgroundColor, shape = shape)
            .border(1.dp, AstralPurple.copy(alpha = 0.1f), shape),
        content = content
    )
}
