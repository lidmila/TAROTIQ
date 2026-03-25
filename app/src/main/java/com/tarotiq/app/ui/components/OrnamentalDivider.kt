package com.tarotiq.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tarotiq.app.ui.theme.OutlineColor

@Composable
fun OrnamentalDivider(
    modifier: Modifier = Modifier,
    color: Color = OutlineColor.copy(alpha = 0.15f),
    height: Dp = 20.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val cy = size.height / 2f
        val cx = size.width / 2f
        val strokeW = 1f

        // Left line
        drawLine(
            color = color,
            start = Offset(size.width * 0.1f, cy),
            end = Offset(cx - 16f, cy),
            strokeWidth = strokeW
        )

        // Right line
        drawLine(
            color = color,
            start = Offset(cx + 16f, cy),
            end = Offset(size.width * 0.9f, cy),
            strokeWidth = strokeW
        )

        // Central diamond ornament
        drawCentralOrnament(cx, cy, color, strokeW)
    }
}

private fun DrawScope.drawCentralOrnament(cx: Float, cy: Float, color: Color, sw: Float) {
    val s = 8f
    val diamond = Path().apply {
        moveTo(cx, cy - s)
        lineTo(cx + s, cy)
        lineTo(cx, cy + s)
        lineTo(cx - s, cy)
        close()
    }
    drawPath(diamond, color = color, style = Stroke(sw))

    // Inner dot
    drawCircle(color = color, radius = 1.5f, center = Offset(cx, cy))

    // Small curves emanating from diamond
    val curve1 = Path().apply {
        moveTo(cx - s - 2f, cy)
        cubicTo(cx - s - 5f, cy - 4f, cx - s - 8f, cy - 3f, cx - s - 10f, cy)
    }
    val curve2 = Path().apply {
        moveTo(cx + s + 2f, cy)
        cubicTo(cx + s + 5f, cy - 4f, cx + s + 8f, cy - 3f, cx + s + 10f, cy)
    }
    drawPath(curve1, color = color.copy(alpha = 0.6f), style = Stroke(sw))
    drawPath(curve2, color = color.copy(alpha = 0.6f), style = Stroke(sw))
}
