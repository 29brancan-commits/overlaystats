package com.example.ui

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesManager
import com.example.data.StatusMonitor
import com.example.model.DeviceStatus
import com.example.model.IndicatorColorMode
import com.example.model.OverlaySettings
import com.example.model.PositionPreset
import com.example.service.OverlayIndicatorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val status: DeviceStatus = DeviceStatus(),
    val settings: OverlaySettings = OverlaySettings(),
    val hasOverlayPermission: Boolean = false,
    val isSimulationMode: Boolean = false,
    val showDetailsDialog: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager.getInstance(application)
    private val statusMonitor = StatusMonitor.getInstance(application)

    private val _isSimulationMode = MutableStateFlow(false)
    private val _simulatedStatus = MutableStateFlow<DeviceStatus?>(null)
    private val _showDetailsDialog = MutableStateFlow(false)

    val uiState: StateFlow<MainUiState> = combine(
        statusMonitor.statusFlow,
        prefsManager.settingsFlow,
        _isSimulationMode,
        _simulatedStatus,
        _showDetailsDialog
    ) { realStatus, settings, isSim, simStatus, showDialog ->
        val effectiveStatus = if (isSim && simStatus != null) simStatus else realStatus
        val hasPermission = Settings.canDrawOverlays(getApplication())

        MainUiState(
            status = effectiveStatus,
            settings = settings,
            hasOverlayPermission = hasPermission,
            isSimulationMode = isSim,
            showDetailsDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun refreshPermissions() {
        val hasPermission = Settings.canDrawOverlays(getApplication())
        if (hasPermission && uiState.value.settings.isOverlayEnabled) {
            OverlayIndicatorService.start(getApplication())
        }
    }

    fun toggleOverlay(enabled: Boolean, context: Context) {
        prefsManager.setOverlayEnabled(enabled)
        if (enabled) {
            if (Settings.canDrawOverlays(context)) {
                OverlayIndicatorService.start(context)
            }
        } else {
            OverlayIndicatorService.stop(context)
        }
    }

    fun setColorMode(mode: IndicatorColorMode) {
        prefsManager.setColorMode(mode)
    }

    fun setScale(scale: Int) {
        prefsManager.setScalePercent(scale)
    }

    fun setOpacity(opacity: Int) {
        prefsManager.setOpacityPercent(opacity)
    }

    fun setPosition(preset: PositionPreset, offsetX: Int, offsetY: Int) {
        prefsManager.setPosition(preset, offsetX, offsetY)
    }

    fun setAvoidDnd(avoid: Boolean) {
        prefsManager.setAvoidDnd(avoid)
    }

    fun setStartOnBoot(start: Boolean) {
        prefsManager.setStartOnBoot(start)
    }

    fun setShowSpeed(show: Boolean) {
        prefsManager.setShowSpeed(show)
    }

    fun setShowBatteryNumber(show: Boolean) {
        prefsManager.setShowBatteryNumber(show)
    }

    fun setShowTimeOnLeft(show: Boolean) {
        prefsManager.setShowTimeOnLeft(show)
    }

    fun setTimeTextScalePercent(scalePercent: Int) {
        prefsManager.setTimeTextScalePercent(scalePercent)
    }

    fun setShowCellSignalDots(show: Boolean) {
        prefsManager.setShowCellSignalDots(show)
    }

    fun setCenterPreference(preference: com.example.model.CenterDisplayPreference) {
        prefsManager.setCenterPreference(preference)
    }

    fun setCameraCutoutStyle(style: com.example.model.CameraCutoutStyle) {
        prefsManager.setCameraCutoutStyle(style)
    }

    fun setCutoutSettings(type: com.example.model.PhoneCutoutType, sizeDp: Int = 13, offsetXDp: Int = 0) {
        prefsManager.setCutoutSettings(type, sizeDp, offsetXDp)
    }

    fun setFoldScreenMode(mode: com.example.model.FoldScreenMode) {
        prefsManager.setFoldScreenMode(mode)
    }

    fun setInnerPosition(offsetX: Int, offsetY: Int) {
        prefsManager.setInnerPosition(offsetX, offsetY)
    }

    fun setFoldedRotatedPosition(offsetX: Int, offsetY: Int) {
        prefsManager.setFoldedRotatedPosition(offsetX, offsetY)
    }

    fun setInnerRotatedPosition(offsetX: Int, offsetY: Int) {
        prefsManager.setInnerRotatedPosition(offsetX, offsetY)
    }

    fun setPositionForState(isUnfolded: Boolean, isRotated: Boolean, offsetX: Int, offsetY: Int) {
        prefsManager.setPositionForState(isUnfolded, isRotated, offsetX, offsetY)
    }

    fun setTimeIndependentMovement(enabled: Boolean) {
        prefsManager.setTimeIndependentMovement(enabled)
    }

    fun setTimePositionForState(isUnfolded: Boolean, isRotated: Boolean, offsetX: Int, offsetY: Int) {
        prefsManager.setTimePositionForState(isUnfolded, isRotated, offsetX, offsetY)
    }

    fun resetTimePosition(isUnfolded: Boolean, isRotated: Boolean) {
        prefsManager.resetTimePosition(isUnfolded, isRotated)
    }

    fun setShowDetailsDialog(show: Boolean) {
        _showDetailsDialog.value = show
    }

    fun toggleSimulationMode(enable: Boolean) {
        _isSimulationMode.value = enable
        if (enable && _simulatedStatus.value == null) {
            _simulatedStatus.value = uiState.value.status.copy()
        }
    }

    fun simulateBatteryState(level: Int, isCharging: Boolean, isPowerSave: Boolean) {
        _isSimulationMode.value = true
        _simulatedStatus.update { current ->
            (current ?: uiState.value.status).copy(
                batteryPercent = level,
                isCharging = isCharging,
                isPowerSaveMode = isPowerSave,
                chargeType = if (isCharging) "AC Fast Charge" else "Unplugged"
            )
        }
    }

    fun simulateNetwork(is5G: Boolean, dots: Int, isWifi: Boolean) {
        _isSimulationMode.value = true
        _simulatedStatus.update { current ->
            (current ?: uiState.value.status).copy(
                is5G = is5G,
                networkTypeLabel = if (is5G) "5G" else "4G",
                cellularSignalLevel = dots,
                isWifiConnected = isWifi
            )
        }
    }

    fun simulateDnd(isDnd: Boolean) {
        _isSimulationMode.value = true
        _simulatedStatus.update { current ->
            (current ?: uiState.value.status).copy(
                isDndActive = isDnd
            )
        }
    }

    fun simulateRingerMode(isSilent: Boolean, isVibrate: Boolean) {
        _isSimulationMode.value = true
        _simulatedStatus.update { current ->
            (current ?: uiState.value.status).copy(
                isRingerSilent = isSilent,
                isRingerVibrate = isVibrate
            )
        }
    }
}
