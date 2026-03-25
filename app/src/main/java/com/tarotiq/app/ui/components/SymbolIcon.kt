package com.tarotiq.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tarotiq.app.ui.theme.AntiqueGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class Symbol {
    COIN,
    STAR,
    TAROT_CARD,
    THIRD_EYE,
    HEART,
    LAUREL,
    QUESTION,
    LOTUS,
    CELTIC_CROSS,
    FLAME,
    MOON_NEW,
    MOON_WAXING,
    MOON_FULL,
    MOON_WANING,
    WAVE,
    LEAF,
    WIND,
    FIRE_ELEMENT,
    SINGLE_INSIGHT,
    TRIPLE_SPREAD,
    CELTIC_WHEEL,
    TWIN_SOULS,
    CROWN,
    SCALES,
    CARD_WITH_STAR,
    THREE_CARDS
}

@Composable
fun SymbolIcon(
    symbol: Symbol,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = AntiqueGold
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        // Thicker strokes at small sizes for readability
        val strokeW = if (s <= 80f) s * 0.08f else s * 0.05f

        when (symbol) {
            Symbol.COIN -> drawCoin(cx, cy, s, color, strokeW)
            Symbol.STAR -> drawStar(cx, cy, s, color, strokeW)
            Symbol.TAROT_CARD -> drawTarotCard(cx, cy, s, color, strokeW)
            Symbol.THIRD_EYE -> drawThirdEye(cx, cy, s, color, strokeW)
            Symbol.HEART -> drawHeart(cx, cy, s, color, strokeW)
            Symbol.LAUREL -> drawLaurel(cx, cy, s, color, strokeW)
            Symbol.QUESTION -> drawQuestion(cx, cy, s, color, strokeW)
            Symbol.LOTUS -> drawLotus(cx, cy, s, color, strokeW)
            Symbol.CELTIC_CROSS -> drawCelticCross(cx, cy, s, color, strokeW)
            Symbol.FLAME -> drawFlame(cx, cy, s, color, strokeW)
            Symbol.MOON_NEW -> drawMoonNew(cx, cy, s, color, strokeW)
            Symbol.MOON_WAXING -> drawMoonWaxing(cx, cy, s, color, strokeW)
            Symbol.MOON_FULL -> drawMoonFull(cx, cy, s, color, strokeW)
            Symbol.MOON_WANING -> drawMoonWaning(cx, cy, s, color, strokeW)
            Symbol.WAVE -> drawWave(cx, cy, s, color, strokeW)
            Symbol.LEAF -> drawLeafElement(cx, cy, s, color, strokeW)
            Symbol.WIND -> drawWind(cx, cy, s, color, strokeW)
            Symbol.FIRE_ELEMENT -> drawFlame(cx, cy, s, color, strokeW)
            Symbol.SINGLE_INSIGHT -> drawSingleInsight(cx, cy, s, color, strokeW)
            Symbol.TRIPLE_SPREAD -> drawTripleSpread(cx, cy, s, color, strokeW)
            Symbol.CELTIC_WHEEL -> drawCelticWheel(cx, cy, s, color, strokeW)
            Symbol.TWIN_SOULS -> drawTwinSouls(cx, cy, s, color, strokeW)
            Symbol.CROWN -> drawCrown(cx, cy, s, color, strokeW)
            Symbol.SCALES -> drawScales(cx, cy, s, color, strokeW)
            Symbol.CARD_WITH_STAR -> drawCardWithStar(cx, cy, s, color, strokeW)
            Symbol.THREE_CARDS -> drawThreeCards(cx, cy, s, color, strokeW)
        }
    }
}

// --- Symbol drawing functions ---

private fun DrawScope.drawCoin(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    val r = s * 0.42f
    // Outer rim
    drawCircle(color = color, radius = r, center = Offset(cx, cy), style = Stroke(sw * 1.2f))
    // Inner rim (embossed edge)
    drawCircle(color = color, radius = r * 0.82f, center = Offset(cx, cy), style = Stroke(sw * 0.5f))
    // Center pentacle (5-pointed star) — mystical coin motif
    val starPath = Path()
    val outerStar = r * 0.55f
    val innerStar = r * 0.25f
    for (i in 0 until 10) {
        val radius = if (i % 2 == 0) outerStar else innerStar
        val angle = (i * 36f - 90f) * (PI.toFloat() / 180f)
        val x = cx + cos(angle) * radius
        val y = cy + sin(angle) * radius
        if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
    }
    starPath.close()
    drawPath(starPath, color = color, style = Stroke(sw * 0.7f))
    // Small decorative dots around inner rim (12 like a clock)
    for (i in 0 until 12) {
        val angle = (i * 30f) * (PI.toFloat() / 180f)
        val dotR = r * 0.72f
        drawCircle(
            color = color,
            radius = s * 0.012f,
            center = Offset(cx + cos(angle) * dotR, cy + sin(angle) * dotR)
        )
    }
}

