package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.DialogProperties
import com.example.model.CameraCutoutStyle
import com.example.model.DeviceStatus
import com.example.model.FoldScreenMode
import com.example.model.OverlaySettings
import com.example.model.PhoneCutoutType
import com.example.model.PositionPreset
import kotlin.math.roundToInt

enum class FoldDisplayPreview {
    COVER_SCREEN,
    INNER_SCREEN
}

enum class StageViewMode {
    STATUS_BAR_ZOOM,
    PHONE_MOCKUP
}

enum class StageDeviceProfile(
    val label: String,
    val shortName: String,
    val coverWidthDp: Int,
    val coverHeightDp: Int,
    val innerWidthDp: Int,
    val innerHeightDp: Int,
    val coverCutoutCenterX: Int,
    val innerUdcCenterX: Int,
    val statusBarCenterYDp: Int
) {
    LIVE_DEVICE("📱 Current Phone (Live Metrics)", "Live Device", 0, 0, 0, 0, 0, 0, 0),
    Z_FOLD_6_7_8("Galaxy Z Fold 6 / 7 / 8", "Fold 6/7/8", 402, 960, 768, 880, 201, 576, 18),
    Z_FOLD_4_5("Galaxy Z Fold 4 / 5", "Fold 4/5", 374, 904, 720, 884, 187, 540, 18),
    STANDARD_PHONE("Standard Phone (19.5:9)", "Standard", 393, 852, 393, 852, 196, 196, 18)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveInteractiveStage(
    status: DeviceStatus,
    settings: OverlaySettings,
    onPositionChanged: (preset: PositionPreset, offsetX: Int, offsetY: Int) -> Unit,
    onCutoutChanged: (type: PhoneCutoutType, sizeDp: Int, offsetXDp: Int) -> Unit = { _, _, _ -> },
    onInnerPositionChanged: (offsetX: Int, offsetY: Int) -> Unit = { _, _ -> },
    onFoldedRotatedPositionChanged: (offsetX: Int, offsetY: Int) -> Unit = { _, _ -> },
    onInnerRotatedPositionChanged: (offsetX: Int, offsetY: Int) -> Unit = { _, _ -> },
    onFoldModeChanged: (FoldScreenMode) -> Unit = {},
    onCameraCutoutStyleChanged: (CameraCutoutStyle) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(true) }
    val chevronRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "stageChevron")

    val configuration = LocalConfiguration.current
    val actualScreenWidthDp = configuration.screenWidthDp
    val actualScreenHeightDp = configuration.screenHeightDp
    val actualSmallestWidthDp = configuration.smallestScreenWidthDp
    val isActualDeviceUnfolded = actualSmallestWidthDp >= 600
    val isActualRotated = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val actualStatusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val actualStatusBarYDp = if (actualStatusBarPadding.value > 0f) {
        (actualStatusBarPadding.value / 2f).roundToInt().coerceIn(12, 32)
    } else {
        18
    }

    var foldPreview by remember(isActualDeviceUnfolded) {
        mutableStateOf(if (isActualDeviceUnfolded) FoldDisplayPreview.INNER_SCREEN else FoldDisplayPreview.COVER_SCREEN)
    }
    var isStageRotated by remember(isActualRotated) {
        mutableStateOf(isActualRotated)
    }
    var viewMode by remember { mutableStateOf(StageViewMode.STATUS_BAR_ZOOM) }
    var isFullscreenTestOpen by remember { mutableStateOf(false) }
    var showCutoutCustomizer by remember { mutableStateOf(false) }

    var selectedProfile by rememberSaveable { mutableStateOf(StageDeviceProfile.LIVE_DEVICE) }

    val activeCoverW = if (selectedProfile == StageDeviceProfile.LIVE_DEVICE) {
        (if (actualScreenWidthDp > actualScreenHeightDp) actualScreenHeightDp else actualScreenWidthDp).toFloat()
    } else {
        selectedProfile.coverWidthDp.toFloat()
    }

    val activeCoverH = if (selectedProfile == StageDeviceProfile.LIVE_DEVICE) {
        (if (actualScreenWidthDp > actualScreenHeightDp) actualScreenWidthDp else actualScreenHeightDp).toFloat()
    } else {
        selectedProfile.coverHeightDp.toFloat()
    }

    val activeInnerW = if (selectedProfile == StageDeviceProfile.LIVE_DEVICE) {
        (if (isActualDeviceUnfolded) actualScreenWidthDp else 768).toFloat()
    } else {
        selectedProfile.innerWidthDp.toFloat()
    }

    val activeInnerH = if (selectedProfile == StageDeviceProfile.LIVE_DEVICE) {
        (if (isActualDeviceUnfolded) actualScreenHeightDp else 880).toFloat()
    } else {
        selectedProfile.innerHeightDp.toFloat()
    }

    val activeStatusBarCenterYDp = if (selectedProfile == StageDeviceProfile.LIVE_DEVICE) {
        actualStatusBarYDp.toFloat()
    } else {
        selectedProfile.statusBarCenterYDp.toFloat()
    }

    val activeCoverCutoutCenterX = (activeCoverW / 2f).roundToInt()
    val activeInnerUdcCenterX = (activeInnerW * 0.75f).roundToInt()

    var wallpaperIndex by remember { mutableIntStateOf(0) }
    val wallpapers = listOf(
        Pair("Dark Titanium", Brush.verticalGradient(listOf(Color(0xFF0C0E14), Color(0xFF181C26)))),
        Pair("Galaxy Mystic", Brush.verticalGradient(listOf(Color(0xFF1E1430), Color(0xFF0F172A)))),
        Pair("Deep Navy", Brush.verticalGradient(listOf(Color(0xFF0A192F), Color(0xFF020C1B)))),
        Pair("Crisp Light", Brush.verticalGradient(listOf(Color(0xFFF1F3F6), Color(0xFFDCE1E8))))
    )
    val currentBrush = wallpapers[wallpaperIndex % wallpapers.size].second
    val isLightWallpaper = wallpaperIndex % wallpapers.size == 3

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_stage_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Galaxy Z Fold & Device Stage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Hardware-Aligned",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Text(
                        text = "Calibrated for actual phone hardware, Galaxy Z Fold 4/5/6/7/8 or live detected display",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isExpanded) {
                        IconButton(
                            onClick = { wallpaperIndex = (wallpaperIndex + 1) % wallpapers.size },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatPaint,
                                contentDescription = "Wallpaper",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { showCutoutCustomizer = !showCutoutCustomizer },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Cutout Options",
                                modifier = Modifier.size(18.dp),
                                tint = if (showCutoutCustomizer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { isFullscreenTestOpen = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Full Screen Calibration",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse Stage" else "Expand Stage",
                            modifier = Modifier.rotate(chevronRotation),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))

                    // One-Tap Hardware Auto-Align Button & Device Profiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                selectedProfile = StageDeviceProfile.LIVE_DEVICE
                                val targetY = actualStatusBarYDp
                                if (foldPreview == FoldDisplayPreview.COVER_SCREEN) {
                                    val targetX = (activeCoverW / 2f).roundToInt()
                                    onCutoutChanged(PhoneCutoutType.Z_FOLD_COVER_PUNCH_HOLE, 12, 0)
                                    if (isStageRotated) {
                                        onFoldedRotatedPositionChanged(targetX, targetY)
                                    } else {
                                        onPositionChanged(PositionPreset.WRAP_CUTOUT, targetX, targetY)
                                    }
                                } else {
                                    val targetX = (activeInnerW * 0.75f).roundToInt()
                                    onCutoutChanged(PhoneCutoutType.Z_FOLD_INNER_UDC, 11, 0)
                                    if (isStageRotated) {
                                        onInnerRotatedPositionChanged(targetX, targetY)
                                    } else {
                                        onInnerPositionChanged(targetX, targetY)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Auto-Align to Actual Phone",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Device Profile Selector Chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        StageDeviceProfile.values().forEach { profile ->
                            val isSelected = selectedProfile == profile
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedProfile = profile },
                                label = {
                                    Text(
                                        text = if (profile == StageDeviceProfile.LIVE_DEVICE) {
                                            "📱 Live Device (${actualScreenWidthDp}×${actualScreenHeightDp}dp)"
                                        } else {
                                            profile.label
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

            // Dual Screen Switcher: Cover Screen (Folded) vs Inner Display (Unfolded)
            TabRow(
                selectedTabIndex = if (foldPreview == FoldDisplayPreview.COVER_SCREEN) 0 else 1,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = foldPreview == FoldDisplayPreview.COVER_SCREEN,
                    onClick = {
                        foldPreview = FoldDisplayPreview.COVER_SCREEN
                        onFoldModeChanged(FoldScreenMode.COVER_SCREEN)
                        onCutoutChanged(PhoneCutoutType.Z_FOLD_COVER_PUNCH_HOLE, settings.cutoutSizeDp, settings.cutoutOffsetXDp)
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Cover Screen (Folded)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
                Tab(
                    selected = foldPreview == FoldDisplayPreview.INNER_SCREEN,
                    onClick = {
                        foldPreview = FoldDisplayPreview.INNER_SCREEN
                        onFoldModeChanged(FoldScreenMode.INNER_SCREEN)
                        onCutoutChanged(PhoneCutoutType.Z_FOLD_INNER_UDC, settings.cutoutSizeDp, settings.cutoutOffsetXDp)
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Tablet, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Inner Screen (Unfolded)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-bar: Orientation Selector + View Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Orientation Switcher: Upright vs Rotated
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = !isStageRotated,
                        onClick = { isStageRotated = false },
                        label = { Text("📱 Upright", fontSize = 11.sp, fontWeight = if (!isStageRotated) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = isStageRotated,
                        onClick = { isStageRotated = true },
                        label = { Text("🔄 Rotated", fontSize = 11.sp, fontWeight = if (isStageRotated) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                // Sub-view: Status Bar Zoom vs Whole Device
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = viewMode == StageViewMode.STATUS_BAR_ZOOM,
                        onClick = { viewMode = StageViewMode.STATUS_BAR_ZOOM },
                        label = { Text("1:1 Zoom", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.ViewStream, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                    FilterChip(
                        selected = viewMode == StageViewMode.PHONE_MOCKUP,
                        onClick = { viewMode = StageViewMode.PHONE_MOCKUP },
                        label = { Text("Chassis", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }
            }

            // Cutout Customizer Accordion
            AnimatedVisibility(visible = showCutoutCustomizer) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Camera Cutout / UDC Configuration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CutoutChip("Z Fold Cover Hole", PhoneCutoutType.Z_FOLD_COVER_PUNCH_HOLE, settings.cutoutType) {
                                onCutoutChanged(it, 12, 0)
                            }
                            CutoutChip("Z Fold Inner UDC", PhoneCutoutType.Z_FOLD_INNER_UDC, settings.cutoutType) {
                                onCutoutChanged(it, 11, 0)
                            }
                            CutoutChip("Center Hole", PhoneCutoutType.CENTER_PUNCH_HOLE, settings.cutoutType) {
                                onCutoutChanged(it, settings.cutoutSizeDp, 0)
                            }
                            CutoutChip("Left Hole", PhoneCutoutType.LEFT_PUNCH_HOLE, settings.cutoutType) {
                                onCutoutChanged(it, settings.cutoutSizeDp, 0)
                            }
                            CutoutChip("Right Hole", PhoneCutoutType.RIGHT_PUNCH_HOLE, settings.cutoutType) {
                                onCutoutChanged(it, settings.cutoutSizeDp, 0)
                            }
                            CutoutChip("No Cutout", PhoneCutoutType.NO_CUTOUT, settings.cutoutType) {
                                onCutoutChanged(it, 0, 0)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Cutout Lens Diameter: ${settings.cutoutSizeDp} dp", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Slider(
                            value = settings.cutoutSizeDp.toFloat(),
                            onValueChange = { onCutoutChanged(settings.cutoutType, it.toInt(), settings.cutoutOffsetXDp) },
                            valueRange = 8f..22f,
                            steps = 7
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- STAGE DISPLAY AREA ---
            if (foldPreview == FoldDisplayPreview.COVER_SCREEN) {
                // Galaxy Z Fold Cover Screen (Tall & Narrow aspect ratio)
                if (viewMode == StageViewMode.STATUS_BAR_ZOOM) {
                    CoverStatusBarCanvas(
                        status = status,
                        settings = settings,
                        currentBrush = currentBrush,
                        isLightWallpaper = isLightWallpaper,
                        isRotated = isStageRotated,
                        coverWidthDp = activeCoverW,
                        coverHeightDp = activeCoverH,
                        statusBarCenterYDp = activeStatusBarCenterYDp,
                        onPositionChanged = onPositionChanged,
                        onFoldedRotatedPositionChanged = onFoldedRotatedPositionChanged
                    )
                } else {
                    CoverPhoneMockupCanvas(
                        status = status,
                        settings = settings,
                        currentBrush = currentBrush,
                        isLightWallpaper = isLightWallpaper,
                        isRotated = isStageRotated,
                        coverWidthDp = activeCoverW,
                        coverHeightDp = activeCoverH,
                        statusBarCenterYDp = activeStatusBarCenterYDp,
                        onPositionChanged = onPositionChanged,
                        onFoldedRotatedPositionChanged = onFoldedRotatedPositionChanged
                    )
                }
            } else {
                // Galaxy Z Fold Inner Main Screen (Large square tablet with crease & UDC)
                if (viewMode == StageViewMode.STATUS_BAR_ZOOM) {
                    InnerStatusBarCanvas(
                        status = status,
                        settings = settings,
                        currentBrush = currentBrush,
                        isLightWallpaper = isLightWallpaper,
                        isRotated = isStageRotated,
                        innerWidthDp = activeInnerW,
                        innerHeightDp = activeInnerH,
                        statusBarCenterYDp = activeStatusBarCenterYDp,
                        onInnerPositionChanged = onInnerPositionChanged,
                        onInnerRotatedPositionChanged = onInnerRotatedPositionChanged
                    )
                } else {
                    InnerTabletMockupCanvas(
                        status = status,
                        settings = settings,
                        currentBrush = currentBrush,
                        isLightWallpaper = isLightWallpaper,
                        isRotated = isStageRotated,
                        innerWidthDp = activeInnerW,
                        innerHeightDp = activeInnerH,
                        statusBarCenterYDp = activeStatusBarCenterYDp,
                        onInnerPositionChanged = onInnerPositionChanged,
                        onInnerRotatedPositionChanged = onInnerRotatedPositionChanged
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val currentTargetLabel = when {
                foldPreview == FoldDisplayPreview.COVER_SCREEN && !isStageRotated -> "Cover Screen (Folded Upright)"
                foldPreview == FoldDisplayPreview.COVER_SCREEN && isStageRotated -> "Cover Screen (Folded Rotated)"
                foldPreview == FoldDisplayPreview.INNER_SCREEN && !isStageRotated -> "Inner Screen (Unfolded Upright)"
                else -> "Inner Screen (Unfolded Rotated)"
            }

            // Fast Snapping Presets based on active profile / hardware
            Text(
                text = "$currentTargetLabel Fast Presets",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when {
                    foldPreview == FoldDisplayPreview.COVER_SCREEN && !isStageRotated -> {
                        AssistChip(
                            onClick = {
                                onCutoutChanged(PhoneCutoutType.Z_FOLD_COVER_PUNCH_HOLE, 12, 0)
                                onPositionChanged(PositionPreset.WRAP_CUTOUT, activeCoverCutoutCenterX, activeStatusBarCenterYDp.toInt())
                            },
                            label = { Text("🎯 Wrap Cover Punch-Hole ($activeCoverCutoutCenterX, ${activeStatusBarCenterYDp.toInt()})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = if (settings.cutoutType == PhoneCutoutType.Z_FOLD_COVER_PUNCH_HOLE && settings.positionPreset == PositionPreset.WRAP_CUTOUT) {
                                AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            } else AssistChipDefaults.assistChipColors()
                        )

                        AssistChip(
                            onClick = { onPositionChanged(PositionPreset.TOP_RIGHT, (activeCoverW - 32f).roundToInt(), activeStatusBarCenterYDp.toInt()) },
                            label = { Text("Cover Status Bar Right", fontSize = 11.sp) }
                        )

                        AssistChip(
                            onClick = { onPositionChanged(PositionPreset.TOP_LEFT, 78, activeStatusBarCenterYDp.toInt()) },
                            label = { Text("Beside Clock (Left)", fontSize = 11.sp) }
                        )
                    }
                    foldPreview == FoldDisplayPreview.COVER_SCREEN && isStageRotated -> {
                        AssistChip(
                            onClick = { onFoldedRotatedPositionChanged((activeCoverH - 40f).roundToInt(), activeStatusBarCenterYDp.toInt()) },
                            label = { Text("Landscape Top-Right", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )

                        AssistChip(
                            onClick = { onFoldedRotatedPositionChanged(70, activeStatusBarCenterYDp.toInt()) },
                            label = { Text("Beside Cutout (70)", fontSize = 11.sp) }
                        )

                        AssistChip(
                            onClick = { onFoldedRotatedPositionChanged(32, activeStatusBarCenterYDp.toInt()) },
                            label = { Text("Top-Left Margin", fontSize = 11.sp) }
                        )
                    }
                    foldPreview == FoldDisplayPreview.INNER_SCREEN && !isStageRotated -> {
                        AssistChip(
                            onClick = {
                                onCutoutChanged(PhoneCutoutType.Z_FOLD_INNER_UDC, 11, 0)
                                onInnerPositionChanged(activeInnerUdcCenterX, activeStatusBarCenterYDp.toInt())
                            },
                            label = { Text("👁️ Wrap Inner UDC ($activeInnerUdcCenterX, ${activeStatusBarCenterYDp.toInt()})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = if (settings.cutoutType == PhoneCutoutType.Z_FOLD_INNER_UDC) {
                                AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            } else AssistChipDefaults.assistChipColors()
                        )

                        AssistChip(
                            onClick = {
                                onInnerPositionChanged((activeInnerW - 40f).roundToInt(), activeStatusBarCenterYDp.toInt())
                            },
                            label = { Text("Inner Top-Right", fontSize = 11.sp) }
                        )

                        AssistChip(
                            onClick = {
                                onInnerPositionChanged(84, activeStatusBarCenterYDp.toInt())
                                onPositionChanged(PositionPreset.CUSTOM, 84, activeStatusBarCenterYDp.toInt())
                            },
                            label = { Text("Beside Clock (Inner)", fontSize = 11.sp) }
                        )
                    }
                    foldPreview == FoldDisplayPreview.INNER_SCREEN && isStageRotated -> {
                        AssistChip(
                            onClick = { onInnerRotatedPositionChanged((activeInnerH - 50f).roundToInt(), activeStatusBarCenterYDp.toInt()) },
                            label = { Text("Landscape Inner Top-Right", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )

                        AssistChip(
                            onClick = { onInnerRotatedPositionChanged((activeInnerH * 0.75f).roundToInt(), activeStatusBarCenterYDp.toInt()) },
                            label = { Text("Beside UDC Camera", fontSize = 11.sp) }
                        )

                        AssistChip(
                            onClick = { onInnerRotatedPositionChanged(80, activeStatusBarCenterYDp.toInt()) },
                            label = { Text("Top-Left Header", fontSize = 11.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stepper controls per screen & rotation state
            when {
                foldPreview == FoldDisplayPreview.COVER_SCREEN && !isStageRotated -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FineTuneStepper(
                            label = "Cover Upright X Offset",
                            value = settings.offsetX,
                            onValueChange = { onPositionChanged(PositionPreset.CUSTOM, it, settings.offsetY) },
                            max = activeCoverW.roundToInt().coerceAtLeast(400)
                        )
                        FineTuneStepper(
                            label = "Cover Upright Y Offset",
                            value = settings.offsetY,
                            onValueChange = { onPositionChanged(PositionPreset.CUSTOM, settings.offsetX, it) },
                            max = 100
                        )
                    }
                }
                foldPreview == FoldDisplayPreview.COVER_SCREEN && isStageRotated -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FineTuneStepper(
                            label = "Folded Rotated X Offset",
                            value = settings.foldedRotatedOffsetX,
                            onValueChange = { onFoldedRotatedPositionChanged(it, settings.foldedRotatedOffsetY) },
                            max = activeCoverH.roundToInt().coerceAtLeast(400)
                        )
                        FineTuneStepper(
                            label = "Folded Rotated Y Offset",
                            value = settings.foldedRotatedOffsetY,
                            onValueChange = { onFoldedRotatedPositionChanged(settings.foldedRotatedOffsetX, it) },
                            max = 100
                        )
                    }
                }
                foldPreview == FoldDisplayPreview.INNER_SCREEN && !isStageRotated -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FineTuneStepper(
                            label = "Inner Upright X Offset",
                            value = settings.innerOffsetX,
                            onValueChange = { onInnerPositionChanged(it, settings.innerOffsetY) },
                            max = activeInnerW.roundToInt().coerceAtLeast(600)
                        )
                        FineTuneStepper(
                            label = "Inner Upright Y Offset",
                            value = settings.innerOffsetY,
                            onValueChange = { onInnerPositionChanged(settings.innerOffsetX, it) },
                            max = 100
                        )
                    }
                }
                foldPreview == FoldDisplayPreview.INNER_SCREEN && isStageRotated -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FineTuneStepper(
                            label = "Inner Rotated X Offset",
                            value = settings.innerRotatedOffsetX,
                            onValueChange = { onInnerRotatedPositionChanged(it, settings.innerRotatedOffsetY) },
                            max = activeInnerH.roundToInt().coerceAtLeast(600)
                        )
                        FineTuneStepper(
                            label = "Inner Rotated Y Offset",
                            value = settings.innerRotatedOffsetY,
                            onValueChange = { onInnerRotatedPositionChanged(settings.innerRotatedOffsetX, it) },
                            max = 100
                        )
                    }
                }
            }
        }
    }
}
    }

    // --- FULLSCREEN LIVE HARDWARE CALIBRATION DIALOG ---
    if (isFullscreenTestOpen) {
        Dialog(
            onDismissRequest = { isFullscreenTestOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
            ) {
                val density = LocalDensity.current
                val maxW = constraints.maxWidth.toFloat()
                val maxH = constraints.maxHeight.toFloat()
                val isScreenRotated = maxW > maxH
                val smallestWidthDp = kotlin.math.min(maxW, maxH) / density.density
                val isScreenUnfolded = smallestWidthDp >= 600

                val scale = (settings.scalePercent / 100f).coerceIn(0.25f, 1.5f)
                val baseDimPx = with(density) { (54.dp * scale).toPx() }
                val ringRadiusPx = baseDimPx / 2f
                val circleWPx = with(density) {
                    (if (settings.positionPreset == PositionPreset.WRAP_CUTOUT && settings.cameraCutoutStyle == CameraCutoutStyle.FLANKING_WINGS) 92.dp else 54.dp).toPx() * scale
                }
                val circleHPx = with(density) {
                    (if (settings.positionPreset == PositionPreset.WRAP_CUTOUT && settings.cameraCutoutStyle == CameraCutoutStyle.ORBITAL_SATELLITE) 70.dp else 54.dp).toPx() * scale
                }
                val timeScaleMultiplier = (settings.timeTextScalePercent / 100f).coerceIn(0.7f, 2.0f)
                val timeOffsetDp = (64.dp * timeScaleMultiplier) + 6.dp
                val indicatorWidthPx = with(density) {
                    (if (settings.showTimeOnLeft) (timeOffsetDp * scale).toPx() + circleWPx else circleWPx)
                }
                val indicatorHeightPx = circleHPx
                val ringCenterInBoxXPx = if (settings.showTimeOnLeft) with(density) { (timeOffsetDp * scale).toPx() } + (circleWPx / 2f) else (circleWPx / 2f)
                val ringCenterInBoxYPx = circleHPx / 2f

                val (initialXDp, initialYDp) = settings.getCurrentOffset(isScreenUnfolded, isScreenRotated)

                var dragCenterXPx by remember(isScreenUnfolded, isScreenRotated, initialXDp) {
                    mutableFloatStateOf(with(density) { initialXDp.dp.toPx() })
                }
                var dragCenterYPx by remember(isScreenUnfolded, isScreenRotated, initialYDp) {
                    mutableFloatStateOf(with(density) { initialYDp.dp.toPx() })
                }

                Box(
                    modifier = Modifier
                        .offset {
                            val left = (dragCenterXPx - ringCenterInBoxXPx).roundToInt()
                            val top = (dragCenterYPx - ringCenterInBoxYPx).roundToInt()
                            IntOffset(left, top)
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragCenterXPx = (dragCenterXPx + dragAmount.x).coerceIn(0f, maxW)
                                    dragCenterYPx = (dragCenterYPx + dragAmount.y).coerceIn(0f, maxH)
                                },
                                onDragEnd = {
                                    val xDp = with(density) { dragCenterXPx.toDp().value.roundToInt() }
                                    val yDp = with(density) { dragCenterYPx.toDp().value.roundToInt() }
                                    when {
                                        isScreenUnfolded && isScreenRotated -> onInnerRotatedPositionChanged(xDp, yDp)
                                        isScreenUnfolded && !isScreenRotated -> onInnerPositionChanged(xDp, yDp)
                                        !isScreenUnfolded && isScreenRotated -> onFoldedRotatedPositionChanged(xDp, yDp)
                                        else -> onPositionChanged(PositionPreset.CUSTOM, xDp, yDp)
                                    }
                                }
                            )
                        }
                ) {
                    DuoStatusIndicator(
                        status = status,
                        settings = settings
                    )
                }

                val screenStateLabel = when {
                    isScreenUnfolded && isScreenRotated -> "Inner Screen (Unfolded Rotated / Landscape)"
                    isScreenUnfolded && !isScreenRotated -> "Inner Screen (Unfolded Upright / Portrait)"
                    !isScreenUnfolded && isScreenRotated -> "Cover Screen (Folded Rotated / Landscape)"
                    else -> "Cover Screen (Folded Upright / Portrait)"
                }

                // Instructions Card at bottom
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$screenStateLabel 1:1 Live Drag",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Ring Center: X = ${with(density) { dragCenterXPx.toDp().value.roundToInt() }}dp, Y = ${with(density) { dragCenterYPx.toDp().value.roundToInt() }}dp. Drag ring directly into position.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = {
                                    dragCenterXPx = maxW / 2f
                                    dragCenterYPx = with(density) { actualStatusBarYDp.dp.toPx() }
                                    val xDp = with(density) { dragCenterXPx.toDp().value.roundToInt() }
                                    val yDp = actualStatusBarYDp
                                    if (!isScreenUnfolded && !isScreenRotated) {
                                        onPositionChanged(PositionPreset.WRAP_CUTOUT, xDp, yDp)
                                    } else if (!isScreenUnfolded && isScreenRotated) {
                                        onFoldedRotatedPositionChanged(xDp, yDp)
                                    } else if (isScreenUnfolded && !isScreenRotated) {
                                        onInnerPositionChanged(xDp, yDp)
                                    } else {
                                        onInnerRotatedPositionChanged(xDp, yDp)
                                    }
                                },
                                label = { Text("🎯 Center Punch-Hole", fontSize = 11.sp) }
                            )
                            if (isScreenUnfolded) {
                                AssistChip(
                                    onClick = {
                                        dragCenterXPx = maxW * 0.75f
                                        dragCenterYPx = with(density) { actualStatusBarYDp.dp.toPx() }
                                        val xDp = with(density) { dragCenterXPx.toDp().value.roundToInt() }
                                        val yDp = actualStatusBarYDp
                                        if (!isScreenRotated) {
                                            onInnerPositionChanged(xDp, yDp)
                                            onPositionChanged(PositionPreset.WRAP_CUTOUT, xDp, yDp)
                                        } else {
                                            onInnerRotatedPositionChanged(xDp, yDp)
                                        }
                                    },
                                    label = { Text("👁️ UDC Camera (75%)", fontSize = 11.sp) }
                                )
                            }
                        }
                        Button(
                            onClick = { isFullscreenTestOpen = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Position")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 1:1 Scale Status Bar Strip for Cover Screen
 */
@Composable
private fun CoverStatusBarCanvas(
    status: DeviceStatus,
    settings: OverlaySettings,
    currentBrush: Brush,
    isLightWallpaper: Boolean,
    isRotated: Boolean,
    coverWidthDp: Float = 412f,
    coverHeightDp: Float = 960f,
    statusBarCenterYDp: Float = 18f,
    onPositionChanged: (PositionPreset, Int, Int) -> Unit,
    onFoldedRotatedPositionChanged: (Int, Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(currentBrush)
    ) {
        val density = LocalDensity.current
        val stageWidthPx = constraints.maxWidth.toFloat()
        val stageHeightPx = constraints.maxHeight.toFloat()
        val statusBarCenterYPx = with(density) { 28.dp.toPx() }

        val scale = (settings.scalePercent / 100f).coerceIn(0.25f, 1.5f)
        val baseDimPx = with(density) { (54.dp * scale).toPx() }
        val ringRadiusPx = baseDimPx / 2f
        val circleWPx = with(density) {
            (if (settings.positionPreset == PositionPreset.WRAP_CUTOUT && settings.cameraCutoutStyle == CameraCutoutStyle.FLANKING_WINGS) 92.dp else 54.dp).toPx() * scale
        }
        val circleHPx = with(density) {
            (if (settings.positionPreset == PositionPreset.WRAP_CUTOUT && settings.cameraCutoutStyle == CameraCutoutStyle.ORBITAL_SATELLITE) 70.dp else 54.dp).toPx() * scale
        }
        val timeScaleMultiplier = (settings.timeTextScalePercent / 100f).coerceIn(0.7f, 2.0f)
        val timeOffsetDp = (64.dp * timeScaleMultiplier) + 6.dp
        val indicatorWidthPx = with(density) {
            (if (settings.showTimeOnLeft) (timeOffsetDp * scale).toPx() + circleWPx else circleWPx)
        }
        val indicatorHeightPx = circleHPx
        val ringCenterInBoxXPx = if (settings.showTimeOnLeft) with(density) { (timeOffsetDp * scale).toPx() } + (circleWPx / 2f) else (circleWPx / 2f)
        val ringCenterInBoxYPx = circleHPx / 2f

        val (targetXDp, targetYDp) = if (isRotated) {
            settings.foldedRotatedOffsetX to settings.foldedRotatedOffsetY
        } else {
            settings.offsetX to settings.offsetY
        }

        val realW = if (isRotated) coverHeightDp else coverWidthDp

        val initialRingCenterXPx = ((targetXDp.toFloat() / realW) * stageWidthPx)
        val initialRingCenterYPx = (statusBarCenterYPx + with(density) { (targetYDp - statusBarCenterYDp).dp.toPx() })

        var dragCenterXPx by remember(targetXDp, stageWidthPx, isRotated) {
            mutableFloatStateOf(initialRingCenterXPx)
        }

        var dragCenterYPx by remember(targetYDp, stageHeightPx, isRotated) {
            mutableFloatStateOf(initialRingCenterYPx)
        }

        // Status bar line (vertically centered at 28.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, (statusBarCenterYPx - with(density) { 10.dp.toPx() }).roundToInt()) }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "09:41",
                color = if (isLightWallpaper) Color(0xFF1E2129) else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "5G", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isLightWallpaper) Color(0xFF1E2129) else Color.White)
                Text(text = "88%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isLightWallpaper) Color(0xFF1E2129) else Color.White)
            }
        }

        // Cover Screen Hole Punch
        val cutoutAlignment = if (isRotated) Alignment.TopStart else Alignment.TopCenter
        val cutoutOffsetModifier = if (isRotated) {
            Modifier.offset { IntOffset(with(density) { 36.dp.toPx() }.roundToInt(), (statusBarCenterYPx - with(density) { (settings.cutoutSizeDp / 2f).dp.toPx() }).roundToInt()) }
        } else {
            Modifier.offset { IntOffset(0, (statusBarCenterYPx - with(density) { (settings.cutoutSizeDp / 2f).dp.toPx() }).roundToInt()) }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(cutoutOffsetModifier),
            contentAlignment = cutoutAlignment
        ) {
            Box(
                modifier = Modifier
                    .size(settings.cutoutSizeDp.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(1.2.dp, Color(0x99FFFFFF), CircleShape)
            )
        }

        // Draggable Indicator
        Box(
            modifier = Modifier
                .offset {
                    val left = (dragCenterXPx - ringCenterInBoxXPx).roundToInt()
                    val top = (dragCenterYPx - ringCenterInBoxYPx).roundToInt()
                    IntOffset(left, top)
                }
                .pointerInput(isRotated) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragCenterXPx = (dragCenterXPx + dragAmount.x).coerceIn(0f, stageWidthPx)
                            dragCenterYPx = (dragCenterYPx + dragAmount.y).coerceIn(0f, stageHeightPx)
                        },
                        onDragEnd = {
                            val realX = ((dragCenterXPx / stageWidthPx) * realW).roundToInt().coerceIn(0, realW.toInt())
                            val deltaY = with(density) { (dragCenterYPx - statusBarCenterYPx).toDp().value }
                            val realY = (statusBarCenterYDp + deltaY).roundToInt().coerceIn(0, 120)
                            if (isRotated) {
                                onFoldedRotatedPositionChanged(realX, realY)
                            } else {
                                onPositionChanged(PositionPreset.CUSTOM, realX, realY)
                            }
                        }
                    )
                }
        ) {
            DuoStatusIndicator(
                status = status.copy(isBehindLight = isLightWallpaper),
                settings = settings
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            val stateText = if (isRotated) "Cover (Folded Rotated)" else "Cover (Folded Upright)"
            Text(
                text = "$stateText • Ring Center: X = ${with(density) { dragCenterXPx.toDp().value.roundToInt() }}dp  Y = ${with(density) { dragCenterYPx.toDp().value.roundToInt() }}dp",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

/**
 * 1:1 Scale Status Bar Strip for Inner Display with UDC (Under Display Camera)
 */
@Composable
private fun InnerStatusBarCanvas(
    status: DeviceStatus,
    settings: OverlaySettings,
    currentBrush: Brush,
    isLightWallpaper: Boolean,
    isRotated: Boolean,
    innerWidthDp: Float = 768f,
    innerHeightDp: Float = 880f,
    statusBarCenterYDp: Float = 18f,
    onInnerPositionChanged: (Int, Int) -> Unit,
    onInnerRotatedPositionChanged: (Int, Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(currentBrush)
    ) {
        val density = LocalDensity.current
        val stageWidthPx = constraints.maxWidth.toFloat()
        val stageHeightPx = constraints.maxHeight.toFloat()
        val udcCenterX = stageWidthPx * 0.75f
        val statusBarCenterYPx = with(density) { 28.dp.toPx() }

        val scale = (settings.scalePercent / 100f).coerceIn(0.25f, 1.5f)
        val baseDimPx = with(density) { (54.dp * scale).toPx() }
        val ringRadiusPx = baseDimPx / 2f
        val circleWPx = with(density) {
            (if (settings.positionPreset == PositionPreset.WRAP_CUTOUT && settings.cameraCutoutStyle == CameraCutoutStyle.FLANKING_WINGS) 92.dp else 54.dp).toPx() * scale
        }
        val circleHPx = with(density) {
            (if (settings.positionPreset == PositionPreset.WRAP_CUTOUT && settings.cameraCutoutStyle == CameraCutoutStyle.ORBITAL_SATELLITE) 70.dp else 54.dp).toPx() * scale
        }
        val timeScaleMultiplier = (settings.timeTextScalePercent / 100f).coerceIn(0.7f, 2.0f)
        val timeOffsetDp = (64.dp * timeScaleMultiplier) + 6.dp
        val indicatorWidthPx = with(density) {
            (if (settings.showTimeOnLeft) (timeOffsetDp * scale).toPx() + circleWPx else circleWPx)
        }
        val indicatorHeightPx = circleHPx
        val ringCenterInBoxXPx = if (settings.showTimeOnLeft) with(density) { (timeOffsetDp * scale).toPx() } + (circleWPx / 2f) else (circleWPx / 2f)
        val ringCenterInBoxYPx = circleHPx / 2f

        val (targetXDp, targetYDp) = if (isRotated) {
            settings.innerRotatedOffsetX to settings.innerRotatedOffsetY
        } else {
            settings.innerOffsetX to settings.innerOffsetY
        }

        val realW = if (isRotated) innerHeightDp else innerWidthDp

        val initialRingCenterXPx = ((targetXDp.toFloat() / realW) * stageWidthPx)
        val initialRingCenterYPx = (statusBarCenterYPx + with(density) { (targetYDp - statusBarCenterYDp).dp.toPx() })

        var dragCenterXPx by remember(targetXDp, stageWidthPx, isRotated) {
            mutableFloatStateOf(initialRingCenterXPx)
        }

        var dragCenterYPx by remember(targetYDp, stageHeightPx, isRotated) {
            mutableFloatStateOf(initialRingCenterYPx)
        }

        // Fold Crease indicator
        if (!isRotated) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight()
                    .width(1.5.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )
        }

        // Status bar line (vertically centered at 28.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, (statusBarCenterYPx - with(density) { 10.dp.toPx() }).roundToInt()) }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "09:41",
                color = if (isLightWallpaper) Color(0xFF1E2129) else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "5G", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isLightWallpaper) Color(0xFF1E2129) else Color.White)
                Text(text = "88%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isLightWallpaper) Color(0xFF1E2129) else Color.White)
            }
        }

        // Under-Display Camera (UDC) Graphic on Fold 8 Inner Screen
        val udcPos = if (isRotated) stageWidthPx * 0.85f else udcCenterX
        Box(
            modifier = Modifier
                .offset { IntOffset((udcPos - with(density) { 6.dp.toPx() }).roundToInt(), (statusBarCenterYPx - with(density) { 6.dp.toPx() }).roundToInt()) }
                .size(12.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .border(1.2.dp, Color(0x9900E5FF), CircleShape)
        )

        // Draggable Indicator
        Box(
            modifier = Modifier
                .offset {
                    val left = (dragCenterXPx - ringCenterInBoxXPx).roundToInt()
                    val top = (dragCenterYPx - ringCenterInBoxYPx).roundToInt()
                    IntOffset(left, top)
                }
                .pointerInput(isRotated) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragCenterXPx = (dragCenterXPx + dragAmount.x).coerceIn(0f, stageWidthPx)
                            dragCenterYPx = (dragCenterYPx + dragAmount.y).coerceIn(0f, stageHeightPx)
                        },
                        onDragEnd = {
                            val realX = ((dragCenterXPx / stageWidthPx) * realW).roundToInt().coerceIn(0, realW.toInt())
                            val deltaY = with(density) { (dragCenterYPx - statusBarCenterYPx).toDp().value }
                            val realY = (statusBarCenterYDp + deltaY).roundToInt().coerceIn(0, 120)
                            if (isRotated) {
                                onInnerRotatedPositionChanged(realX, realY)
                            } else {
                                onInnerPositionChanged(realX, realY)
                            }
                        }
                    )
                }
        ) {
            DuoStatusIndicator(
                status = status.copy(isBehindLight = isLightWallpaper),
                settings = settings
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            val stateText = if (isRotated) "Inner Display (Rotated)" else "Inner Display (Upright)"
            Text(
                text = "$stateText: UDC Camera • Ring Center: X = ${with(density) { dragCenterXPx.toDp().value.roundToInt() }}dp  Y = ${with(density) { dragCenterYPx.toDp().value.roundToInt() }}dp",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

/**
 * Full Phone Mockup: Galaxy Z Fold 8 Cover Screen (Folded Narrow 22:9 Form Factor or Rotated)
 */
@Composable
private fun CoverPhoneMockupCanvas(
    status: DeviceStatus,
    settings: OverlaySettings,
    currentBrush: Brush,
    isLightWallpaper: Boolean,
    isRotated: Boolean,
    coverWidthDp: Float = 412f,
    coverHeightDp: Float = 960f,
    statusBarCenterYDp: Float = 18f,
    onPositionChanged: (PositionPreset, Int, Int) -> Unit,
    onFoldedRotatedPositionChanged: (Int, Int) -> Unit
) {
    val chassisWidth = if (isRotated) 270.dp else 150.dp
    val chassisHeight = if (isRotated) 155.dp else 280.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer chassis
        Box(
            modifier = Modifier
                .width(chassisWidth)
                .height(chassisHeight)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF14161C))
                .border(2.5.dp, Color(0xFF3F4452), RoundedCornerShape(26.dp))
                .padding(3.dp)
        ) {
            // Fold Hinge line
            if (!isRotated) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(3.dp)
                        .background(Color(0xFF2A2E39))
                )
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color(0xFF2A2E39))
                )
            }

            // Screen surface
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(22.dp))
                    .background(currentBrush)
            ) {
                val density = LocalDensity.current
                val stageWidthPx = constraints.maxWidth.toFloat()
                val stageHeightPx = constraints.maxHeight.toFloat()

                val (targetXDp, targetYDp) = if (isRotated) {
                    settings.foldedRotatedOffsetX to settings.foldedRotatedOffsetY
                } else {
                    settings.offsetX to settings.offsetY
                }

                val scaledPercent = (settings.scalePercent * (if (isRotated) 0.52f else 0.44f)).toInt().coerceIn(25, 100)
                val scaleFactor = scaledPercent / 100f
                val circleWPx = with(density) {
                    (if (settings.positionPreset == PositionPreset.WRAP_CUTOUT && settings.cameraCutoutStyle == CameraCutoutStyle.FLANKING_WINGS) 92.dp else 54.dp).toPx() * scaleFactor
                }
                val circleHPx = with(density) {
                    (if (settings.positionPreset == PositionPreset.WRAP_CUTOUT && settings.cameraCutoutStyle == CameraCutoutStyle.ORBITAL_SATELLITE) 70.dp else 54.dp).toPx() * scaleFactor
                }
                val timeScaleMultiplier = (settings.timeTextScalePercent / 100f).coerceIn(0.7f, 2.0f)
                val timeOffsetDp = (64.dp * timeScaleMultiplier) + 6.dp
                val indicatorWidthPx = with(density) {
                    (if (settings.showTimeOnLeft) (timeOffsetDp * scaleFactor).toPx() + circleWPx else circleWPx)
                }
                val indicatorHeightPx = circleHPx
                val ringRadiusPx = with(density) { (27.dp * scaleFactor).toPx() }
                val ringCenterInBoxXPx = if (settings.showTimeOnLeft) with(density) { (timeOffsetDp * scaleFactor).toPx() } + (circleWPx / 2f) else (circleWPx / 2f)
                val ringCenterInBoxYPx = circleHPx / 2f

                val realW = if (isRotated) coverHeightDp else coverWidthDp
                val realH = if (isRotated) coverWidthDp else coverHeightDp
                val mockupStatusBarY = with(density) { 10.dp.toPx() }
                val punchHoleXPx = if (isRotated) with(density) { 10.dp.toPx() } else (stageWidthPx / 2f)
                val punchHoleYPx = if (isRotated) (stageHeightPx / 2f) else mockupStatusBarY

                val initialCenterXPx = ((targetXDp.toFloat() / realW) * stageWidthPx)
                val initialCenterYPx = (mockupStatusBarY + with(density) { ((targetYDp - statusBarCenterYDp) * 0.7f).dp.toPx() })

                var dragCenterXPx by remember(targetXDp, stageWidthPx, isRotated) {
                    mutableFloatStateOf(initialCenterXPx)
                }
                var dragCenterYPx by remember(targetYDp, stageHeightPx, isRotated) {
                    mutableFloatStateOf(initialCenterYPx)
                }

                // Status Bar text
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "09:41", color = if (isLightWallpaper) Color(0xFF1E2129) else Color.White, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                    Text(text = "5G 88%", color = if (isLightWallpaper) Color(0xFF1E2129) else Color.White, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                }

                // Camera hole-punch
                Box(
                    modifier = Modifier
                        .align(if (isRotated) Alignment.CenterStart else Alignment.TopCenter)
                        .padding(if (isRotated) PaddingValues(start = 6.dp) else PaddingValues(top = 6.dp))
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .border(0.5.dp, Color(0x66FFFFFF), CircleShape)
                )

                // Home clock widget
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = if (isRotated) 0.dp else 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "09:41", fontSize = if (isRotated) 18.sp else 22.sp, fontWeight = FontWeight.ExtraLight, color = if (isLightWallpaper) Color(0xFF1E2129) else Color.White)
                    Text(text = if (isRotated) "Folded Rotated" else "Cover Display", fontSize = 8.sp, color = if (isLightWallpaper) Color(0xFF555B6E) else Color.White.copy(alpha = 0.7f))
                }

                // Draggable indicator
                Box(
                    modifier = Modifier
                        .offset {
                            val left = (dragCenterXPx - ringCenterInBoxXPx).roundToInt()
                            val top = (dragCenterYPx - ringCenterInBoxYPx).roundToInt()
                            IntOffset(left, top)
                        }
                        .pointerInput(isRotated) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragCenterXPx = (dragCenterXPx + dragAmount.x).coerceIn(0f, stageWidthPx)
                                    dragCenterYPx = (dragCenterYPx + dragAmount.y).coerceIn(0f, stageHeightPx)
                                },
                                onDragEnd = {
                                    val realX = ((dragCenterXPx / stageWidthPx) * realW).roundToInt().coerceIn(0, realW.toInt())
                                    val deltaY = (dragCenterYPx - mockupStatusBarY) / with(density) { 0.7f.dp.toPx() }
                                    val realY = (statusBarCenterYDp + deltaY).roundToInt().coerceIn(0, 120)
                                    if (isRotated) {
                                        onFoldedRotatedPositionChanged(realX, realY)
                                    } else {
                                        onPositionChanged(PositionPreset.CUSTOM, realX, realY)
                                    }
                                }
                            )
                        }
                ) {
                    DuoStatusIndicator(
                        status = status.copy(isBehindLight = isLightWallpaper),
                        settings = settings.copy(scalePercent = scaledPercent)
                    )
                }
            }
        }
    }
}

/**
 * Full Phone Mockup: Galaxy Z Fold 8 Inner Main Screen (Unfolded Square 7.6"+ Tablet Form Factor or Rotated)
 */
@Composable
private fun InnerTabletMockupCanvas(
    status: DeviceStatus,
    settings: OverlaySettings,
    currentBrush: Brush,
    isLightWallpaper: Boolean,
    isRotated: Boolean,
    innerWidthDp: Float = 768f,
    innerHeightDp: Float = 880f,
    statusBarCenterYDp: Float = 18f,
    onInnerPositionChanged: (Int, Int) -> Unit,
    onInnerRotatedPositionChanged: (Int, Int) -> Unit
) {
    val chassisWidth = if (isRotated) 280.dp else 280.dp
    val chassisHeight = if (isRotated) 240.dp else 270.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer chassis (Wide unfolded book frame with dual bezels)
        Box(
            modifier = Modifier
                .width(chassisWidth)
                .height(chassisHeight)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF14161C))
                .border(2.5.dp, Color(0xFF3F4452), RoundedCornerShape(24.dp))
                .padding(4.dp)
        ) {
            // Main display screen
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(currentBrush)
            ) {
                val density = LocalDensity.current
                val stageWidthPx = constraints.maxWidth.toFloat()
                val stageHeightPx = constraints.maxHeight.toFloat()
                val udcCenterX = stageWidthPx * 0.75f

                val (targetXDp, targetYDp) = if (isRotated) {
                    settings.innerRotatedOffsetX to settings.innerRotatedOffsetY
                } else {
                    settings.innerOffsetX to settings.innerOffsetY
                }

                val scaledPercent = (settings.scalePercent * (if (isRotated) 0.50f else 0.44f)).toInt().coerceIn(25, 100)
                val scaleFactor = scaledPercent / 100f
                val circleWPx = with(density) {
                    (if (settings.positionPreset == PositionPreset.WRAP_CUTOUT && settings.cameraCutoutStyle == CameraCutoutStyle.FLANKING_WINGS) 92.dp else 54.dp).toPx() * scaleFactor
                }
                val circleHPx = with(density) {
                    (if (settings.positionPreset == PositionPreset.WRAP_CUTOUT && settings.cameraCutoutStyle == CameraCutoutStyle.ORBITAL_SATELLITE) 70.dp else 54.dp).toPx() * scaleFactor
                }
                val timeScaleMultiplier = (settings.timeTextScalePercent / 100f).coerceIn(0.7f, 2.0f)
                val timeOffsetDp = (64.dp * timeScaleMultiplier) + 6.dp
                val indicatorWidthPx = with(density) {
                    (if (settings.showTimeOnLeft) (timeOffsetDp * scaleFactor).toPx() + circleWPx else circleWPx)
                }
                val indicatorHeightPx = circleHPx
                val ringRadiusPx = with(density) { (27.dp * scaleFactor).toPx() }
                val ringCenterInBoxXPx = if (settings.showTimeOnLeft) with(density) { (timeOffsetDp * scaleFactor).toPx() } + (circleWPx / 2f) else (circleWPx / 2f)
                val ringCenterInBoxYPx = circleHPx / 2f

                val realW = if (isRotated) innerHeightDp else innerWidthDp
                val realH = if (isRotated) innerWidthDp else innerHeightDp
                val mockupStatusBarY = with(density) { 9.dp.toPx() }
                val udcTargetXPx = if (isRotated) stageWidthPx * 0.85f else udcCenterX

                val initialCenterXPx = ((targetXDp.toFloat() / realW) * stageWidthPx)
                val initialCenterYPx = (mockupStatusBarY + with(density) { ((targetYDp - statusBarCenterYDp) * 0.7f).dp.toPx() })

                var dragCenterXPx by remember(targetXDp, stageWidthPx, isRotated) {
                    mutableFloatStateOf(initialCenterXPx)
                }
                var dragCenterYPx by remember(targetYDp, stageHeightPx, isRotated) {
                    mutableFloatStateOf(initialCenterYPx)
                }

                // Central Hinge Fold Crease (vertical when upright, horizontal when rotated)
                if (!isRotated) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxHeight()
                            .width(4.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.White.copy(alpha = 0.08f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.White.copy(alpha = 0.08f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // Status Bar line
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "09:41", color = if (isLightWallpaper) Color(0xFF1E2129) else Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "5G", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isLightWallpaper) Color(0xFF1E2129) else Color.White)
                        Text(text = "88%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isLightWallpaper) Color(0xFF1E2129) else Color.White)
                    }
                }

                // Under-Display Camera (UDC) on Fold 8 Inner Display
                Box(
                    modifier = Modifier
                        .offset { IntOffset((udcCenterX - with(density) { 4.dp.toPx() }).roundToInt(), with(density) { 5.dp.toPx() }.roundToInt()) }
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .border(0.5.dp, Color(0x8800E5FF), CircleShape)
                )

                // Unfolded Multi-window / Taskbar Dock mockup
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(6) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.4f))
                        )
                    }
                }

                // Draggable indicator
                Box(
                    modifier = Modifier
                        .offset {
                            val left = (dragCenterXPx - ringCenterInBoxXPx).roundToInt()
                            val top = (dragCenterYPx - ringCenterInBoxYPx).roundToInt()
                            IntOffset(left, top)
                        }
                        .pointerInput(isRotated) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragCenterXPx = (dragCenterXPx + dragAmount.x).coerceIn(0f, stageWidthPx)
                                    dragCenterYPx = (dragCenterYPx + dragAmount.y).coerceIn(0f, stageHeightPx)
                                },
                                onDragEnd = {
                                    val realX = ((dragCenterXPx / stageWidthPx) * realW).roundToInt().coerceIn(0, realW.toInt())
                                    val deltaY = (dragCenterYPx - mockupStatusBarY) / with(density) { 0.7f.dp.toPx() }
                                    val realY = (statusBarCenterYDp + deltaY).roundToInt().coerceIn(0, 120)
                                    if (isRotated) {
                                        onInnerRotatedPositionChanged(realX, realY)
                                    } else {
                                        onInnerPositionChanged(realX, realY)
                                    }
                                }
                            )
                        }
                ) {
                    DuoStatusIndicator(
                        status = status.copy(isBehindLight = isLightWallpaper),
                        settings = settings.copy(scalePercent = scaledPercent)
                    )
                }
            }
        }
    }
}

@Composable
private fun CutoutChip(
    label: String,
    target: PhoneCutoutType,
    current: PhoneCutoutType,
    onSelect: (PhoneCutoutType) -> Unit
) {
    FilterChip(
        selected = target == current,
        onClick = { onSelect(target) },
        label = { Text(label, fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun FineTuneStepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    max: Int = 500
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "${value}dp",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = { onValueChange((value - 10).coerceIn(0, max)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("-10", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    FilledTonalIconButton(
                        onClick = { onValueChange((value - 1).coerceIn(0, max)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease 1dp", modifier = Modifier.size(16.dp))
                    }
                }

                Slider(
                    value = value.toFloat().coerceIn(0f, max.toFloat()),
                    onValueChange = { onValueChange(it.roundToInt()) },
                    valueRange = 0f..max.toFloat(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = { onValueChange((value + 1).coerceIn(0, max)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase 1dp", modifier = Modifier.size(16.dp))
                    }
                    FilledTonalIconButton(
                        onClick = { onValueChange((value + 10).coerceIn(0, max)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("+10", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
