package com.example.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.View
import com.example.model.DeviceStatus
import com.example.model.IndicatorColorMode
import com.example.model.OverlaySettings
import kotlin.math.roundToInt

/**
 * Dedicated Overlay View for displaying the floating time pill independently
 * from the main status indicator, allowing independent coordinates & positioning.
 */
class DuoTimeOverlayView(context: Context) : View(context) {

    private var status = DeviceStatus()
    private var settings = OverlaySettings()

    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        fontFeatureSettings = "tnum"
    }

    private val pillRect = RectF()

    fun updateData(newStatus: DeviceStatus, newSettings: OverlaySettings) {
        this.status = newStatus
        this.settings = newSettings
        alpha = (settings.opacityPercent / 100f).coerceIn(0.4f, 1.0f)
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        val scale = (settings.scalePercent / 100f).coerceIn(0.25f, 1.5f)
        val timeScaleMultiplier = (settings.timeTextScalePercent / 100f).coerceIn(0.7f, 2.0f)

        val pillW = ((64f * timeScaleMultiplier) * density * scale).roundToInt()
        val pillH = ((28f * timeScaleMultiplier) * density * scale).roundToInt()

        setMeasuredDimension(pillW, pillH)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val density = resources.displayMetrics.density
        val scale = (settings.scalePercent / 100f).coerceIn(0.25f, 1.5f)
        val timeScaleMultiplier = (settings.timeTextScalePercent / 100f).coerceIn(0.7f, 2.0f)

        val isDark = when (settings.colorMode) {
            IndicatorColorMode.AUTO -> !status.isBehindLight
            IndicatorColorMode.DARK, IndicatorColorMode.JUST_WHITE -> true
            IndicatorColorMode.LIGHT -> false
            IndicatorColorMode.CYBER_MINT, IndicatorColorMode.ELECTRIC_CYAN -> true
        }

        val primaryFgColor = when {
            settings.colorMode == IndicatorColorMode.JUST_WHITE -> Color.WHITE
            settings.colorMode == IndicatorColorMode.CYBER_MINT -> Color.parseColor("#00E676")
            settings.colorMode == IndicatorColorMode.ELECTRIC_CYAN -> Color.parseColor("#00E5FF")
            isDark -> Color.WHITE
            else -> Color.parseColor("#14171F")
        }

        pillRect.set(0f, 0f, w, h)

        val timeText = if (status.currentTimeText.isNotEmpty()) {
            status.currentTimeText
        } else {
            val is24 = android.text.format.DateFormat.is24HourFormat(context)
            val sdf = java.text.SimpleDateFormat(if (is24) "HH:mm" else "h:mm", java.util.Locale.getDefault())
            sdf.format(java.util.Date())
        }

        timePaint.apply {
            color = primaryFgColor
            textSize = 16f * timeScaleMultiplier * density * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(3f, 0f, 1f, if (isDark) Color.argb(100, 0, 0, 0) else Color.argb(80, 255, 255, 255))
        }

        val fm = timePaint.fontMetrics
        val textY = pillRect.centerY() - (fm.ascent + fm.descent) / 2f
        canvas.drawText(timeText, pillRect.centerX(), textY, timePaint)
    }
}