private fun DrawScope.drawStar(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    // Outer 6-pointed star
    val outerR = s * 0.4f
    val innerR = s * 0.2f
    val path = Path()
    for (i in 0 until 12) {
        val r = if (i % 2 == 0) outerR else innerR
        val angle = (i * 30f - 90f) * PI.toFloat() / 180f
        val x = cx + cos(angle) * r
        val y = cy + sin(angle) * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color, style = Stroke(sw))
    // Inner circle
    drawCircle(color.copy(alpha = 0.4f), radius = innerR * 0.8f, center = Offset(cx, cy), style = Stroke(sw * 0.5f))
    // Central gem
    drawCircle(color, radius = sw * 1.2f, center = Offset(cx, cy))
    // Outer halo ring
    drawCircle(color.copy(alpha = 0.2f), radius = outerR * 1.15f, center = Offset(cx, cy), style = Stroke(sw * 0.3f))
    // Tiny rays between star points
    for (i in 0 until 6) {
        val angle = (i * 60f - 60f) * PI.toFloat() / 180f
        val dotDist = outerR * 0.75f
        drawCircle(color.copy(alpha = 0.3f), radius = s * 0.01f, center = Offset(cx + cos(angle) * dotDist, cy + sin(angle) * dotDist))
    }
}

private fun DrawScope.drawTarotCard(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    val w = s * 0.36f
    val h = s * 0.50f
    val rect = Rect(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2)
    // Outer frame
    val path = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(rect, 4f, 4f)) }
    drawPath(path, color = color, style = Stroke(sw * 1.1f))
    // Inner ornamental border
    val inset = sw * 1.5f
    val innerRect = Rect(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset)
    val innerPath = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(innerRect, 2f, 2f)) }
    drawPath(innerPath, color = color.copy(alpha = 0.4f), style = Stroke(sw * 0.5f))
    // Center star (6-pointed, like Star of David)
    val starR = s * 0.1f
    val starInner = s * 0.05f
    val starPath = Path()
    for (i in 0 until 12) {
        val r = if (i % 2 == 0) starR else starInner
        val angle = (i * 30f - 90f) * (PI.toFloat() / 180f)
        val x = cx + cos(angle) * r
        val y = cy + sin(angle) * r
        if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
    }
    starPath.close()
    drawPath(starPath, color = color, style = Stroke(sw * 0.6f))
    // Corner flourishes (small dots at 4 corners of inner rect)
    val dotR = s * 0.015f
    drawCircle(color, dotR, Offset(innerRect.left + inset, innerRect.top + inset))
    drawCircle(color, dotR, Offset(innerRect.right - inset, innerRect.top + inset))
    drawCircle(color, dotR, Offset(innerRect.left + inset, innerRect.bottom - inset))
    drawCircle(color, dotR, Offset(innerRect.right - inset, innerRect.bottom - inset))
    // Decorative lines top and bottom
    drawLine(color.copy(alpha = 0.3f), Offset(cx - w * 0.25f, innerRect.top + inset * 0.5f), Offset(cx + w * 0.25f, innerRect.top + inset * 0.5f), sw * 0.4f)
    drawLine(color.copy(alpha = 0.3f), Offset(cx - w * 0.25f, innerRect.bottom - inset * 0.5f), Offset(cx + w * 0.25f, innerRect.bottom - inset * 0.5f), sw * 0.4f)
}

private fun DrawScope.drawThirdEye(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    val w = s * 0.45f
    val h = s * 0.2f
    // Eye shape
    val eyePath = Path().apply {
        moveTo(cx - w, cy)
        cubicTo(cx - w * 0.5f, cy - h, cx + w * 0.5f, cy - h, cx + w, cy)
        cubicTo(cx + w * 0.5f, cy + h, cx - w * 0.5f, cy + h, cx - w, cy)
    }
    drawPath(eyePath, color = color, style = Stroke(sw))
    // Iris
    drawCircle(color, radius = s * 0.1f, center = Offset(cx, cy), style = Stroke(sw))
    // Pupil
    drawCircle(color, radius = s * 0.04f, center = Offset(cx, cy))
    // Rays above
    for (i in -2..2) {
        val angle = (i * 20f - 90f) * PI.toFloat() / 180f
        val startR = s * 0.28f
        val endR = s * 0.38f
        drawLine(
            color.copy(alpha = 0.6f),
            Offset(cx + cos(angle) * startR, cy + sin(angle) * startR),
            Offset(cx + cos(angle) * endR, cy + sin(angle) * endR),
            sw * 0.7f
        )
    }
}

