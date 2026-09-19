package com.example.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.View
import com.example.model.CameraCutoutStyle
import com.example.model.CenterDisplayPreference
import com.example.model.DeviceStatus
import com.example.model.IndicatorColorMode
import com.example.model.OverlaySettings
import com.example.model.PositionPreset
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Custom Overlay View matching the unified circular indicator design:
 * - Upper arc (half-circle): Battery level & charge state
 * - Lower arc (4 dots): Cellular signal strength
 * - Center: Wi-Fi, Do Not Disturb, Silent, or 4G/5G
 * Supports 4 camera cutout overlap modes: Orbital Satellite, Smart Auto-Hide, Perimeter Halo, Flanking Wings
 */
class DuoOverlayView(context: Context) : View(context) {

    private var status = DeviceStatus()
    private var settings = OverlaySettings()
    private var isCameraInUse = false

    fun setCameraInUse(inUse: Boolean) {
        if (this.isCameraInUse != inUse) {
            this.isCameraInUse = inUse
            postInvalidate()
        }
    }

    private enum class CenterMode {
        WIFI,
        DND,
        SILENT,
        CELLULAR_5G,
        CELLULAR_4G
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
    }
    private val ringTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val ringProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val dotActivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val dotInactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val iconStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val iconFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val arcRect = RectF()
    private val tempRect = RectF()
    private val bellPath = Path()

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
        val baseDp = 54f
        val dimension = (baseDp * density * scale).roundToInt()

        val isWrapCutout = settings.positionPreset == PositionPreset.WRAP_CUTOUT
        var w = dimension
        var h = dimension

        if (isWrapCutout) {
            when (settings.cameraCutoutStyle) {
                CameraCutoutStyle.ORBITAL_SATELLITE -> {
                    h = dimension + (18f * density * scale).roundToInt()
                }
                CameraCutoutStyle.FLANKING_WINGS -> {
                    w = dimension + (40f * density * scale).roundToInt()
                }
                else -> {}
            }
        }

        if (settings.showTimeOnLeft && !settings.timeIndependentMovement) {
            val timeScaleMultiplier = settings.timeTextScalePercent / 100f
            val basePillW = 64f * timeScaleMultiplier
            val timePillW = (basePillW * density * scale).roundToInt()
            val spacing = (6f * density * scale).roundToInt()
            w += timePillW + spacing
        }

        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val density = resources.displayMetrics.density
        val scale = (settings.scalePercent / 100f).coerceIn(0.25f, 1.5f)
        val baseDp = 54f
        val baseDim = (baseDp * density * scale)

        val isDark = when (settings.colorMode) {
            IndicatorColorMode.AUTO -> !status.isBehindLight
            IndicatorColorMode.DARK, IndicatorColorMode.JUST_WHITE -> true
            IndicatorColorMode.LIGHT -> false
            IndicatorColorMode.CYBER_MINT, IndicatorColorMode.ELECTRIC_CYAN -> true
        }

        // Palette setup
        val primaryFgColor = when {
            settings.colorMode == IndicatorColorMode.JUST_WHITE -> Color.WHITE
            settings.colorMode == IndicatorColorMode.CYBER_MINT -> Color.parseColor("#00E676")
            settings.colorMode == IndicatorColorMode.ELECTRIC_CYAN -> Color.parseColor("#00E5FF")
            isDark -> Color.WHITE
            else -> Color.parseColor("#14171F")
        }
        val trackColor = if (isDark) Color.argb(60, 255, 255, 255) else Color.argb(50, 0, 0, 0)

        val isWrapCutout = settings.positionPreset == PositionPreset.WRAP_CUTOUT
        val isFlankingWings = isWrapCutout && settings.cameraCutoutStyle == CameraCutoutStyle.FLANKING_WINGS
        val circleW = if (isFlankingWings) baseDim + (40f * density * scale) else baseDim

        val timeScaleMultiplier = (settings.timeTextScalePercent / 100f).coerceIn(0.7f, 2.0f)
        val timePillWBase = 64f * timeScaleMultiplier
        val timeOffset = if (settings.showTimeOnLeft && !settings.timeIndependentMovement) {
            (timePillWBase + 6f) * density * scale
        } else {
            0f
        }

