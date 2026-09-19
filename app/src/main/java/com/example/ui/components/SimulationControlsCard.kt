package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimulationControlsCard(
    status: DeviceStatus,
    isSimulationMode: Boolean,
    onToggleSimulation: (Boolean) -> Unit,
    onSimulateBattery: (level: Int, isCharging: Boolean, isPowerSave: Boolean) -> Unit,
    onSimulateNetwork: (is5G: Boolean, dots: Int, isWifi: Boolean) -> Unit,
    onSimulateDnd: (Boolean) -> Unit = {},
    onSimulateRinger: (isSilent: Boolean, isVibrate: Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "simulationChevron")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("simulation_controls_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = "Test Simulator",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "State Testing & Sandbox",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isSimulationMode) "Simulation Active • Sandbox Override" else "Simulate indicator states (Battery, Dots, Center)",
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
                                text = if (isSimulationMode) "Simulating" else "Live",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isSimulationMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = if (isSimulationMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse Sandbox" else "Expand Sandbox",
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
                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSimulationMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Simulation Override", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    if (isSimulationMode) "Override hardware status with test values" else "Use live device hardware sensors",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isSimulationMode,
                                onCheckedChange = onToggleSimulation,
                                modifier = Modifier.testTag("simulation_toggle")
                            )
                        }
                    }

                    if (isSimulationMode) {
                        Spacer(modifier = Modifier.height(14.dp))

                // 1. Center Indicator Simulators (Wi-Fi, DND, Silent, 5G, 4G)
                Text(
                    text = "Center Glyph Simulator",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Switch center icon to Wi-Fi, DND, Silent, 5G, or 4G",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = status.isWifiConnected && !status.isDndActive && !status.isRingerSilent,
                        onClick = {
                            onSimulateDnd(false)
                            onSimulateRinger(false, false)
                            onSimulateNetwork(true, status.cellularSignalLevel, true)
                        },
                        label = { Text("Wi-Fi Wave", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = status.isDndActive,
                        onClick = {
                            onSimulateDnd(true)
                            onSimulateRinger(false, false)
                        },
                        label = { Text("Do Not Disturb", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.errorContainer)
                    )
                    FilterChip(
                        selected = status.isRingerSilent,
                        onClick = {
                            onSimulateDnd(false)
                            onSimulateRinger(true, false)
                        },
                        label = { Text("Silent Mode", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    )
                    FilterChip(
                        selected = !status.isWifiConnected && status.is5G && !status.isDndActive && !status.isRingerSilent,
                        onClick = {
                            onSimulateDnd(false)
                            onSimulateRinger(false, false)
                            onSimulateNetwork(true, status.cellularSignalLevel, false)
                        },
                        label = { Text("5G Cellular", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = !status.isWifiConnected && !status.is5G && !status.isDndActive && !status.isRingerSilent,
                        onClick = {
                            onSimulateDnd(false)
                            onSimulateRinger(false, false)
                            onSimulateNetwork(false, status.cellularSignalLevel, false)
                        },
                        label = { Text("4G Cellular", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Cellular Signal Dots (0 to 4 dots)
                Text(
                    text = "Cell Signal Dots (Bottom Arc)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (dots in 0..4) {
                        FilterChip(
                            selected = status.cellularSignalLevel == dots,
                            onClick = { onSimulateNetwork(status.is5G, dots, status.isWifiConnected) },
                            label = { Text(if (dots == 0) "0 Dots" else "$dots ${if (dots == 1) "Dot" else "Dots"}", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Battery Ring Presets & Slider
                Text(
                    text = "Battery Half-Circle Arc",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = status.isCharging,
                        onClick = { onSimulateBattery(88, true, false) },
                        label = { Text("⚡ Charging (Cyan)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.25f))
                    )
                    FilterChip(
                        selected = status.batteryPercent <= 20 && !status.isCharging,
                        onClick = { onSimulateBattery(15, false, false) },
                        label = { Text("⚠️ Low (Red)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFF5252).copy(alpha = 0.25f))
                    )
                    FilterChip(
                        selected = status.isPowerSaveMode,
                        onClick = { onSimulateBattery(45, false, true) },
                        label = { Text("🌙 Power Save (Yellow)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFD600).copy(alpha = 0.25f))
                    )
                    FilterChip(
                        selected = status.batteryPercent == 30 && !status.isCharging,
                        onClick = { onSimulateBattery(30, false, false) },
                        label = { Text("30% (Screenshot State)", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = status.batteryPercent == 100 && !status.isCharging,
                        onClick = { onSimulateBattery(100, false, false) },
                        label = { Text("100% Full", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF00E676).copy(alpha = 0.25f))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Battery Level: ${status.batteryPercent}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Slider(
                    value = status.batteryPercent.toFloat(),
                    onValueChange = { onSimulateBattery(it.toInt(), status.isCharging, status.isPowerSaveMode) },
                    valueRange = 1f..100f,
                    modifier = Modifier.testTag("battery_level_slider")
                )
            }
        }
    }
}
    }
}