private fun DrawScope.drawHeart(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    // Outer heart
    val path = Path().apply {
        moveTo(cx, cy + s * 0.28f)
        cubicTo(cx - s * 0.48f, cy - s * 0.02f, cx - s * 0.38f, cy - s * 0.38f, cx, cy - s * 0.15f)
        cubicTo(cx + s * 0.38f, cy - s * 0.38f, cx + s * 0.48f, cy - s * 0.02f, cx, cy + s * 0.28f)
    }
    drawPath(path, color = color, style = Stroke(sw * 1.1f))
    // Inner heart (embossed)
    val innerPath = Path().apply {
        moveTo(cx, cy + s * 0.18f)
        cubicTo(cx - s * 0.32f, cy + s * 0.02f, cx - s * 0.25f, cy - s * 0.25f, cx, cy - s * 0.08f)
        cubicTo(cx + s * 0.25f, cy - s * 0.25f, cx + s * 0.32f, cy + s * 0.02f, cx, cy + s * 0.18f)
    }
    drawPath(innerPath, color = color.copy(alpha = 0.35f), style = Stroke(sw * 0.5f))
    // Radiant lines from center (love energy)
    for (i in 0 until 8) {
        val angle = (i * 45f - 90f) * (PI.toFloat() / 180f)
        val startR = s * 0.06f
        val endR = s * 0.12f
        drawLine(
            color.copy(alpha = 0.3f),
            Offset(cx + cos(angle) * startR, cy - s * 0.02f + sin(angle) * startR),
            Offset(cx + cos(angle) * endR, cy - s * 0.02f + sin(angle) * endR),
            sw * 0.5f
        )
    }
    // Vine flourishes at bottom
    val vineLeft = Path().apply {
        moveTo(cx, cy + s * 0.28f)
        cubicTo(cx - s * 0.08f, cy + s * 0.35f, cx - s * 0.15f, cy + s * 0.32f, cx - s * 0.12f, cy + s * 0.38f)
    }
    drawPath(vineLeft, color = color.copy(alpha = 0.4f), style = Stroke(sw * 0.5f))
    val vineRight = Path().apply {
        moveTo(cx, cy + s * 0.28f)
        cubicTo(cx + s * 0.08f, cy + s * 0.35f, cx + s * 0.15f, cy + s * 0.32f, cx + s * 0.12f, cy + s * 0.38f)
    }
    drawPath(vineRight, color = color.copy(alpha = 0.4f), style = Stroke(sw * 0.5f))
}

private fun DrawScope.drawLaurel(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    // Two arching branches
    for (side in listOf(-1f, 1f)) {
        val branch = Path().apply {
            moveTo(cx, cy + s * 0.35f)
            cubicTo(
                cx + side * s * 0.15f, cy + s * 0.1f,
                cx + side * s * 0.3f, cy - s * 0.1f,
                cx + side * s * 0.15f, cy - s * 0.35f
            )
        }
        drawPath(branch, color = color, style = Stroke(sw))
        // Leaves along the branch
        for (i in 0..3) {
            val t = 0.2f + i * 0.2f
            val bx = cx + side * s * 0.15f * t * 1.5f
            val by = cy + s * 0.35f - s * 0.7f * t
            val leafPath = Path().apply {
                moveTo(bx, by)
                cubicTo(bx + side * 6f, by - 4f, bx + side * 8f, by + 2f, bx + side * 4f, by + 5f)
            }
            drawPath(leafPath, color = color.copy(alpha = 0.6f), style = Stroke(sw * 0.6f))
        }
    }
}

private fun DrawScope.drawQuestion(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    val path = Path().apply {
        moveTo(cx - s * 0.12f, cy - s * 0.2f)
        cubicTo(cx - s * 0.12f, cy - s * 0.38f, cx + s * 0.18f, cy - s * 0.38f, cx + s * 0.12f, cy - s * 0.2f)
        cubicTo(cx + s * 0.08f, cy - s * 0.1f, cx, cy - s * 0.05f, cx, cy + s * 0.05f)
    }
    drawPath(path, color = color, style = Stroke(sw * 1.2f))
    // Dot
    drawCircle(color, radius = sw * 1.2f, center = Offset(cx, cy + s * 0.2f))
}

private fun DrawScope.drawLotus(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    // 5 petals
    for (i in 0..4) {
        val angle = (i * 36f - 90f + (if (i % 2 == 0) 0f else 18f))
        val spreadAngle = if (i == 0) 0f else if (i <= 2) -25f + i * 25f else -25f + (i - 2) * 25f
        rotate(spreadAngle - 90f + i * 36f, pivot = Offset(cx, cy)) {
            val petalPath = Path().apply {
                moveTo(cx, cy)
                cubicTo(cx - s * 0.1f, cy - s * 0.25f, cx + s * 0.1f, cy - s * 0.25f, cx, cy - s * 0.4f)
                cubicTo(cx + s * 0.08f, cy - s * 0.2f, cx - s * 0.08f, cy - s * 0.2f, cx, cy)
            }
            drawPath(petalPath, color = color, style = Stroke(sw * 0.8f))
        }
    }
}

