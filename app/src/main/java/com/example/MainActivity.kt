package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.BatteryTelemetryCard
import com.example.ui.components.DuoStatusIndicator
import com.example.ui.components.IndicatorSettingsCard
import com.example.ui.components.LiveInteractiveStage
import com.example.ui.components.NetworkTelemetryCard
import com.example.ui.components.SimulationControlsCard
import com.example.ui.components.StatusDetailsDialog
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                MainScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onRefreshPermission = { viewModel.refreshPermissions() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    onRefreshPermission: () -> Unit
) {
    val context = LocalContext.current

    // Runtime permission launcher for phone state (for dual-sim telemetry)
    val phoneStateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onRefreshPermission()
    }

    // Runtime permission launcher for notifications (Android 13+)
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onRefreshPermission()
    }

    val hasPhoneStatePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_PHONE_STATE
    ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(Unit) {
        if (!hasPhoneStatePermission) {
            phoneStateLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotif = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasNotif) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (uiState.settings.isOverlayEnabled) Color(0xFF00E676) else Color(0xFF00E5FF))
                        )
                        Text(
                            text = "O.status",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "v1.0.0",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setShowDetailsDialog(true) },
                        modifier = Modifier.testTag("details_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Device Status Details",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("main_scroll_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Status Banner with interactive click
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("status_hero_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Compact Duo Indicator",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (uiState.settings.isOverlayEnabled) "Overlay active on screen" else "Live unified status preview",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DuoStatusIndicator(
                            status = uiState.status,
                            settings = uiState.settings
                        )
                    }
                }
            }

            // Permissions alert card if overlay permission needed
            if (!uiState.hasOverlayPermission) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Permission warning",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Overlay Permission Required",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "To draw the floating indicator over other applications, grant 'Display over other apps'.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
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
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Grant", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Customization & Overlay Rules Card
            item {
                IndicatorSettingsCard(
                    settings = uiState.settings,
                    hasOverlayPermission = uiState.hasOverlayPermission,
                    onToggleOverlay = { enabled -> viewModel.toggleOverlay(enabled, context) },
                    onColorModeChanged = { mode -> viewModel.setColorMode(mode) },
                    onScaleChanged = { scale -> viewModel.setScale(scale) },
                    onOpacityChanged = { opacity -> viewModel.setOpacity(opacity) },
                    onAvoidDndChanged = { avoid -> viewModel.setAvoidDnd(avoid) },
                    onStartOnBootChanged = { boot -> viewModel.setStartOnBoot(boot) },
                    onShowSpeedChanged = { speed -> viewModel.setShowSpeed(speed) },
                    onCenterPreferenceChanged = { pref -> viewModel.setCenterPreference(pref) },
                    onCameraCutoutStyleChanged = { style -> viewModel.setCameraCutoutStyle(style) },
                    onShowBatteryNumberChanged = { show -> viewModel.setShowBatteryNumber(show) },
                    onShowTimeOnLeftChanged = { show -> viewModel.setShowTimeOnLeft(show) },
                    onTimeTextScalePercentChanged = { scale -> viewModel.setTimeTextScalePercent(scale) },
                    onShowCellSignalDotsChanged = { show -> viewModel.setShowCellSignalDots(show) }
                )
            }

            // Live Interactive Placement Stage
            item {
                LiveInteractiveStage(
                    status = uiState.status,
                    settings = uiState.settings,
                    onPositionChanged = { preset, x, y ->
                        viewModel.setPosition(preset, x, y)
                    },
                    onCutoutChanged = { type, size, offset ->
                        viewModel.setCutoutSettings(type, size, offset)
                    },
                    onInnerPositionChanged = { x, y ->
                        viewModel.setInnerPosition(x, y)
                    },
                    onFoldedRotatedPositionChanged = { x, y ->
                        viewModel.setFoldedRotatedPosition(x, y)
                    },
                    onInnerRotatedPositionChanged = { x, y ->
                        viewModel.setInnerRotatedPosition(x, y)
                    },
                    onFoldModeChanged = { mode ->
                        viewModel.setFoldScreenMode(mode)
                    },
                    onCameraCutoutStyleChanged = { style ->
                        viewModel.setCameraCutoutStyle(style)
                    }
                )
            }

            // Network & Dual-SIM Telemetry Card
            item {
                NetworkTelemetryCard(
                    status = uiState.status
                )
            }

            // Battery Telemetry Ring Card
            item {
                BatteryTelemetryCard(
                    status = uiState.status
                )
            }

            // Simulation & Testing Sandbox
            item {
                SimulationControlsCard(
                    status = uiState.status,
                    isSimulationMode = uiState.isSimulationMode,
                    onToggleSimulation = { enable -> viewModel.toggleSimulationMode(enable) },
                    onSimulateBattery = { lvl, chg, pwr -> viewModel.simulateBatteryState(lvl, chg, pwr) },
                    onSimulateNetwork = { is5g, dots, wifi -> viewModel.simulateNetwork(is5g, dots, wifi) },
                    onSimulateDnd = { isDnd -> viewModel.simulateDnd(isDnd) },
                    onSimulateRinger = { isSilent, isVibrate -> viewModel.simulateRingerMode(isSilent, isVibrate) }
                )
            }

            // Footer credits
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "O.status • Version 1.0.0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Jason Leung / CATCH7NG.L · All Rights Reserved",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lightweight minimal device status indicator overlay",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    if (uiState.showDetailsDialog) {
        StatusDetailsDialog(
            status = uiState.status,
            onDismiss = { viewModel.setShowDetailsDialog(false) }
        )
    }
}
