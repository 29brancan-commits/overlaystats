package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraCutoutStyle
import com.example.model.IndicatorColorMode
import com.example.model.OverlaySettings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IndicatorSettingsCard(
    settings: OverlaySettings,
    hasOverlayPermission: Boolean,
    onToggleOverlay: (Boolean) -> Unit,
    onColorModeChanged: (IndicatorColorMode) -> Unit,
    onScaleChanged: (Int) -> Unit,
    onOpacityChanged: (Int) -> Unit,
    onAvoidDndChanged: (Boolean) -> Unit,
    onStartOnBootChanged: (Boolean) -> Unit,
    onShowSpeedChanged: (Boolean) -> Unit,
    onCenterPreferenceChanged: (com.example.model.CenterDisplayPreference) -> Unit = {},
    onCameraCutoutStyleChanged: (CameraCutoutStyle) -> Unit = {},
    onShowBatteryNumberChanged: (Boolean) -> Unit = {},
    onShowTimeOnLeftChanged: (Boolean) -> Unit = {},
    onTimeTextScalePercentChanged: (Int) -> Unit = {},
    onShowCellSignalDotsChanged: (Boolean) -> Unit = {},
    onTimeIndependentMovementChanged: (Boolean) -> Unit = {},
    onResetTimePosition: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "settingsChevron")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("indicator_settings_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Indicator Customization",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (settings.isOverlayEnabled) "Overlay: Active • ${settings.colorMode.name}" else "Overlay: Off • Tap to configure",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SuggestionChip(
                        onClick = { isExpanded = !isExpanded },
                        label = {
                            Text(
                                text = if (settings.isOverlayEnabled) "Active" else "Off",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (settings.isOverlayEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = if (settings.isOverlayEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse Customization" else "Expand Customization",
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
                    Spacer(modifier = Modifier.height(16.dp))

            // Main Overlay Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (settings.isOverlayEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Overlay",
                                tint = if (settings.isOverlayEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column {
                                Text(
                                    text = "System Overlay Indicator",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (settings.isOverlayEnabled) "Active on screen over other apps" else "Disabled (Visible in live preview)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = settings.isOverlayEnabled,
                            onCheckedChange = { onToggleOverlay(it) },
                            modifier = Modifier.testTag("overlay_toggle_switch")
                        )
                    }

                    if (!hasOverlayPermission) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Requires 'Display over other apps' permission",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.testTag("grant_overlay_permission_button")
                            ) {
                                Text("Grant Permission", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Mode Selection
            Text(
                text = "Color Theme & Override",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorChip("Auto (System)", IndicatorColorMode.AUTO, settings.colorMode) { onColorModeChanged(it) }
                ColorChip("Just White", IndicatorColorMode.JUST_WHITE, settings.colorMode) { onColorModeChanged(it) }
                ColorChip("Dark (Obsidian)", IndicatorColorMode.DARK, settings.colorMode) { onColorModeChanged(it) }
                ColorChip("Light (Clean)", IndicatorColorMode.LIGHT, settings.colorMode) { onColorModeChanged(it) }
                ColorChip("Cyber Mint", IndicatorColorMode.CYBER_MINT, settings.colorMode) { onColorModeChanged(it) }
                ColorChip("Electric Cyan", IndicatorColorMode.ELECTRIC_CYAN, settings.colorMode) { onColorModeChanged(it) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center Display Target
            Text(
                text = "Center Icon Display",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Choose what is shown in the center of the ring",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CenterPrefChip("Auto (Priority)", com.example.model.CenterDisplayPreference.AUTO, settings.centerPreference) { onCenterPreferenceChanged(it) }
                CenterPrefChip("Wi-Fi", com.example.model.CenterDisplayPreference.WIFI, settings.centerPreference) { onCenterPreferenceChanged(it) }
                CenterPrefChip("Cellular (5G/4G)", com.example.model.CenterDisplayPreference.CELLULAR, settings.centerPreference) { onCenterPreferenceChanged(it) }
                CenterPrefChip("Do Not Disturb", com.example.model.CenterDisplayPreference.DND, settings.centerPreference) { onCenterPreferenceChanged(it) }
                CenterPrefChip("Silent", com.example.model.CenterDisplayPreference.SILENT, settings.centerPreference) { onCenterPreferenceChanged(it) }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(14.dp))

            // Camera Cutout Overlap Style
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Camera Cutout Overlap Style",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "How center icons behave when wrapping over camera punch-hole/UDC",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CutoutStyleChip("🛰️ Orbital Satellite", CameraCutoutStyle.ORBITAL_SATELLITE, settings.cameraCutoutStyle) { onCameraCutoutStyleChanged(it) }
                CutoutStyleChip("⚡ Smart Auto-Hide", CameraCutoutStyle.SMART_AUTO_HIDE, settings.cameraCutoutStyle) { onCameraCutoutStyleChanged(it) }
                CutoutStyleChip("💫 Perimeter Halo", CameraCutoutStyle.PERIMETER_HALO, settings.cameraCutoutStyle) { onCameraCutoutStyleChanged(it) }
                CutoutStyleChip("🪽 Flanking Wings", CameraCutoutStyle.FLANKING_WINGS, settings.cameraCutoutStyle) { onCameraCutoutStyleChanged(it) }
                CutoutStyleChip("⭕ Hollow Ring", CameraCutoutStyle.HOLLOW_RING, settings.cameraCutoutStyle) { onCameraCutoutStyleChanged(it) }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(14.dp))

            // Size & Opacity Adjustments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Indicator Scale",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${settings.scalePercent}%",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = settings.scalePercent.toFloat(),
                onValueChange = { onScaleChanged(it.toInt()) },
                valueRange = 25f..150f,
                steps = 24,
                modifier = Modifier.testTag("scale_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overlay Opacity",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${settings.opacityPercent}%",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = settings.opacityPercent.toFloat(),
                onValueChange = { onOpacityChanged(it.toInt()) },
                valueRange = 40f..100f,
                steps = 11,
                modifier = Modifier.testTag("opacity_slider")
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(14.dp))

            // Feature switches (DND Avoidance, Speed, Start on Boot)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SwitchRow(
                    icon = Icons.Default.Schedule,
                    title = "Time on Left",
                    subtitle = "Displays live clock time to the left of the status indicator",
                    checked = settings.showTimeOnLeft,
                    onCheckedChange = onShowTimeOnLeftChanged,
                    testTag = "time_on_left_switch"
                )

                if (settings.showTimeOnLeft) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, end = 4.dp, bottom = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Time Font Size",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val actualSp = (16 * (settings.timeTextScalePercent / 100f)).toInt()
                            Text(
                                text = "${actualSp}sp (${settings.timeTextScalePercent}%)",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Slider(
                            value = settings.timeTextScalePercent.toFloat(),
                            onValueChange = { onTimeTextScalePercentChanged(it.toInt()) },
                            valueRange = 70f..200f,
                            steps = 12,
                            modifier = Modifier.testTag("time_size_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SuggestionChip(
                                onClick = { onTimeTextScalePercentChanged((settings.timeTextScalePercent - 10).coerceAtLeast(70)) },
                                label = { Text("-10%", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f).testTag("time_size_step_down")
                            )
                            SuggestionChip(
                                onClick = { onTimeTextScalePercentChanged(100) },
                                label = { Text("Default", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f).testTag("time_size_step_reset")
                            )
                            SuggestionChip(
                                onClick = { onTimeTextScalePercentChanged((settings.timeTextScalePercent + 10).coerceAtMost(200)) },
                                label = { Text("+10%", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f).testTag("time_size_step_up")
                            )
                            SuggestionChip(
                                onClick = { onTimeTextScalePercentChanged(150) },
                                label = { Text("Large", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f).testTag("time_size_step_large")
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        SwitchRow(
                            icon = Icons.Default.Tune,
                            title = "Move Time Independently",
                            subtitle = "Drag and position the time pill freely anywhere on screen, separate from indicator",
                            checked = settings.timeIndependentMovement,
                            onCheckedChange = onTimeIndependentMovementChanged,
                            testTag = "time_independent_movement_switch"
                        )

                        if (settings.timeIndependentMovement) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 32.dp, top = 2.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Offset: X=${settings.timeOffsetX}dp, Y=${settings.timeOffsetY}dp",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                SuggestionChip(
                                    onClick = onResetTimePosition,
                                    label = { Text("Reset to Indicator", fontSize = 10.sp) },
                                    modifier = Modifier.testTag("reset_time_offset_btn")
                                )
                            }
                        }
                    }
                }

                SwitchRow(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "Battery Number at Top",
                    subtitle = "Displays percentage value at the top crest of the half circle",
                    checked = settings.showBatteryNumber,
                    onCheckedChange = onShowBatteryNumberChanged,
                    testTag = "battery_number_switch"
                )

                SwitchRow(
                    icon = Icons.Default.NetworkCell,
                    title = "Cell Signal Dots (Bottom Arc)",
                    subtitle = "4 dots along bottom arc indicate live cellular reception strength",
                    checked = settings.showCellSignalDots,
                    onCheckedChange = onShowCellSignalDotsChanged,
                    testTag = "cell_signal_dots_switch"
                )

                SwitchRow(
                    icon = Icons.Default.DoNotDisturbOn,
                    title = "Visible during Do Not Disturb",
                    subtitle = "Keep status indicator and DND badge visible on screen when Do Not Disturb is on",
                    checked = !settings.avoidDnd,
                    onCheckedChange = { isVisible -> onAvoidDndChanged(!isVisible) },
                    testTag = "dnd_switch"
                )

                SwitchRow(
                    icon = Icons.Default.Speed,
                    title = "Live Speed in Overlay",
                    subtitle = "Embeds real-time download KB/s inside the Duo indicator",
                    checked = settings.showSpeedInOverlay,
                    onCheckedChange = onShowSpeedChanged,
                    testTag = "speed_switch"
                )

                SwitchRow(
                    icon = Icons.Default.PowerSettingsNew,
                    title = "Start on Boot",
                    subtitle = "Restores status indicator automatically after device restarts",
                    checked = settings.startOnBoot,
                    onCheckedChange = onStartOnBootChanged,
                    testTag = "boot_switch"
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Independent Screen Offsets Readout
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Calibrated Screen Offsets",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Folded Upright", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("X: ${settings.offsetX}dp, Y: ${settings.offsetY}dp", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Folded Rotated", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("X: ${settings.foldedRotatedOffsetX}dp, Y: ${settings.foldedRotatedOffsetY}dp", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Inner Upright", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Text("X: ${settings.innerOffsetX}dp, Y: ${settings.innerOffsetY}dp", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Inner Rotated", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Text("X: ${settings.innerRotatedOffsetX}dp, Y: ${settings.innerRotatedOffsetY}dp", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}
}
}

@Composable
private fun ColorChip(
    label: String,
    mode: IndicatorColorMode,
    currentMode: IndicatorColorMode,
    onSelect: (IndicatorColorMode) -> Unit
) {
    FilterChip(
        selected = mode == currentMode,
        onClick = { onSelect(mode) },
        leadingIcon = {
            val dotColor = when (mode) {
                IndicatorColorMode.AUTO -> MaterialTheme.colorScheme.primary
                IndicatorColorMode.JUST_WHITE -> Color.White
                IndicatorColorMode.DARK -> Color(0xFF1E222B)
                IndicatorColorMode.LIGHT -> Color(0xFFF0F2F5)
                IndicatorColorMode.CYBER_MINT -> Color(0xFF00E676)
                IndicatorColorMode.ELECTRIC_CYAN -> Color(0xFF00E5FF)
            }
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(dotColor)
                    .border(
                        width = 1.dp,
                        color = if (mode == IndicatorColorMode.JUST_WHITE || mode == IndicatorColorMode.LIGHT) Color(0x66888888) else Color.Transparent,
                        shape = CircleShape
                    )
            )
        },
        label = { Text(label, fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.testTag("color_mode_chip_${mode.name.lowercase()}")
    )
}

@Composable
private fun SwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun CenterPrefChip(
    label: String,
    targetPref: com.example.model.CenterDisplayPreference,
    currentPref: com.example.model.CenterDisplayPreference,
    onSelect: (com.example.model.CenterDisplayPreference) -> Unit
) {
    FilterChip(
        selected = targetPref == currentPref,
        onClick = { onSelect(targetPref) },
        label = { Text(label, fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}

@Composable
private fun CutoutStyleChip(
    label: String,
    targetStyle: CameraCutoutStyle,
    currentStyle: CameraCutoutStyle,
    onSelect: (CameraCutoutStyle) -> Unit
) {
    FilterChip(
        selected = targetStyle == currentStyle,
        onClick = { onSelect(targetStyle) },
        label = { Text(label, fontSize = 11.sp, fontWeight = if (targetStyle == currentStyle) FontWeight.Bold else FontWeight.Normal) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