private fun DrawScope.drawCelticCross(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    val arm = s * 0.35f
    val shortArm = s * 0.25f
    // Vertical bar (longer, extends below circle)
    drawLine(color, Offset(cx, cy - arm), Offset(cx, cy + arm * 1.15f), sw * 1.1f)
    // Horizontal bar (shorter)
    drawLine(color, Offset(cx - shortArm, cy - arm * 0.1f), Offset(cx + shortArm, cy - arm * 0.1f), sw * 1.1f)
    // Celtic ring/nimbus around intersection
    val ringR = s * 0.2f
    drawCircle(color, radius = ringR, center = Offset(cx, cy - arm * 0.1f), style = Stroke(sw * 0.9f))
    // Inner ring
    drawCircle(color.copy(alpha = 0.3f), radius = ringR * 0.7f, center = Offset(cx, cy - arm * 0.1f), style = Stroke(sw * 0.4f))
    // Knotwork detail — small arcs at 4 intersection points of cross and ring
    val knots = listOf(0f, 90f, 180f, 270f)
    for (angle in knots) {
        val rad = angle * (PI.toFloat() / 180f)
        val kx = cx + cos(rad) * ringR * 0.85f
        val ky = (cy - arm * 0.1f) + sin(rad) * ringR * 0.85f
        drawCircle(color.copy(alpha = 0.5f), radius = s * 0.02f, center = Offset(kx, ky))
    }
    // Decorative serifs at arm ends
    val serifLen = s * 0.04f
    // Top
    drawLine(color.copy(alpha = 0.6f), Offset(cx - serifLen, cy - arm), Offset(cx + serifLen, cy - arm), sw * 0.7f)
    // Bottom
    drawLine(color.copy(alpha = 0.6f), Offset(cx - serifLen, cy + arm * 1.15f), Offset(cx + serifLen, cy + arm * 1.15f), sw * 0.7f)
    // Left
    drawLine(color.copy(alpha = 0.6f), Offset(cx - shortArm, cy - arm * 0.1f - serifLen), Offset(cx - shortArm, cy - arm * 0.1f + serifLen), sw * 0.7f)
    // Right
    drawLine(color.copy(alpha = 0.6f), Offset(cx + shortArm, cy - arm * 0.1f - serifLen), Offset(cx + shortArm, cy - arm * 0.1f + serifLen), sw * 0.7f)
    // Center gem (filled circle)
    drawCircle(color, radius = s * 0.03f, center = Offset(cx, cy - arm * 0.1f))
}

private fun DrawScope.drawFlame(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    val path = Path().apply {
        moveTo(cx, cy + s * 0.35f)
        cubicTo(cx - s * 0.2f, cy + s * 0.1f, cx - s * 0.15f, cy - s * 0.15f, cx, cy - s * 0.35f)
        cubicTo(cx + s * 0.15f, cy - s * 0.15f, cx + s * 0.2f, cy + s * 0.1f, cx, cy + s * 0.35f)
    }
    drawPath(path, color = color, style = Stroke(sw))
    // Inner flame
    val inner = Path().apply {
        moveTo(cx, cy + s * 0.2f)
        cubicTo(cx - s * 0.08f, cy + s * 0.05f, cx - s * 0.06f, cy - s * 0.1f, cx, cy - s * 0.18f)
        cubicTo(cx + s * 0.06f, cy - s * 0.1f, cx + s * 0.08f, cy + s * 0.05f, cx, cy + s * 0.2f)
    }
    drawPath(inner, color = color.copy(alpha = 0.5f), style = Stroke(sw * 0.7f))
}

private fun DrawScope.drawMoonNew(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    drawCircle(color, radius = s * 0.3f, center = Offset(cx, cy), style = Stroke(sw))
}

private fun DrawScope.drawMoonWaxing(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    drawCircle(color, radius = s * 0.3f, center = Offset(cx, cy), style = Stroke(sw))
    // Right half filled
    val path = Path().apply {
        val r = s * 0.3f
        addArc(Rect(cx - r, cy - r, cx + r, cy + r), -90f, 180f)
        cubicTo(cx - r * 0.3f, cy + r, cx - r * 0.3f, cy - r, cx, cy - r)
    }
    drawPath(path, color = color.copy(alpha = 0.3f))
}

