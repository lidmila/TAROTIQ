package com.tarotiq.app.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.FileProvider
import com.tarotiq.app.R
import java.io.File
import java.io.FileOutputStream

object ShareUtils {

    fun shareReadingAsImage(
        context: Context,
        cardResId: Int,
        cardName: String,
        spreadType: String,
        interpretation: String,
        date: String
    ) {
        val bitmap = createShareBitmap(context, cardResId, cardName, spreadType, interpretation, date)
        shareWithBitmap(context, bitmap, buildShareText(context, cardName))
    }

    fun shareDailyCard(
        context: Context,
        cardResId: Int,
        cardName: String,
        insight: String?,
        date: String
    ) {
        val bitmap = createShareBitmap(
            context, cardResId, cardName,
            context.getString(R.string.share_daily_card_label),
            insight ?: "", date
        )
        shareWithBitmap(context, bitmap, buildShareText(context, cardName))
    }

    private fun buildShareText(context: Context, cardName: String): String {
        return context.getString(R.string.share_text, cardName)
    }

    private fun createShareBitmap(
        context: Context,
        cardResId: Int,
        cardName: String,
        label: String,
        text: String,
        date: String
    ): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = android.graphics.Paint().apply {
            color = 0xFF131316.toInt()
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Gold border
        val borderPaint = android.graphics.Paint().apply {
            color = 0xFFe9c349.toInt()
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(30f, 30f, width - 30f, height - 30f, borderPaint)

        // Card image
        val drawable = context.resources.getDrawable(cardResId, context.theme)
        val cardBitmap = if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val bmp = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val c = Canvas(bmp)
            drawable.setBounds(0, 0, c.width, c.height)
            drawable.draw(c)
            bmp
        }

        val cardW = 480
        val cardH = (cardW * cardBitmap.height.toFloat() / cardBitmap.width).toInt()
        val cardX = (width - cardW) / 2f
        val cardY = 180f
        val scaledCard = Bitmap.createScaledBitmap(cardBitmap, cardW, cardH, true)
        canvas.drawBitmap(scaledCard, cardX, cardY, null)

        // Card name
        val namePaint = android.graphics.Paint().apply {
            color = 0xFFe9c349.toInt()
            textSize = 52f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        }
        canvas.drawText(cardName, width / 2f, cardY + cardH + 70f, namePaint)

        // Label (spread type or "Daily Card")
        val labelPaint = android.graphics.Paint().apply {
            color = 0xFFedb1ff.toInt()
            textSize = 32f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.15f
        }
        canvas.drawText(label.uppercase(), width / 2f, cardY + cardH + 120f, labelPaint)

        // Interpretation text (truncated, wrapped)
        val textPaint = android.graphics.Paint().apply {
            color = 0xFFc8c4d7.toInt()
            textSize = 34f
            isAntiAlias = true
        }
        val truncated = if (text.length > 300) text.take(297) + "…" else text
        drawWrappedText(canvas, truncated, textPaint, 80f, cardY + cardH + 170f, width - 160f, 44f)

        // Date
        val datePaint = android.graphics.Paint().apply {
            color = 0xFF928ea0.toInt()
            textSize = 26f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(date, width / 2f, height - 130f, datePaint)

        // Branding
        val brandPaint = android.graphics.Paint().apply {
            color = 0xFFe9c349.toInt()
            textSize = 36f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
            letterSpacing = 0.2f
        }
        canvas.drawText("TAROTIQ", width / 2f, height - 70f, brandPaint)

        scaledCard.recycle()
        return bitmap
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        paint: android.graphics.Paint,
        x: Float,
        startY: Float,
        maxWidth: Float,
        lineHeight: Float
    ) {
        val words = text.split(" ")
        var currentLine = ""
        var y = startY
        val maxLines = 8
        var lineCount = 0

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val testWidth = paint.measureText(testLine)
            if (testWidth > maxWidth && currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, x, y, paint)
                currentLine = word
                y += lineHeight
                lineCount++
                if (lineCount >= maxLines) break
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty() && lineCount < maxLines) {
            canvas.drawText(currentLine, x, y, paint)
        }
    }

    private fun shareWithBitmap(context: Context, bitmap: Bitmap, shareText: String) {
        val cacheDir = File(context.cacheDir, "shared_images")
        cacheDir.mkdirs()
        val file = File(cacheDir, "tarotiq_share.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        bitmap.recycle()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, null))
    }
}
