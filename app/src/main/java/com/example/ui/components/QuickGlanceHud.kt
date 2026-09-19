package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceStatus
import kotlinx.coroutines.delay

/**
 * Compact, floating Quick-Glance HUD showing live hardware metrics:
 * - Battery percentage, charging rate/mode
 * - Wi-Fi connection strength
 * - Cellular 5G/4G carrier and signal
 * - Sound & DND profile
 */
@Composable
fun QuickGlanceHud(
    status: DeviceStatus,
    visible: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    autoDismissSeconds: Int = 4
) {
    LaunchedEffect(visible) {
        if (visible && autoDismissSeconds > 0) {
            delay(autoDismissSeconds * 1000L)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.92f),
        exit = fadeOut() + scaleOut(targetScale = 0.92f),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xF2121722),
            shadowElevation = 12.dp,
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(22.dp))
                .testTag("quick_glance_hud")
                .clickable { onDismiss() }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header: Battery Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (status.isCharging) Color(0x3300E5FF) else Color(0x22FFFFFF),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (status.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                contentDescription = "Battery Status",
                                tint = if (status.isCharging) Color(0xFF00E5FF) else if (status.batteryPercent <= 20) Color(0xFFFF5252) else Color(0xFF00E676),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${status.batteryPercent}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = when {
                                    status.isCharging -> "Fast Charging ⚡"
                                    status.isPowerSaveMode -> "Power Saver Active"
                                    status.batteryPercent <= 20 -> "Battery Low"
                                    else -> "Normal Discharging"
                                },
                                fontSize = 11.sp,
                                color = if (status.isCharging) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Battery Mini Bar
                LinearProgressIndicator(
                    progress = { (status.batteryPercent.coerceIn(0, 100) / 100f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (status.isCharging) Color(0xFF00E5FF) else if (status.batteryPercent <= 20) Color(0xFFFF5252) else Color(0xFF00E676),
                    trackColor = Color.White.copy(alpha = 0.12f)
                )

                // Divider line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )

                // Status Matrix (Wi-Fi & Cellular)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Wi-Fi
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Wi-Fi",
                            tint = if (status.isWifiConnected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (status.isWifiConnected) "Wi-Fi (${status.wifiSignalLevel}/4)" else "Offline",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Cellular
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NetworkCell,
                            contentDescription = "Cellular",
                            tint = if (status.cellularSignalLevel > 0) Color(0xFF00E676) else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${if (status.is5G) "5G" else "4G"} (${status.cellularSignalLevel}/4)",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Sound / DND profile
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            status.isDndActive -> Icons.Default.DoNotDisturb
                            status.isRingerSilent -> Icons.Default.NotificationsOff
                            else -> Icons.Default.Notifications
                        },
                        contentDescription = "Ringer status",
                        tint = when {
                            status.isDndActive || status.isRingerSilent -> Color(0xFFFF9100)
                            else -> Color(0xFF81D4FA)
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            status.isDndActive -> "Do Not Disturb"
                            status.isRingerSilent -> "Silent Profile"
                            status.isRingerVibrate -> "Vibrate"
                            else -> "Normal Ringing"
                        },
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                // Optional action button
                if (onOpenSettings != null) {
                    FilledTonalButton(
                        onClick = {
                            onDismiss()
                            onOpenSettings()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0x3300E5FF),
                            contentColor = Color(0xFF00E5FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open Settings",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Duo Settings", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