private fun DrawScope.drawMoonFull(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    drawCircle(color, radius = s * 0.3f, center = Offset(cx, cy), style = Stroke(sw))
    drawCircle(color.copy(alpha = 0.15f), radius = s * 0.28f, center = Offset(cx, cy))
}

private fun DrawScope.drawMoonWaning(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    drawCircle(color, radius = s * 0.3f, center = Offset(cx, cy), style = Stroke(sw))
    // Left half filled
    val path = Path().apply {
        val r = s * 0.3f
        addArc(Rect(cx - r, cy - r, cx + r, cy + r), 90f, 180f)
        cubicTo(cx + r * 0.3f, cy - r, cx + r * 0.3f, cy + r, cx, cy + r)
    }
    drawPath(path, color = color.copy(alpha = 0.3f))
}

private fun DrawScope.drawWave(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    for (i in 0..2) {
        val yOff = (i - 1) * s * 0.15f
        val path = Path().apply {
            moveTo(cx - s * 0.35f, cy + yOff)
            cubicTo(cx - s * 0.15f, cy + yOff - s * 0.1f, cx + s * 0.05f, cy + yOff + s * 0.1f, cx + s * 0.35f, cy + yOff)
        }
        drawPath(path, color = color.copy(alpha = 0.8f - i * 0.2f), style = Stroke(sw))
    }
}

private fun DrawScope.drawLeafElement(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    // Central vein
    drawLine(color, Offset(cx, cy + s * 0.35f), Offset(cx, cy - s * 0.35f), sw * 0.7f)
    // Leaf shape
    val path = Path().apply {
        moveTo(cx, cy - s * 0.35f)
        cubicTo(cx + s * 0.25f, cy - s * 0.2f, cx + s * 0.2f, cy + s * 0.15f, cx, cy + s * 0.35f)
        cubicTo(cx - s * 0.2f, cy + s * 0.15f, cx - s * 0.25f, cy - s * 0.2f, cx, cy - s * 0.35f)
    }
    drawPath(path, color = color, style = Stroke(sw))
}

private fun DrawScope.drawWind(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    for (i in 0..2) {
        val yOff = (i - 1) * s * 0.18f
        val path = Path().apply {
            moveTo(cx - s * 0.3f, cy + yOff)
            cubicTo(cx - s * 0.1f, cy + yOff, cx + s * 0.15f, cy + yOff - s * 0.08f, cx + s * 0.3f, cy + yOff + s * 0.05f)
        }
        drawPath(path, color = color, style = Stroke(sw))
        // Small curl at end
        val curl = Path().apply {
            moveTo(cx + s * 0.3f, cy + yOff + s * 0.05f)
            cubicTo(cx + s * 0.35f, cy + yOff - s * 0.02f, cx + s * 0.32f, cy + yOff - s * 0.06f, cx + s * 0.28f, cy + yOff - s * 0.03f)
        }
        drawPath(curl, color = color.copy(alpha = 0.6f), style = Stroke(sw * 0.7f))
    }
}

private fun DrawScope.drawSingleInsight(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    val w = s * 0.45f
    val h = s * 0.18f
    // Eye shape (almond)
    val eyePath = Path().apply {
        moveTo(cx - w, cy)
        cubicTo(cx - w * 0.5f, cy - h * 1.4f, cx + w * 0.5f, cy - h * 1.4f, cx + w, cy)
        cubicTo(cx + w * 0.5f, cy + h * 1.4f, cx - w * 0.5f, cy + h * 1.4f, cx - w, cy)
    }
    drawPath(eyePath, color = color, style = Stroke(sw * 1.1f))
    // Iris circle
    drawCircle(color, radius = s * 0.12f, center = Offset(cx, cy), style = Stroke(sw * 0.7f))
    // Star pupil (5-pointed)
    val pupilR = s * 0.06f
    val pupilInner = s * 0.03f
    val starPath = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) pupilR else pupilInner
        val angle = (i * 36f - 90f) * (PI.toFloat() / 180f)
        val x = cx + cos(angle) * r
        val y = cy + sin(angle) * r
        if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
    }
    starPath.close()
    drawPath(starPath, color = color)
    // 5 rays above the eye
    for (i in -2..2) {
        val angle = (i * 22f - 90f) * (PI.toFloat() / 180f)
        val startR = s * 0.26f
        val endR = s * 0.38f
        drawLine(color.copy(alpha = 0.5f),
            Offset(cx + cos(angle) * startR, cy + sin(angle) * startR),
            Offset(cx + cos(angle) * endR, cy + sin(angle) * endR), sw * 0.6f)
    }
    // Outer halo
    drawCircle(color.copy(alpha = 0.15f), radius = s * 0.42f, center = Offset(cx, cy), style = Stroke(sw * 0.3f))
}