        // Center coordinates of the primary ring
        val cx = timeOffset + (circleW / 2f)
        val cy = baseDim / 2f

        // Render Time Pill on the left if enabled and attached (Background removed, pure floating text)
        if (settings.showTimeOnLeft && !settings.timeIndependentMovement) {
            val pillW = (timePillWBase - 2f) * density * scale
            val pillH = (28f * timeScaleMultiplier) * density * scale
            val pillLeft = 1f * density * scale
            val pillTop = cy - (pillH / 2f)
            val pillRight = pillLeft + pillW
            val pillBottom = pillTop + pillH
            val pillRect = RectF(pillLeft, pillTop, pillRight, pillBottom)

            val timeText = if (status.currentTimeText.isNotEmpty()) {
                status.currentTimeText
            } else {
                val is24 = android.text.format.DateFormat.is24HourFormat(context)
                val sdf = java.text.SimpleDateFormat(if (is24) "HH:mm" else "h:mm", java.util.Locale.getDefault())
                sdf.format(java.util.Date())
            }

            val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = primaryFgColor
                textSize = 16f * timeScaleMultiplier * density * scale
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                fontFeatureSettings = "tnum"
                setShadowLayer(3f, 0f, 1f, if (isDark) Color.argb(100, 0, 0, 0) else Color.argb(80, 255, 255, 255))
            }
            val fm = timePaint.fontMetrics
            val textY = pillRect.centerY() - (fm.ascent + fm.descent) / 2f
            canvas.drawText(timeText, pillRect.centerX(), textY, timePaint)
        }

        val radius = (baseDim / 2f) - (7.5f * density * scale)
        val strokeWidth = radius * 0.17f

        val centerMode = when (settings.centerPreference) {
            CenterDisplayPreference.AUTO -> {
                when {
                    status.isDndActive -> CenterMode.DND
                    status.isRingerSilent || status.isRingerVibrate -> CenterMode.SILENT
                    status.isWifiConnected -> CenterMode.WIFI
                    status.is5G -> CenterMode.CELLULAR_5G
                    else -> CenterMode.CELLULAR_4G
                }
            }
            CenterDisplayPreference.WIFI -> CenterMode.WIFI
            CenterDisplayPreference.CELLULAR -> if (status.is5G) CenterMode.CELLULAR_5G else CenterMode.CELLULAR_4G
            CenterDisplayPreference.DND -> CenterMode.DND
            CenterDisplayPreference.SILENT -> CenterMode.SILENT
        }

        // Overlap style handling (halo aura around camera cutout, no backgrounds)
        if (isWrapCutout && settings.cameraCutoutStyle == CameraCutoutStyle.PERIMETER_HALO) {
            val haloColor = when {
                settings.colorMode == IndicatorColorMode.JUST_WHITE -> Color.argb(150, 255, 255, 255)
                centerMode == CenterMode.WIFI -> if (isDark) Color.argb(120, 0, 229, 255) else Color.argb(140, 0, 131, 143)
                centerMode == CenterMode.DND || centerMode == CenterMode.SILENT -> if (isDark) Color.argb(120, 255, 145, 0) else Color.argb(140, 216, 67, 21)
                centerMode == CenterMode.CELLULAR_5G -> if (isDark) Color.argb(120, 0, 230, 118) else Color.argb(140, 0, 137, 123)
                else -> if (isDark) Color.argb(100, 120, 144, 156) else Color.argb(100, 69, 90, 100)
            }
            val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                setStrokeWidth(strokeWidth * 0.6f)
                color = haloColor
            }
            canvas.drawCircle(cx, cy, radius + strokeWidth * 1.05f, auraPaint)
        }

        // --- 1. Battery Half Circle / Upper Arc ---
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius)
        val batteryPct = status.batteryPercent.coerceIn(0, 100)

        // Arc colors & Dynamic Gradient
        val (gradStart, gradEnd, baseBatteryColor) = when {
            settings.colorMode == IndicatorColorMode.JUST_WHITE -> Triple(Color.WHITE, Color.WHITE, Color.WHITE)
            status.isCharging -> if (isDark) Triple(Color.parseColor("#00E5FF"), Color.parseColor("#00FFC4"), Color.parseColor("#00E5FF")) else Triple(Color.parseColor("#00838F"), Color.parseColor("#00BFA5"), Color.parseColor("#00838F"))
            status.isPowerSaveMode -> if (isDark) Triple(Color.parseColor("#FFEA00"), Color.parseColor("#FF9100"), Color.parseColor("#FFD600")) else Triple(Color.parseColor("#FF8F00"), Color.parseColor("#E65100"), Color.parseColor("#E65100"))
            batteryPct <= 20 -> if (isDark) Triple(Color.parseColor("#FF9100"), Color.parseColor("#FF1744"), Color.parseColor("#FF5252")) else Triple(Color.parseColor("#D50000"), Color.parseColor("#FF3D00"), Color.parseColor("#D50000"))
            batteryPct >= 95 -> if (isDark) Triple(Color.parseColor("#00E676"), Color.parseColor("#00E5FF"), Color.parseColor("#00E676")) else Triple(Color.parseColor("#00796B"), Color.parseColor("#00C853"), Color.parseColor("#00796B"))
            else -> Triple(primaryFgColor, primaryFgColor, primaryFgColor)
        }

        ringTrackPaint.strokeWidth = strokeWidth
        ringTrackPaint.color = trackColor

        ringProgressPaint.strokeWidth = strokeWidth
        ringProgressPaint.shader = LinearGradient(
            cx - radius, cy, cx + radius, cy,
            gradStart, gradEnd, Shader.TileMode.CLAMP
        )

        // Notch gap calculation when battery number is displayed
        val hasNotch = settings.showBatteryNumber
        if (hasNotch) {
            val batteryText = if (status.isCharging) "⚡$batteryPct" else "$batteryPct"
            val isThreeChars = batteryText.length >= 3
            val batteryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = baseBatteryColor
                textSize = if (isThreeChars) radius * 0.48f else radius * 0.54f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                fontFeatureSettings = "tnum"
                letterSpacing = -0.02f
                textAlign = Paint.Align.CENTER
            }
            val textWidth = batteryTextPaint.measureText(batteryText)
            val fontMetrics = batteryTextPaint.fontMetrics

            val clearanceMargin = 3.5f * density * scale
            val halfGapX = (textWidth / 2f) + (strokeWidth * 0.5f) + clearanceMargin
            val sinVal = (halfGapX / radius).coerceIn(0.25f, 0.82f)
            val gapHalfAngle = Math.toDegrees(kotlin.math.asin(sinVal.toDouble())).toFloat()

            val trackSegmentSweep = 115f - gapHalfAngle
            val rightStartAngle = 270f + gapHalfAngle

            // Left track segment
            canvas.drawArc(arcRect, 155f, trackSegmentSweep, false, ringTrackPaint)
            // Right track segment
            canvas.drawArc(arcRect, rightStartAngle, trackSegmentSweep, false, ringTrackPaint)

            // Battery progress fill
            val totalTrackSweep = trackSegmentSweep * 2f
            val totalProgressSweep = (batteryPct / 100f) * totalTrackSweep
            if (totalProgressSweep > 0.5f) {
                val fill1 = minOf(totalProgressSweep, trackSegmentSweep)
                canvas.drawArc(arcRect, 155f, fill1, false, ringProgressPaint)
                if (totalProgressSweep > trackSegmentSweep) {
                    val fill2 = totalProgressSweep - trackSegmentSweep
                    canvas.drawArc(arcRect, rightStartAngle, fill2, false, ringProgressPaint)
                }
            }

            // Draw battery text in the clear notch
            val pillCenterY = cy - radius
            batteryTextPaint.setShadowLayer(3f, 0f, 1f, if (isDark) Color.argb(100, 0, 0, 0) else Color.argb(80, 255, 255, 255))
            val textBaseline = pillCenterY - (fontMetrics.ascent + fontMetrics.descent) / 2f
            canvas.drawText(batteryText, cx, textBaseline, batteryTextPaint)
        } else {
            // Full continuous upper arc
            canvas.drawArc(arcRect, 155f, 230f, false, ringTrackPaint)
            val progressSweep = (batteryPct / 100f) * 230f
            if (progressSweep > 0.5f) {
                canvas.drawArc(arcRect, 155f, progressSweep, false, ringProgressPaint)
            }
        }

        // --- 2. Cellular Signal 4 Dots along Bottom Arc (135°, 105°, 75°, 45°) ---
        if (settings.showCellSignalDots) {
            val dotAngles = floatArrayOf(135f, 105f, 75f, 45f)
            val activeLevel = status.cellularSignalLevel.coerceIn(0, 4)
            val baseDotRadius = strokeWidth * 0.48f

            // Size scale factors for 4 dots: progressive scaling like cellular signal bars (0.80x, 0.94x, 1.08x, 1.22x)
            val sizeMultipliers = floatArrayOf(0.80f, 0.94f, 1.08f, 1.22f)

            // Signal-aware cellular dot color (or monochrome if JUST_WHITE)
            val activeDotColor = when {
                settings.colorMode == IndicatorColorMode.JUST_WHITE -> Color.WHITE
                settings.colorMode == IndicatorColorMode.CYBER_MINT -> Color.parseColor("#00E676")
                settings.colorMode == IndicatorColorMode.ELECTRIC_CYAN -> Color.parseColor("#00E5FF")
                !isDark -> when {
                    activeLevel >= 3 -> Color.parseColor("#00897B")
                    activeLevel == 2 -> Color.parseColor("#E65100")
                    activeLevel == 1 -> Color.parseColor("#C62828")
                    else -> Color.parseColor("#D50000")
                }
                activeLevel >= 3 -> Color.parseColor("#00E5FF") // Vibrant Cyan for cell reception
                activeLevel == 2 -> Color.parseColor("#FFD600") // Moderate amber
                activeLevel == 1 -> Color.parseColor("#FF5252") // Weak red
                else -> Color.parseColor("#FF1744") // Disconnected alert
            }

            dotActivePaint.color = activeDotColor
            dotInactivePaint.color = trackColor

            for (i in 0 until 4) {
                val angleDeg = dotAngles[i]
                val rad = Math.toRadians(angleDeg.toDouble())
                val dx = (cx + radius * cos(rad)).toFloat()
                val dy = (cy + radius * sin(rad)).toFloat()
                val dotRadius = baseDotRadius * sizeMultipliers[i]

                if (i < activeLevel) {
                    canvas.drawCircle(dx, dy, dotRadius, dotActivePaint)
                } else {
                    canvas.drawCircle(dx, dy, dotRadius, dotInactivePaint)
                }
            }
        }

        // --- 3. Center or Satellite / Flanking Content ---
        if (!isWrapCutout) {
            drawCenterGlyph(canvas, cx, cy, radius, strokeWidth, centerMode, primaryFgColor, isDark)
        } else {
            when (settings.cameraCutoutStyle) {
                CameraCutoutStyle.HOLLOW_RING, CameraCutoutStyle.PERIMETER_HALO -> {
                    // Center left clear
                }
                CameraCutoutStyle.SMART_AUTO_HIDE -> {
                    if (!isCameraInUse) {
                        drawCenterGlyph(canvas, cx, cy, radius * 0.85f, strokeWidth, centerMode, primaryFgColor, isDark)
                    }
                }
                CameraCutoutStyle.ORBITAL_SATELLITE -> {
                    // Draw micro satellite badge at bottom 6 o'clock (beneath the camera ring)
                    val satCy = cy + radius + (8f * density * scale)
                    drawCenterGlyph(canvas, cx, satCy, radius * 0.48f, strokeWidth * 0.7f, centerMode, primaryFgColor, isDark)
                }
                CameraCutoutStyle.FLANKING_WINGS -> {
                    // Left wing pill: Wi-Fi or DND/Silent (floating)
                    val leftCx = cx - radius - (11f * density * scale)
                    drawCenterGlyph(canvas, leftCx, cy, radius * 0.45f, strokeWidth * 0.65f, centerMode, primaryFgColor, isDark)

                    // Right wing pill: 5G/4G badge (floating)
                    val rightCx = cx + radius + (11f * density * scale)
                    textPaint.color = primaryFgColor
                    textPaint.textSize = radius * 0.40f
                    textPaint.setShadowLayer(3f, 0f, 1f, if (isDark) Color.argb(100, 0, 0, 0) else Color.argb(80, 255, 255, 255))
                    val yOffset = (textPaint.descent() + textPaint.ascent()) / 2f
                    val cellText = if (status.is5G) "5G" else "4G"
                    canvas.drawText(cellText, rightCx, cy - yOffset, textPaint)
                }
            }
        }
    }

    private fun drawCenterGlyph(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        strokeWidth: Float,
        centerMode: CenterMode,
        primaryFgColor: Int,
        isDark: Boolean
    ) {
        val iconColor = when {
            settings.colorMode == IndicatorColorMode.JUST_WHITE -> Color.WHITE
            centerMode == CenterMode.WIFI -> if (isDark) Color.parseColor("#00E5FF") else Color.parseColor("#00838F")
            centerMode == CenterMode.DND || centerMode == CenterMode.SILENT -> if (isDark) Color.parseColor("#FF9100") else Color.parseColor("#D84315")
            centerMode == CenterMode.CELLULAR_5G -> if (isDark) Color.parseColor("#00E676") else Color.parseColor("#00796B")
            else -> primaryFgColor
        }
        iconStrokePaint.color = iconColor
        iconFillPaint.color = iconColor

        when (centerMode) {
            CenterMode.WIFI -> {
                // Wi-Fi Glyph: Bottom dot + 2 concentric curved waves
                val wifiBottomY = cy + radius * 0.32f
                val dotR = strokeWidth * 0.44f
                canvas.drawCircle(cx, wifiBottomY, dotR, iconFillPaint)

                // Middle wave
                val waveStroke = strokeWidth * 0.72f
                iconStrokePaint.strokeWidth = waveStroke
                val rMid = radius * 0.36f
                tempRect.set(cx - rMid, wifiBottomY - rMid, cx + rMid, wifiBottomY + rMid)
                canvas.drawArc(tempRect, 220f, 100f, false, iconStrokePaint)

                // Outer wave
                val rOuter = radius * 0.60f
                tempRect.set(cx - rOuter, wifiBottomY - rOuter, cx + rOuter, wifiBottomY + rOuter)
                canvas.drawArc(tempRect, 225f, 90f, false, iconStrokePaint)
            }

            CenterMode.DND -> {
                // Do Not Disturb Glyph: Circle outline + horizontal bar
                val dndRadius = radius * 0.42f
                val dndStroke = strokeWidth * 0.72f
                iconStrokePaint.strokeWidth = dndStroke
                canvas.drawCircle(cx, cy, dndRadius, iconStrokePaint)
                canvas.drawLine(cx - dndRadius * 0.65f, cy, cx + dndRadius * 0.65f, cy, iconStrokePaint)
            }

            CenterMode.SILENT -> {
                // Silent Mode: Bell silhouette with clean diagonal slash
                val silentSize = radius * 0.44f
                val silentStroke = strokeWidth * 0.72f
                iconStrokePaint.strokeWidth = silentStroke

                // Bell dome
                bellPath.reset()
                bellPath.moveTo(cx, cy - silentSize * 0.8f)
                bellPath.quadTo(cx - silentSize * 0.6f, cy, cx - silentSize * 0.7f, cy + silentSize * 0.5f)
                bellPath.lineTo(cx + silentSize * 0.7f, cy + silentSize * 0.5f)
                bellPath.quadTo(cx + silentSize * 0.6f, cy, cx, cy - silentSize * 0.8f)
                canvas.drawPath(bellPath, iconStrokePaint)

                // Clapper
                canvas.drawCircle(cx, cy + silentSize * 0.65f, strokeWidth * 0.35f, iconFillPaint)

                // Slash through bell
                canvas.drawLine(
                    cx - silentSize * 0.75f,
                    cy - silentSize * 0.75f,
                    cx + silentSize * 0.75f,
                    cy + silentSize * 0.75f,
                    iconStrokePaint
                )
            }

            CenterMode.CELLULAR_5G -> {
                textPaint.color = primaryFgColor
                textPaint.textSize = radius * 0.70f
                val yOffset = (textPaint.descent() + textPaint.ascent()) / 2f
                canvas.drawText("5G", cx, cy - yOffset, textPaint)
            }

            CenterMode.CELLULAR_4G -> {
                textPaint.color = primaryFgColor
                textPaint.textSize = radius * 0.70f
                val yOffset = (textPaint.descent() + textPaint.ascent()) / 2f
                canvas.drawText("4G", cx, cy - yOffset, textPaint)
            }
        }
    }
}
