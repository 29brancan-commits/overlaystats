package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceStatus

@Composable
fun StatusDetailsDialog(
    status: DeviceStatus,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("status_details_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Device Status Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section: Dual-SIM & Cellular
                DetailCard(
                    title = "Cellular & Dual-SIM Details",
                    summary = "${status.carrierName} • ${status.networkTypeLabel}",
                    icon = Icons.Default.NetworkCell,
                    iconColor = Color(0xFF00E5FF)
                ) {
                    DetailRow("Default Data SIM", status.carrierName)
                    DetailRow("Network Tech", status.networkTypeLabel)
                    DetailRow("Signal Strength", "${status.cellularSignalLevel} of 4 dots")
                    DetailRow("5G NSA Display", if (status.is5G) "Active (Supported 5G NSA)" else "LTE / Standard")
                    DetailRow("Active SIMs", "${status.simCount}")

                    status.sim1?.let {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                        DetailRow("SIM 1 Name", it.displayName)
                        DetailRow("SIM 1 Roaming", if (it.isDataRoaming) "Active" else "Disabled")
                    }

                    status.sim2?.let {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                        DetailRow("SIM 2 Name", it.displayName)
                        DetailRow("SIM 2 Status", "Secondary SIM")
                    }
                }

                // Section: Wi-Fi Telemetry
                DetailCard(
                    title = "Wi-Fi & Speed Telemetry",
                    summary = if (status.isWifiConnected) status.wifiSsid else "Disconnected",
                    icon = Icons.Default.Wifi,
                    iconColor = Color(0xFF00E676)
                ) {
                    DetailRow("Wi-Fi Status", if (status.isWifiConnected) "Connected" else "Disconnected")
                    if (status.isWifiConnected) {
                        DetailRow("SSID", status.wifiSsid)
                        DetailRow("Frequency", "${status.wifiFrequencyMhz} MHz (${status.wifiStandard})")
                        DetailRow("Signal (RSSI)", "${status.wifiRssi} dBm (${status.wifiSignalLevel}/4)")
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                    DetailRow("Live Download Speed", formatSpeed(status.downloadSpeedBps))
                    DetailRow("Live Upload Speed", formatSpeed(status.uploadSpeedBps))
                }

                // Section: Battery Diagnostics
                DetailCard(
                    title = "Battery Diagnostics",
                    summary = "${status.batteryPercent}% • ${if (status.isCharging) "Charging" else "Discharging"}",
                    icon = Icons.Default.BatteryStd,
                    iconColor = Color(0xFFFFD600)
                ) {
                    DetailRow("Battery Level", "${status.batteryPercent}%")
                    DetailRow("Power State", if (status.isCharging) "Charging (${status.chargeType})" else "Discharging")
                    DetailRow("Battery Health", status.batteryHealth)
                    DetailRow("Temperature", "${String.format("%.1f", status.batteryTemperature)} °C")
                    DetailRow("Terminal Voltage", "${status.batteryVoltage} mV")
                    DetailRow("Power Saving Mode", if (status.isPowerSaveMode) "Active" else "Standard")
                    DetailRow("Do Not Disturb", if (status.isDndActive) "Active" else "Inactive")
                    DetailRow("Ringer Mode", if (status.isRingerSilent) "Silent" else if (status.isRingerVibrate) "Vibrate" else "Normal")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun DetailCard(
    title: String,
    summary: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val chevronRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "detailChevron")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (summary.isNotEmpty()) {
                            Text(
                                text = summary,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.rotate(chevronRotation),
                        tint = iconColor
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatSpeed(bps: Long): String {
    return when {
        bps >= 1024 * 1024 -> String.format("%.2f MB/s", bps / (1024f * 1024f))
        bps >= 1024 -> String.format("%d KB/s", bps / 1024)
        else -> "$bps B/s"
    }
}