private fun DrawScope.drawTripleSpread(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    val cardW = s * 0.22f
    val cardH = s * 0.35f
    val angles = listOf(-12f, 0f, 12f)
    val offsets = listOf(-s * 0.12f, 0f, s * 0.12f)
    for (i in angles.indices) {
        rotate(angles[i], pivot = Offset(cx + offsets[i], cy)) {
            val left = cx + offsets[i] - cardW / 2
            val top = cy - cardH / 2
            val rect = Rect(left, top, left + cardW, top + cardH)
            val path = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(rect, 3f, 3f)) }
            // Fill with very subtle alpha for depth
            drawPath(path, color = color.copy(alpha = 0.05f + i * 0.03f))
            drawPath(path, color = color.copy(alpha = 0.4f + i * 0.2f), style = Stroke(sw * (0.5f + i * 0.15f)))
        }
    }
    // Small star on front card center
    drawCircle(color, radius = s * 0.025f, center = Offset(cx, cy))
    // "III" numeral below
    for (d in listOf(-s * 0.06f, 0f, s * 0.06f)) {
        drawLine(color.copy(alpha = 0.4f), Offset(cx + d, cy + cardH / 2 + s * 0.04f), Offset(cx + d, cy + cardH / 2 + s * 0.1f), sw * 0.5f)
    }
}

private fun DrawScope.drawCelticWheel(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    val r = s * 0.38f
    // Outer knotwork ring (double line)
    drawCircle(color, radius = r, center = Offset(cx, cy), style = Stroke(sw * 1.2f))
    drawCircle(color.copy(alpha = 0.35f), radius = r * 0.88f, center = Offset(cx, cy), style = Stroke(sw * 0.4f))
    // Cross arms inside circle
    val armLen = r * 0.75f
    drawLine(color, Offset(cx, cy - armLen), Offset(cx, cy + armLen), sw * 0.9f)
    drawLine(color, Offset(cx - armLen, cy), Offset(cx + armLen, cy), sw * 0.9f)
    // 4 cardinal gems (small filled circles at ring edge)
    for (angle in listOf(0f, 90f, 180f, 270f)) {
        val rad = angle * (PI.toFloat() / 180f)
        drawCircle(color, radius = s * 0.035f, center = Offset(cx + cos(rad) * r, cy + sin(rad) * r))
    }
    // 4 diagonal decorative dots (between cardinals)
    for (angle in listOf(45f, 135f, 225f, 315f)) {
        val rad = angle * (PI.toFloat() / 180f)
        drawCircle(color.copy(alpha = 0.4f), radius = s * 0.02f, center = Offset(cx + cos(rad) * r * 0.92f, cy + sin(rad) * r * 0.92f))
    }
    // Center gem
    drawCircle(color, radius = s * 0.05f, center = Offset(cx, cy), style = Stroke(sw * 0.6f))
    drawCircle(color, radius = s * 0.02f, center = Offset(cx, cy))
}

private fun DrawScope.drawTwinSouls(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    // Left heart (smaller, shifted left)
    val leftPath = Path().apply {
        val hx = cx - s * 0.1f
        val hy = cy - s * 0.02f
        val hs = s * 0.7f
        moveTo(hx, hy + hs * 0.2f)
        cubicTo(hx - hs * 0.35f, hy - hs * 0.02f, hx - hs * 0.25f, hy - hs * 0.28f, hx, hy - hs * 0.1f)
        cubicTo(hx + hs * 0.25f, hy - hs * 0.28f, hx + hs * 0.35f, hy - hs * 0.02f, hx, hy + hs * 0.2f)
    }
    drawPath(leftPath, color = color.copy(alpha = 0.5f), style = Stroke(sw * 0.8f))
    // Right heart (smaller, shifted right, overlapping)
    val rightPath = Path().apply {
        val hx = cx + s * 0.1f
        val hy = cy - s * 0.02f
        val hs = s * 0.7f
        moveTo(hx, hy + hs * 0.2f)
        cubicTo(hx - hs * 0.35f, hy - hs * 0.02f, hx - hs * 0.25f, hy - hs * 0.28f, hx, hy - hs * 0.1f)
        cubicTo(hx + hs * 0.25f, hy - hs * 0.28f, hx + hs * 0.35f, hy - hs * 0.02f, hx, hy + hs * 0.2f)
    }
    drawPath(rightPath, color = color, style = Stroke(sw * 0.9f))
    // Connecting vine at bottom
    val vine = Path().apply {
        moveTo(cx - s * 0.1f, cy + s * 0.12f)
        cubicTo(cx - s * 0.05f, cy + s * 0.2f, cx + s * 0.05f, cy + s * 0.2f, cx + s * 0.1f, cy + s * 0.12f)
    }
    drawPath(vine, color = color.copy(alpha = 0.4f), style = Stroke(sw * 0.5f))
    // Small gem where hearts overlap
    drawCircle(color, radius = s * 0.025f, center = Offset(cx, cy - s * 0.04f))
}

