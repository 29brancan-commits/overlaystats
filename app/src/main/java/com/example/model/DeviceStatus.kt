package com.example.model

enum class IndicatorColorMode {
    AUTO,
    JUST_WHITE,
    DARK,
    LIGHT,
    CYBER_MINT,
    ELECTRIC_CYAN
}

enum class PositionPreset {
    TOP_RIGHT,
    TOP_LEFT,
    TOP_CENTER_NOTCH,
    WRAP_CUTOUT,
    CUSTOM
}

enum class CameraCutoutStyle {
    ORBITAL_SATELLITE, // Micro-badge at 6 o'clock just beneath camera lens
    SMART_AUTO_HIDE,   // Centered over camera, dynamically fades to transparent when camera opens
    PERIMETER_HALO,    // Zero obstruction, status aura pulses around outer perimeter ring
    FLANKING_WINGS,    // Mini dynamic capsule with left wing for Wi-Fi/DND and right wing for cellular
    HOLLOW_RING        // Clean minimalist halo ring around camera
}

enum class PhoneCutoutType {
    AUTO_DETECT,
    CENTER_PUNCH_HOLE,
    Z_FOLD_COVER_PUNCH_HOLE,
    Z_FOLD_INNER_UDC,
    LEFT_PUNCH_HOLE,
    RIGHT_PUNCH_HOLE,
    WATERDROP_NOTCH,
    PILL_CUTOUT,
    NO_CUTOUT
}

enum class FoldScreenMode {
    AUTO_DETECT,
    COVER_SCREEN,
    INNER_SCREEN
}

enum class CenterDisplayPreference {
    AUTO, // Priority: DND > Silent > Wi-Fi > 5G/4G
    WIFI,
    CELLULAR, // 5G / 4G
    DND,
    SILENT
}

data class SimInfo(
    val slotIndex: Int,
    val carrierName: String,
    val displayName: String,
    val networkType: String,
    val signalLevel: Int, // 0 to 4
    val isDataRoaming: Boolean,
    val isDefaultData: Boolean
)

data class DeviceStatus(
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val chargeType: String = "Unplugged",
    val batteryTemperature: Float = 28.0f,
    val batteryVoltage: Int = 4120, // mV
    val batteryHealth: String = "Good",
    val isPowerSaveMode: Boolean = false,
    
    val isWifiConnected: Boolean = true,
    val wifiSsid: String = "Office_5G",
    val wifiSignalLevel: Int = 4, // 0 to 4
    val wifiRssi: Int = -58,
    val wifiFrequencyMhz: Int = 5180,
    val wifiStandard: String = "Wi-Fi 6",

    val isCellularConnected: Boolean = true,
    val cellularSignalLevel: Int = 4, // 0 to 4 dots
    val networkTypeLabel: String = "5G NSA",
    val carrierName: String = "Default Data SIM",
    val is5G: Boolean = true,
    
    val downloadSpeedBps: Long = 1845000L, // bytes/sec
    val uploadSpeedBps: Long = 345000L,

    val isDndActive: Boolean = false,
    val isRingerSilent: Boolean = false,
    val isRingerVibrate: Boolean = false,
    val simCount: Int = 1,
    val sim1: SimInfo? = SimInfo(
        slotIndex = 0,
        carrierName = "Carrier Primary",
        displayName = "SIM 1 (Data)",
        networkType = "5G NSA",
        signalLevel = 4,
        isDataRoaming = false,
        isDefaultData = true
    ),
    val sim2: SimInfo? = null,
    val defaultDataSlot: Int = 0,
    val currentTimeText: String = "10:45",
    val isBehindLight: Boolean = false
)

data class OverlaySettings(
    val isOverlayEnabled: Boolean = false,
    val colorMode: IndicatorColorMode = IndicatorColorMode.AUTO,
    val scalePercent: Int = 100, // 25 to 150
    val opacityPercent: Int = 95, // 40 to 100
    val positionPreset: PositionPreset = PositionPreset.TOP_RIGHT,
    val offsetX: Int = 206, // dp from anchor (Folded Portrait - center of 412dp screen)
    val offsetY: Int = 16, // dp from anchor (Folded Portrait - status bar center)
    val foldedRotatedOffsetX: Int = 860, // dp from anchor (Folded Rotated / Landscape - top right)
    val foldedRotatedOffsetY: Int = 16, // dp from anchor (Folded Rotated / Landscape - status bar center)
    val avoidDnd: Boolean = false,
    val startOnBoot: Boolean = false,
    val showSpeedInOverlay: Boolean = false,
    val centerPreference: CenterDisplayPreference = CenterDisplayPreference.AUTO,
    val cutoutType: PhoneCutoutType = PhoneCutoutType.Z_FOLD_COVER_PUNCH_HOLE,
    val cutoutSizeDp: Int = 12, // dp diameter of camera cutout
    val cutoutOffsetXDp: Int = 0, // fine horizontal shift if needed
    val foldScreenMode: FoldScreenMode = FoldScreenMode.AUTO_DETECT,
    val innerOffsetX: Int = 540, // dp from anchor (Inner Unfolded Portrait - UDC camera 75% width of 720dp)
    val innerOffsetY: Int = 16, // dp from anchor (Inner Unfolded Portrait - status bar center)
    val innerRotatedOffsetX: Int = 780, // dp from anchor (Inner Unfolded Rotated / Landscape - UDC camera)
    val innerRotatedOffsetY: Int = 16, // dp from anchor (Inner Unfolded Rotated / Landscape - status bar center)
    val cameraCutoutStyle: CameraCutoutStyle = CameraCutoutStyle.ORBITAL_SATELLITE,
    val showBatteryNumber: Boolean = true,
    val showTimeOnLeft: Boolean = true,
    val timeTextScalePercent: Int = 100, // 70 to 200 percent of base time size
    val showCellSignalDots: Boolean = true,
    val timeIndependentMovement: Boolean = false,
    val timeOffsetX: Int = 0, // dp offset relative to indicator (-200..200)
    val timeOffsetY: Int = 0, // dp offset relative to indicator (-100..100)
    val timeFoldedRotatedOffsetX: Int = 0,
    val timeFoldedRotatedOffsetY: Int = 0,
    val timeInnerOffsetX: Int = 0,
    val timeInnerOffsetY: Int = 0,
    val timeInnerRotatedOffsetX: Int = 0,
    val timeInnerRotatedOffsetY: Int = 0
) {
    fun getCurrentOffset(isUnfolded: Boolean, isRotated: Boolean): Pair<Int, Int> {
        return when {
            isUnfolded && isRotated -> innerRotatedOffsetX to innerRotatedOffsetY
            isUnfolded && !isRotated -> innerOffsetX to innerOffsetY
            !isUnfolded && isRotated -> foldedRotatedOffsetX to foldedRotatedOffsetY
            else -> offsetX to offsetY
        }
    }

    fun getCurrentTimeRelativeOffset(isUnfolded: Boolean, isRotated: Boolean): Pair<Int, Int> {
        return when {
            isUnfolded && isRotated -> timeInnerRotatedOffsetX to timeInnerRotatedOffsetY
            isUnfolded && !isRotated -> timeInnerOffsetX to timeInnerOffsetY
            !isUnfolded && isRotated -> timeFoldedRotatedOffsetX to timeFoldedRotatedOffsetY
            else -> timeOffsetX to timeOffsetY
        }
    }
}
