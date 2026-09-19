package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraCutoutStyle
import com.example.model.CenterDisplayPreference
import com.example.model.DeviceStatus
import com.example.model.IndicatorColorMode
import com.example.model.OverlaySettings
import com.example.model.PositionPreset
import kotlin.math.cos
import kotlin.math.sin

enum class IndicatorCenterMode {
    WIFI,
    DND,
    SILENT,
    CELLULAR_5G,
    CELLULAR_4G
}

@Composable
fun DuoStatusIndicator(
    status: DeviceStatus,
    settings: OverlaySettings,
    modifier: Modifier = Modifier,
    isCameraActive: Boolean = false,
    isBehindLight: Boolean? = null
) {
    val behindIsLight = isBehindLight ?: status.isBehindLight
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (settings.colorMode) {
        IndicatorColorMode.AUTO -> !behindIsLight
        IndicatorColorMode.DARK, IndicatorColorMode.JUST_WHITE -> true
        IndicatorColorMode.LIGHT -> false
        IndicatorColorMode.CYBER_MINT, IndicatorColorMode.ELECTRIC_CYAN -> true
    }

    val primaryFgColor by animateColorAsState(
        targetValue = when {
            settings.colorMode == IndicatorColorMode.JUST_WHITE -> Color.White
            settings.colorMode == IndicatorColorMode.CYBER_MINT -> Color(0xFF00E676)
            settings.colorMode == IndicatorColorMode.ELECTRIC_CYAN -> Color(0xFF00E5FF)
            isDark -> Color.White
            else -> Color(0xFF14171F)
        },
        label = "fgColor"
    )

    val trackColor by animateColorAsState(
        targetValue = if (isDark) Color(0x3BFFFFFF) else Color(0x33000000),
        label = "trackColor"
    )

    val batteryPct = status.batteryPercent.coerceIn(0, 100)
    val batteryColor by animateColorAsState(
        targetValue = when {
            settings.colorMode == IndicatorColorMode.JUST_WHITE -> Color.White
            status.isCharging -> if (isDark) Color(0xFF00E5FF) else Color(0xFF00838F)
            status.isPowerSaveMode -> if (isDark) Color(0xFFFFD600) else Color(0xFFE65100)
            batteryPct <= 20 -> if (isDark) Color(0xFFFF5252) else Color(0xFFC62828)
            batteryPct >= 95 -> if (isDark) Color(0xFF00E676) else Color(0xFF00796B)
            else -> primaryFgColor
        },
        label = "batteryColor"
    )

    val animatedBatteryProgress by animateFloatAsState(
        targetValue = batteryPct / 100f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "batteryProgress"
    )

    val centerMode = when (settings.centerPreference) {
        CenterDisplayPreference.AUTO -> {
            when {
                status.isDndActive -> IndicatorCenterMode.DND
                status.isRingerSilent || status.isRingerVibrate -> IndicatorCenterMode.SILENT
                status.isWifiConnected -> IndicatorCenterMode.WIFI
                status.is5G -> IndicatorCenterMode.CELLULAR_5G
                else -> IndicatorCenterMode.CELLULAR_4G
            }
        }
        CenterDisplayPreference.WIFI -> IndicatorCenterMode.WIFI
        CenterDisplayPreference.CELLULAR -> if (status.is5G) IndicatorCenterMode.CELLULAR_5G else IndicatorCenterMode.CELLULAR_4G
        CenterDisplayPreference.DND -> IndicatorCenterMode.DND
        CenterDisplayPreference.SILENT -> IndicatorCenterMode.SILENT
    }

    val scaleFactor = (settings.scalePercent / 100f).coerceIn(0.25f, 1.5f)
    val opacityFactor = (settings.opacityPercent / 100f).coerceIn(0.4f, 1.0f)

    val isWrapCutout = settings.positionPreset == PositionPreset.WRAP_CUTOUT

    val showCenterOverLens = !isWrapCutout || (
        settings.cameraCutoutStyle == CameraCutoutStyle.SMART_AUTO_HIDE && !isCameraActive
    )

    val context = LocalContext.current
    val currentTime = if (status.currentTimeText.isNotEmpty()) {
        status.currentTimeText
    } else {
        val is24 = android.text.format.DateFormat.is24HourFormat(context)
        val sdf = remember(is24) { java.text.SimpleDateFormat(if (is24) "HH:mm" else "h:mm", java.util.Locale.getDefault()) }
        remember { sdf.format(java.util.Date()) }
    }

    val timeScaleMultiplier = (settings.timeTextScalePercent / 100f).coerceIn(0.7f, 2.0f)

    val timePill = @Composable {
        Box(
            modifier = Modifier
                .testTag("time_left_pill")
                .width((64 * timeScaleMultiplier).dp)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentTime,
                color = primaryFgColor,
                fontSize = (16 * timeScaleMultiplier).sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    fontFeatureSettings = "tnum",
                    letterSpacing = (-0.5).sp,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = if (isDark) Color.Black.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.40f),
                        blurRadius = 3f
                    )
                )
            )
        }
    }

    val indicatorCanvas = @Composable {
        Box(
            modifier = Modifier.size(
                width = if (isWrapCutout && settings.cameraCutoutStyle == CameraCutoutStyle.FLANKING_WINGS) 92.dp else 54.dp,
                height = if (isWrapCutout && settings.cameraCutoutStyle == CameraCutoutStyle.ORBITAL_SATELLITE) 70.dp else 54.dp
            ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
            val totalW = size.width
            val totalH = size.height
            val baseDim = 54.dp.toPx()

            val cx = totalW / 2f
            val cy = baseDim / 2f

            val radius = (baseDim / 2f) - 7.5.dp.toPx()
            val strokeWidth = radius * 0.17f

            // Cutout Overlap: Perimeter Halo
            if (isWrapCutout && settings.cameraCutoutStyle == CameraCutoutStyle.PERIMETER_HALO) {
                val haloColor = when {
                    settings.colorMode == IndicatorColorMode.JUST_WHITE -> Color(0x99FFFFFF)
                    centerMode == IndicatorCenterMode.WIFI -> if (isDark) Color(0x9900E5FF) else Color(0x9900838F)
                    centerMode == IndicatorCenterMode.DND || centerMode == IndicatorCenterMode.SILENT -> if (isDark) Color(0x99FF9100) else Color(0x99D84315)
                    centerMode == IndicatorCenterMode.CELLULAR_5G -> if (isDark) Color(0x9900E676) else Color(0x9900897B)
                    else -> if (isDark) Color(0x8078909C) else Color(0x60455A64)
                }
                drawCircle(
                    color = haloColor,
                    radius = radius + strokeWidth * 1.15f,
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeWidth * 0.65f)
                )
            }

            // Dynamic Gradient for Battery Arc
            val batteryGradient = Brush.linearGradient(
                colors = when {
                    settings.colorMode == IndicatorColorMode.JUST_WHITE -> listOf(Color.White, Color(0xFFEBEBEB))
                    status.isCharging -> if (isDark) listOf(Color(0xFF00E5FF), Color(0xFF00FFC4)) else listOf(Color(0xFF00838F), Color(0xFF00BFA5))
                    status.isPowerSaveMode -> if (isDark) listOf(Color(0xFFFFEA00), Color(0xFFFF9100)) else listOf(Color(0xFFFF8F00), Color(0xFFE65100))
                    batteryPct <= 20 -> if (isDark) listOf(Color(0xFFFF9100), Color(0xFFFF1744)) else listOf(Color(0xFFD50000), Color(0xFFFF3D00))
                    batteryPct >= 95 -> if (isDark) listOf(Color(0xFF00E676), Color(0xFF00E5FF)) else listOf(Color(0xFF00796B), Color(0xFF00C853))
                    else -> listOf(primaryFgColor.copy(alpha = 0.88f), primaryFgColor)
                },
                start = Offset(cx - radius, cy),
                end = Offset(cx + radius, cy)
            )

            // --- 1. Battery Half Circle / Upper Arc ---
            val arcTopLeft = Offset(cx - radius, cy - radius)
            val arcSize = Size(radius * 2f, radius * 2f)

            // Setup battery text Paint first so we can dynamically carve out the exact notch clearance
            val batteryText = if (status.isCharging) "⚡$batteryPct" else "$batteryPct"
            val isThreeChars = batteryText.length >= 3
            val textPaint = android.graphics.Paint().apply {
                color = batteryColor.toArgb()
                textSize = if (isThreeChars) radius * 0.48f else radius * 0.54f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                fontFeatureSettings = "tnum"
                letterSpacing = -0.02f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                setShadowLayer(3f, 0f, 1f, if (isDark) android.graphics.Color.argb(100, 0, 0, 0) else android.graphics.Color.argb(80, 255, 255, 255))
            }
            val textWidth = textPaint.measureText(batteryText)
            val fontMetrics = textPaint.fontMetrics

            if (settings.showBatteryNumber) {
                // Dynamic angular notch gap: calculate required clearance so the track & progress bar NEVER overlap the text
                val clearanceMargin = with(density) { 3.5.dp.toPx() }
                val halfGapX = (textWidth / 2f) + (strokeWidth * 0.5f) + clearanceMargin
                val sinVal = (halfGapX / radius).coerceIn(0.25f, 0.82f)
                val gapHalfAngle = Math.toDegrees(kotlin.math.asin(sinVal.toDouble())).toFloat()

                val trackSegmentSweep = 115f - gapHalfAngle
                val rightStartAngle = 270f + gapHalfAngle

                // Left track
                drawArc(
                    color = trackColor,
                    startAngle = 155f,
                    sweepAngle = trackSegmentSweep,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                // Right track
                drawArc(
                    color = trackColor,
                    startAngle = rightStartAngle,
                    sweepAngle = trackSegmentSweep,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Battery Progress fill
                val totalTrackSweep = trackSegmentSweep * 2f
                val totalProgressSweep = animatedBatteryProgress * totalTrackSweep
                if (totalProgressSweep > 0.5f) {
                    val fill1 = minOf(totalProgressSweep, trackSegmentSweep)
                    drawArc(
                        brush = batteryGradient,
                        startAngle = 155f,
                        sweepAngle = fill1,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    if (totalProgressSweep > trackSegmentSweep) {
                        val fill2 = totalProgressSweep - trackSegmentSweep
                        drawArc(
                            brush = batteryGradient,
                            startAngle = rightStartAngle,
                            sweepAngle = fill2,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Render battery number directly in the notch gap
                val pillCenterY = cy - radius
                val textBaseline = pillCenterY - (fontMetrics.ascent + fontMetrics.descent) / 2f
                drawContext.canvas.nativeCanvas.drawText(batteryText, cx, textBaseline, textPaint)
            } else {
                // Full continuous track
                drawArc(
                    color = trackColor,
                    startAngle = 155f,
                    sweepAngle = 230f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                val sweepProgress = animatedBatteryProgress * 230f
                if (sweepProgress > 0.5f) {
                    drawArc(
                        brush = batteryGradient,
                        startAngle = 155f,
                        sweepAngle = sweepProgress,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
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
                    settings.colorMode == IndicatorColorMode.JUST_WHITE -> Color.White
                    settings.colorMode == IndicatorColorMode.CYBER_MINT -> Color(0xFF00E676)
                    settings.colorMode == IndicatorColorMode.ELECTRIC_CYAN -> Color(0xFF00E5FF)
                    !isDark -> when {
                        activeLevel >= 3 -> Color(0xFF00897B)
                        activeLevel == 2 -> Color(0xFFE65100)
                        activeLevel == 1 -> Color(0xFFC62828)
                        else -> Color(0xFFD50000)
                    }
                    activeLevel >= 3 -> Color(0xFF00E5FF)
                    activeLevel == 2 -> Color(0xFFFFD600)
                    activeLevel == 1 -> Color(0xFFFF5252)
                    else -> Color(0xFFFF1744)
                }

                for (i in 0 until 4) {
                    val angleDeg = dotAngles[i]
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val dx = (cx + radius * cos(rad)).toFloat()
                    val dy = (cy + radius * sin(rad)).toFloat()
                    val isLit = i < activeLevel
                    val dotRadius = baseDotRadius * sizeMultipliers[i]

                    if (isLit) {
                        drawCircle(
                            color = activeDotColor,
                            radius = dotRadius,
                            center = Offset(dx, dy)
                        )
                    } else {
                        drawCircle(
                            color = trackColor,
                            radius = dotRadius,
                            center = Offset(dx, dy)
                        )
                    }
                }
            }

            // --- 3. Center or Satellite / Flanking Content ---
            if (showCenterOverLens) {
                drawCenterGlyphCompose(this, cx, cy, radius, strokeWidth, centerMode, primaryFgColor)
            } else if (isWrapCutout) {
                when (settings.cameraCutoutStyle) {
                    CameraCutoutStyle.ORBITAL_SATELLITE -> {
                        val satCy = cy + radius + 8.dp.toPx()
                        drawCenterGlyphCompose(this, cx, satCy, radius * 0.48f, strokeWidth * 0.7f, centerMode, primaryFgColor)
                    }
                    CameraCutoutStyle.FLANKING_WINGS -> {
                        val leftCx = cx - radius - 11.dp.toPx()
                        val rightCx = cx + radius + 11.dp.toPx()

                        // Left wing: Wi-Fi / DND (floating)
                        drawCenterGlyphCompose(this, leftCx, cy, radius * 0.45f, strokeWidth * 0.65f, centerMode, primaryFgColor)

                        // Right wing: 5G / 4G (floating)
                        val textPaint = android.graphics.Paint().apply {
                            color = primaryFgColor.toArgb()
                            textSize = radius * 0.42f
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                            setShadowLayer(3f, 0f, 1f, if (isDark) android.graphics.Color.argb(100, 0, 0, 0) else android.graphics.Color.argb(80, 255, 255, 255))
                        }
                        val yOffset = (textPaint.descent() + textPaint.ascent()) / 2f
                        val cellText = if (status.is5G) "5G" else "4G"
                        drawContext.canvas.nativeCanvas.drawText(cellText, rightCx, cy - yOffset, textPaint)
                    }
                    else -> {}
                }
            }
        }
    }
    }

    val baseModifier = modifier
        .testTag("duo_status_indicator")
        .semantics {
            contentDescription = "Battery ${status.batteryPercent}%, Cell signal ${status.cellularSignalLevel} of 4 dots"
        }
        .scaleWithLayout(scaleFactor)
        .alpha(opacityFactor)

    if (settings.showTimeOnLeft && !settings.timeIndependentMovement) {
        Row(
            modifier = baseModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            timePill()
            indicatorCanvas()
        }
    } else {
        Box(
            modifier = baseModifier,
            contentAlignment = Alignment.Center
        ) {
            indicatorCanvas()
        }
    }
}

/**
 * Dedicated Composable for rendering the floating time pill separately
 * when timeIndependentMovement is enabled.
 */
@Composable
fun DuoTimePill(
    status: DeviceStatus,
    settings: OverlaySettings,
    modifier: Modifier = Modifier,
    isBehindLight: Boolean? = null
) {
    val behindIsLight = isBehindLight ?: status.isBehindLight
    val isDark = when (settings.colorMode) {
        IndicatorColorMode.AUTO -> !behindIsLight
        IndicatorColorMode.DARK, IndicatorColorMode.JUST_WHITE -> true
        IndicatorColorMode.LIGHT -> false
        IndicatorColorMode.CYBER_MINT, IndicatorColorMode.ELECTRIC_CYAN -> true
    }

    val primaryFgColor by animateColorAsState(
        targetValue = when {
            settings.colorMode == IndicatorColorMode.JUST_WHITE -> Color.White
            settings.colorMode == IndicatorColorMode.CYBER_MINT -> Color(0xFF00E676)
            settings.colorMode == IndicatorColorMode.ELECTRIC_CYAN -> Color(0xFF00E5FF)
            isDark -> Color.White
            else -> Color(0xFF14171F)
        },
        label = "timePillFgColor"
    )

    val context = LocalContext.current
    val currentTime = if (status.currentTimeText.isNotEmpty()) {
        status.currentTimeText
    } else {
        val is24 = android.text.format.DateFormat.is24HourFormat(context)
        val sdf = remember(is24) { java.text.SimpleDateFormat(if (is24) "HH:mm" else "h:mm", java.util.Locale.getDefault()) }
        remember { sdf.format(java.util.Date()) }
    }

    val scaleFactor = (settings.scalePercent / 100f).coerceIn(0.25f, 1.5f)
    val opacityFactor = (settings.opacityPercent / 100f).coerceIn(0.4f, 1.0f)
    val timeScaleMultiplier = (settings.timeTextScalePercent / 100f).coerceIn(0.7f, 2.0f)

    Box(
        modifier = modifier
            .testTag("independent_time_pill")
            .scaleWithLayout(scaleFactor)
            .alpha(opacityFactor)
            .width((64 * timeScaleMultiplier).dp)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = currentTime,
            color = primaryFgColor,
            fontSize = (16 * timeScaleMultiplier).sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                fontFeatureSettings = "tnum",
                letterSpacing = (-0.5).sp,
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = if (isDark) Color.Black.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.40f),
                    blurRadius = 3f
                )
            )
        )
    }
}

/**
 * Scales the composable while simultaneously adjusting its layout measurement so the parent
 * container receives the true visual dimensions and origin, avoiding invisible margins or clipping.
 */
private fun Modifier.scaleWithLayout(scale: Float): Modifier {
    if (kotlin.math.abs(scale - 1.0f) < 0.001f) return this
    return this.layout { measurable, constraints ->
        val childConstraints = constraints.copy(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        val placeable = measurable.measure(childConstraints)
        val scaledWidth = kotlin.math.round((placeable.width * scale)).toInt().coerceAtLeast(1)
        val scaledHeight = kotlin.math.round((placeable.height * scale)).toInt().coerceAtLeast(1)
        layout(scaledWidth, scaledHeight) {
            placeable.placeWithLayer(0, 0) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}

private fun drawCenterGlyphCompose(
    drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
    cx: Float,
    cy: Float,
    radius: Float,
    strokeWidth: Float,
    centerMode: IndicatorCenterMode,
    primaryFgColor: Color
) {
    with(drawScope) {
        when (centerMode) {
            IndicatorCenterMode.WIFI -> {
                val wifiBottomY = cy + radius * 0.32f
                val dotR = strokeWidth * 0.44f

                drawCircle(
                    color = primaryFgColor,
                    radius = dotR,
                    center = Offset(cx, wifiBottomY)
                )

                val waveStroke = strokeWidth * 0.72f
                val rMid = radius * 0.36f
                drawArc(
                    color = primaryFgColor,
                    startAngle = 220f,
                    sweepAngle = 100f,
                    useCenter = false,
                    topLeft = Offset(cx - rMid, wifiBottomY - rMid),
                    size = Size(rMid * 2f, rMid * 2f),
                    style = Stroke(width = waveStroke, cap = StrokeCap.Round)
                )

                val rOuter = radius * 0.60f
                drawArc(
                    color = primaryFgColor,
                    startAngle = 225f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx - rOuter, wifiBottomY - rOuter),
                    size = Size(rOuter * 2f, rOuter * 2f),
                    style = Stroke(width = waveStroke, cap = StrokeCap.Round)
                )
            }

            IndicatorCenterMode.DND -> {
                val dndRadius = radius * 0.42f
                val dndStroke = strokeWidth * 0.72f

                drawCircle(
                    color = primaryFgColor,
                    radius = dndRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = dndStroke)
                )

                drawLine(
                    color = primaryFgColor,
                    start = Offset(cx - dndRadius * 0.65f, cy),
                    end = Offset(cx + dndRadius * 0.65f, cy),
                    strokeWidth = dndStroke,
                    cap = StrokeCap.Round
                )
            }

            IndicatorCenterMode.SILENT -> {
                val silentSize = radius * 0.44f
                val silentStroke = strokeWidth * 0.72f

                val path = Path().apply {
                    moveTo(cx, cy - silentSize * 0.8f)
                    quadraticTo(cx - silentSize * 0.6f, cy, cx - silentSize * 0.7f, cy + silentSize * 0.5f)
                    lineTo(cx + silentSize * 0.7f, cy + silentSize * 0.5f)
                    quadraticTo(cx + silentSize * 0.6f, cy, cx, cy - silentSize * 0.8f)
                }
                drawPath(
                    path = path,
                    color = primaryFgColor,
                    style = Stroke(width = silentStroke, cap = StrokeCap.Round)
                )

                drawCircle(
                    color = primaryFgColor,
                    radius = strokeWidth * 0.35f,
                    center = Offset(cx, cy + silentSize * 0.65f)
                )

                drawLine(
                    color = primaryFgColor,
                    start = Offset(cx - silentSize * 0.75f, cy - silentSize * 0.75f),
                    end = Offset(cx + silentSize * 0.75f, cy + silentSize * 0.75f),
                    strokeWidth = silentStroke,
                    cap = StrokeCap.Round
                )
            }

            IndicatorCenterMode.CELLULAR_5G -> {
                val textPaint = android.graphics.Paint().apply {
                    color = primaryFgColor.hashCode()
                    textSize = radius * 0.70f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                val yOffset = (textPaint.descent() + textPaint.ascent()) / 2f
                drawContext.canvas.nativeCanvas.drawText("5G", cx, cy - yOffset, textPaint)
            }

            IndicatorCenterMode.CELLULAR_4G -> {
                val textPaint = android.graphics.Paint().apply {
                    color = primaryFgColor.hashCode()
                    textSize = radius * 0.70f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                val yOffset = (textPaint.descent() + textPaint.ascent()) / 2f
                drawContext.canvas.nativeCanvas.drawText("4G", cx, cy - yOffset, textPaint)
            }
        }
    }
}