private fun DrawScope.drawCrown(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    // Crown base (horizontal band)
    val baseY = cy + s * 0.12f
    val baseW = s * 0.35f
    drawLine(color, Offset(cx - baseW, baseY), Offset(cx + baseW, baseY), sw * 1.2f)
    // Inner band
    drawLine(color.copy(alpha = 0.4f), Offset(cx - baseW, baseY + sw * 1.5f), Offset(cx + baseW, baseY + sw * 1.5f), sw * 0.6f)
    // Crown peaks (5 peaks with zigzag)
    val crownPath = Path().apply {
        moveTo(cx - baseW, baseY)
        lineTo(cx - baseW * 0.7f, cy - s * 0.2f)  // peak 1
        lineTo(cx - baseW * 0.35f, cy + s * 0.02f) // valley
        lineTo(cx, cy - s * 0.3f)                   // center peak (tallest)
        lineTo(cx + baseW * 0.35f, cy + s * 0.02f)  // valley
        lineTo(cx + baseW * 0.7f, cy - s * 0.2f)    // peak 5
        lineTo(cx + baseW, baseY)
    }
    drawPath(crownPath, color = color, style = Stroke(sw))
    // Gems on peaks (3 filled circles)
    drawCircle(color, radius = s * 0.025f, center = Offset(cx - baseW * 0.7f, cy - s * 0.2f))
    drawCircle(color, radius = s * 0.035f, center = Offset(cx, cy - s * 0.3f))  // center gem larger
    drawCircle(color, radius = s * 0.025f, center = Offset(cx + baseW * 0.7f, cy - s * 0.2f))
    // Decorative dots on base band
    for (i in -2..2) {
        drawCircle(color.copy(alpha = 0.5f), radius = s * 0.012f, center = Offset(cx + i * baseW * 0.4f, baseY + sw * 0.7f))
    }
    // Radiant lines from center gem
    for (i in -1..1) {
        val angle = (i * 25f - 90f) * (PI.toFloat() / 180f)
        val r1 = s * 0.04f
        val r2 = s * 0.1f
        drawLine(color.copy(alpha = 0.3f),
            Offset(cx + cos(angle) * r1, cy - s * 0.3f + sin(angle) * r1),
            Offset(cx + cos(angle) * r2, cy - s * 0.3f + sin(angle) * r2), sw * 0.4f)
    }
}

private fun DrawScope.drawScales(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    // Central pillar
    drawLine(color, Offset(cx, cy - s * 0.3f), Offset(cx, cy + s * 0.25f), sw)
    // Base
    drawLine(color, Offset(cx - s * 0.15f, cy + s * 0.25f), Offset(cx + s * 0.15f, cy + s * 0.25f), sw * 1.1f)
    // Balance beam (horizontal, slightly curved)
    val beamW = s * 0.35f
    drawLine(color, Offset(cx - beamW, cy - s * 0.18f), Offset(cx + beamW, cy - s * 0.18f), sw * 0.9f)
    // Fulcrum triangle at top
    val triPath = Path().apply {
        moveTo(cx, cy - s * 0.32f)
        lineTo(cx - s * 0.06f, cy - s * 0.22f)
        lineTo(cx + s * 0.06f, cy - s * 0.22f)
        close()
    }
    drawPath(triPath, color = color)
    // Left bowl (arc)
    val bowlPath = Path().apply {
        moveTo(cx - beamW, cy - s * 0.18f)
        lineTo(cx - beamW - s * 0.02f, cy + s * 0.02f)
        cubicTo(cx - beamW - s * 0.02f, cy + s * 0.12f, cx - beamW + s * 0.18f, cy + s * 0.12f, cx - beamW + s * 0.18f, cy + s * 0.02f)
        lineTo(cx - beamW + s * 0.16f, cy - s * 0.18f)
    }
    drawPath(bowlPath, color = color, style = Stroke(sw * 0.7f))
    // Right bowl
    val bowlPath2 = Path().apply {
        moveTo(cx + beamW, cy - s * 0.18f)
        lineTo(cx + beamW + s * 0.02f, cy + s * 0.02f)
        cubicTo(cx + beamW + s * 0.02f, cy + s * 0.12f, cx + beamW - s * 0.18f, cy + s * 0.12f, cx + beamW - s * 0.18f, cy + s * 0.02f)
        lineTo(cx + beamW - s * 0.16f, cy - s * 0.18f)
    }
    drawPath(bowlPath2, color = color, style = Stroke(sw * 0.7f))
    // Chains (thin lines from beam to bowls)
    drawLine(color.copy(alpha = 0.5f), Offset(cx - beamW, cy - s * 0.18f), Offset(cx - beamW - s * 0.02f, cy + s * 0.02f), sw * 0.4f)
    drawLine(color.copy(alpha = 0.5f), Offset(cx - beamW + s * 0.16f, cy - s * 0.18f), Offset(cx - beamW + s * 0.18f, cy + s * 0.02f), sw * 0.4f)
    drawLine(color.copy(alpha = 0.5f), Offset(cx + beamW, cy - s * 0.18f), Offset(cx + beamW + s * 0.02f, cy + s * 0.02f), sw * 0.4f)
    drawLine(color.copy(alpha = 0.5f), Offset(cx + beamW - s * 0.16f, cy - s * 0.18f), Offset(cx + beamW - s * 0.18f, cy + s * 0.02f), sw * 0.4f)
}

private fun DrawScope.drawCardWithStar(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    // Single tarot card with glowing star
    val cw = s * 0.38f
    val ch = s * 0.52f
    val rect = Rect(cx - cw / 2, cy - ch / 2, cx + cw / 2, cy + ch / 2)
    // Outer frame
    val cardPath = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(rect, 4f, 4f)) }
    drawPath(cardPath, color = color, style = Stroke(sw * 1.1f))
    // Inner frame
    val inset = sw * 2f
    val inner = Rect(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset)
    val innerPath = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(inner, 2f, 2f)) }
    drawPath(innerPath, color = color.copy(alpha = 0.3f), style = Stroke(sw * 0.4f))
    // Center star (8-pointed, large and prominent)
    val starOuter = s * 0.12f
    val starInner = s * 0.055f
    val starPath = Path()
    for (i in 0 until 16) {
        val r = if (i % 2 == 0) starOuter else starInner
        val angle = (i * 22.5f - 90f) * (PI.toFloat() / 180f)
        val x = cx + cos(angle) * r
        val y = cy + sin(angle) * r
        if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
    }
    starPath.close()
    drawPath(starPath, color = color)
    // Glow rays from star (4 diagonal)
    for (i in 0 until 4) {
        val angle = (i * 90f + 45f) * (PI.toFloat() / 180f)
        drawLine(color.copy(alpha = 0.25f),
            Offset(cx + cos(angle) * starOuter * 1.2f, cy + sin(angle) * starOuter * 1.2f),
            Offset(cx + cos(angle) * starOuter * 2.2f, cy + sin(angle) * starOuter * 2.2f), sw * 0.3f)
    }
}

private fun DrawScope.drawThreeCards(cx: Float, cy: Float, s: Float, color: Color, sw: Float) {
    // Three distinct cards side by side with small gaps
    val cw = s * 0.2f
    val ch = s * 0.36f
    val gap = s * 0.06f
    for (i in -1..1) {
        val cardCx = cx + i * (cw + gap)
        val rect = Rect(cardCx - cw / 2, cy - ch / 2, cardCx + cw / 2, cy + ch / 2)
        val cardPath = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(rect, 3f, 3f)) }
        // Middle card slightly more prominent
        val alpha = if (i == 0) 1f else 0.6f
        val strokeMul = if (i == 0) 1.1f else 0.8f
        drawPath(cardPath, color = color.copy(alpha = alpha), style = Stroke(sw * strokeMul))
        // Inner border
        val inset = sw * 1.2f
        val innerRect = Rect(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset)
        val innerPath = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(innerRect, 2f, 2f)) }
        drawPath(innerPath, color = color.copy(alpha = alpha * 0.25f), style = Stroke(sw * 0.3f))
        // Small symbol in each card center
        val dotR = s * 0.015f
        if (i == 0) {
            // Center card: diamond
            val d = s * 0.035f
            val diamond = Path().apply {
                moveTo(cardCx, cy - d); lineTo(cardCx + d, cy)
                lineTo(cardCx, cy + d); lineTo(cardCx - d, cy); close()
            }
            drawPath(diamond, color = color.copy(alpha = alpha))
        } else {
            // Side cards: small dot
            drawCircle(color.copy(alpha = alpha), radius = dotR * 1.5f, center = Offset(cardCx, cy))
        }
    }
    // Connecting line below cards (timeline motif)
    drawLine(color.copy(alpha = 0.2f), Offset(cx - cw - gap, cy + ch / 2 + s * 0.04f), Offset(cx + cw + gap, cy + ch / 2 + s * 0.04f), sw * 0.3f)
    // Three dots on the line (past, present, future)
    for (i in -1..1) {
        drawCircle(color.copy(alpha = 0.4f), radius = s * 0.012f, center = Offset(cx + i * (cw + gap), cy + ch / 2 + s * 0.04f))
    }
}
