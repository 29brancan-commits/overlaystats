package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.IndicatorColorMode
import com.example.model.OverlaySettings
import com.example.model.PositionPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<OverlaySettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): OverlaySettings {
        val enabled = prefs.getBoolean(KEY_OVERLAY_ENABLED, false)
        val colorModeStr = prefs.getString(KEY_COLOR_MODE, IndicatorColorMode.AUTO.name) ?: IndicatorColorMode.AUTO.name
        val colorMode = try {
            IndicatorColorMode.valueOf(colorModeStr)
        } catch (_: Exception) {
            IndicatorColorMode.AUTO
        }
        val scale = prefs.getInt(KEY_SCALE_PERCENT, 100).coerceIn(25, 150)
        val opacity = prefs.getInt(KEY_OPACITY_PERCENT, 95).coerceIn(40, 100)
        val presetStr = prefs.getString(KEY_POSITION_PRESET, PositionPreset.TOP_RIGHT.name) ?: PositionPreset.TOP_RIGHT.name
        val preset = try {
            PositionPreset.valueOf(presetStr)
        } catch (_: Exception) {
            PositionPreset.TOP_RIGHT
        }
        val offsetX = prefs.getInt(KEY_OFFSET_X, 206)
        val offsetY = prefs.getInt(KEY_OFFSET_Y, 16)
        val avoidDnd = if (prefs.contains(KEY_VISIBLE_IN_DND)) {
            !prefs.getBoolean(KEY_VISIBLE_IN_DND, true)
        } else {
            prefs.getBoolean(KEY_AVOID_DND, false)
        }
        val startOnBoot = prefs.getBoolean(KEY_START_ON_BOOT, false)
        val showSpeed = prefs.getBoolean(KEY_SHOW_SPEED, false)
        val centerPrefStr = prefs.getString(KEY_CENTER_PREF, com.example.model.CenterDisplayPreference.AUTO.name)
            ?: com.example.model.CenterDisplayPreference.AUTO.name
        val centerPref = try {
            com.example.model.CenterDisplayPreference.valueOf(centerPrefStr)
        } catch (_: Exception) {
            com.example.model.CenterDisplayPreference.AUTO
        }
        val cutoutTypeStr = prefs.getString(KEY_CUTOUT_TYPE, com.example.model.PhoneCutoutType.Z_FOLD_COVER_PUNCH_HOLE.name)
            ?: com.example.model.PhoneCutoutType.Z_FOLD_COVER_PUNCH_HOLE.name
        val cutoutType = try {
            com.example.model.PhoneCutoutType.valueOf(cutoutTypeStr)
        } catch (_: Exception) {
            com.example.model.PhoneCutoutType.Z_FOLD_COVER_PUNCH_HOLE
        }
        val cutoutSize = prefs.getInt(KEY_CUTOUT_SIZE, 12)
        val cutoutOffsetX = prefs.getInt(KEY_CUTOUT_OFFSET_X, 0)
        val foldModeStr = prefs.getString(KEY_FOLD_SCREEN_MODE, com.example.model.FoldScreenMode.AUTO_DETECT.name)
            ?: com.example.model.FoldScreenMode.AUTO_DETECT.name
        val foldMode = try {
            com.example.model.FoldScreenMode.valueOf(foldModeStr)
        } catch (_: Exception) {
            com.example.model.FoldScreenMode.AUTO_DETECT
        }
        var innerX = prefs.getInt(KEY_INNER_OFFSET_X, 540)
        var innerY = prefs.getInt(KEY_INNER_OFFSET_Y, 16)
        if ((innerX == 498 && innerY == 6) || (innerX == 20 && innerY == 24)) {
            innerX = 540
            innerY = 16
        }
        var foldedRotatedX = prefs.getInt(KEY_FOLDED_ROTATED_OFFSET_X, 860)
        var foldedRotatedY = prefs.getInt(KEY_FOLDED_ROTATED_OFFSET_Y, 16)
        if ((foldedRotatedX == 820 && foldedRotatedY == 8) || (foldedRotatedX == 24 && foldedRotatedY == 16)) {
            foldedRotatedX = 860
            foldedRotatedY = 16
        }
        var innerRotatedX = prefs.getInt(KEY_INNER_ROTATED_OFFSET_X, 780)
        var innerRotatedY = prefs.getInt(KEY_INNER_ROTATED_OFFSET_Y, 16)
        if ((innerRotatedX == 610 && innerRotatedY == 8) || (innerRotatedX == 24 && innerRotatedY == 16)) {
            innerRotatedX = 780
            innerRotatedY = 16
        }
        val cutoutStyleStr = prefs.getString(KEY_CAMERA_CUTOUT_STYLE, com.example.model.CameraCutoutStyle.ORBITAL_SATELLITE.name)
            ?: com.example.model.CameraCutoutStyle.ORBITAL_SATELLITE.name
        val cutoutStyle = try {
            com.example.model.CameraCutoutStyle.valueOf(cutoutStyleStr)
        } catch (_: Exception) {
            com.example.model.CameraCutoutStyle.ORBITAL_SATELLITE
        }
        val showBatteryNum = prefs.getBoolean(KEY_SHOW_BATTERY_NUMBER, true)
        val showTimeLeft = prefs.getBoolean(KEY_SHOW_TIME_ON_LEFT, true)
        val timeScale = prefs.getInt(KEY_TIME_TEXT_SCALE_PERCENT, 100).coerceIn(70, 200)
        val showCellDots = prefs.getBoolean(KEY_SHOW_CELL_SIGNAL_DOTS, true)
        val timeIndependent = prefs.getBoolean(KEY_TIME_INDEPENDENT_MOVEMENT, false)
        val timeOffsetX = prefs.getInt(KEY_TIME_OFFSET_X, 0)
        val timeOffsetY = prefs.getInt(KEY_TIME_OFFSET_Y, 0)
        val timeFoldedRotatedOffsetX = prefs.getInt(KEY_TIME_FOLDED_ROTATED_OFFSET_X, 0)
        val timeFoldedRotatedOffsetY = prefs.getInt(KEY_TIME_FOLDED_ROTATED_OFFSET_Y, 0)
        val timeInnerOffsetX = prefs.getInt(KEY_TIME_INNER_OFFSET_X, 0)
        val timeInnerOffsetY = prefs.getInt(KEY_TIME_INNER_OFFSET_Y, 0)
        val timeInnerRotatedOffsetX = prefs.getInt(KEY_TIME_INNER_ROTATED_OFFSET_X, 0)
        val timeInnerRotatedOffsetY = prefs.getInt(KEY_TIME_INNER_ROTATED_OFFSET_Y, 0)

        return OverlaySettings(
            isOverlayEnabled = enabled,
            colorMode = colorMode,
            scalePercent = scale,
            opacityPercent = opacity,
            positionPreset = preset,
            offsetX = offsetX,
            offsetY = offsetY,
            foldedRotatedOffsetX = foldedRotatedX,
            foldedRotatedOffsetY = foldedRotatedY,
            avoidDnd = avoidDnd,
            startOnBoot = startOnBoot,
            showSpeedInOverlay = showSpeed,
            centerPreference = centerPref,
            cutoutType = cutoutType,
            cutoutSizeDp = cutoutSize,
            cutoutOffsetXDp = cutoutOffsetX,
            foldScreenMode = foldMode,
            innerOffsetX = innerX,
            innerOffsetY = innerY,
            innerRotatedOffsetX = innerRotatedX,
            innerRotatedOffsetY = innerRotatedY,
            cameraCutoutStyle = cutoutStyle,
            showBatteryNumber = showBatteryNum,
            showTimeOnLeft = showTimeLeft,
            timeTextScalePercent = timeScale,
            showCellSignalDots = showCellDots,
            timeIndependentMovement = timeIndependent,
            timeOffsetX = timeOffsetX,
            timeOffsetY = timeOffsetY,
            timeFoldedRotatedOffsetX = timeFoldedRotatedOffsetX,
            timeFoldedRotatedOffsetY = timeFoldedRotatedOffsetY,
            timeInnerOffsetX = timeInnerOffsetX,
            timeInnerOffsetY = timeInnerOffsetY,
            timeInnerRotatedOffsetX = timeInnerRotatedOffsetX,
            timeInnerRotatedOffsetY = timeInnerRotatedOffsetY
        )
    }

    fun updateSettings(update: (OverlaySettings) -> OverlaySettings) {
        val current = _settingsFlow.value
        val newSettings = update(current)
        _settingsFlow.value = newSettings

        prefs.edit().apply {
            putBoolean(KEY_OVERLAY_ENABLED, newSettings.isOverlayEnabled)
            putString(KEY_COLOR_MODE, newSettings.colorMode.name)
            putInt(KEY_SCALE_PERCENT, newSettings.scalePercent)
            putInt(KEY_OPACITY_PERCENT, newSettings.opacityPercent)
            putString(KEY_POSITION_PRESET, newSettings.positionPreset.name)
            putInt(KEY_OFFSET_X, newSettings.offsetX)
            putInt(KEY_OFFSET_Y, newSettings.offsetY)
            putInt(KEY_FOLDED_ROTATED_OFFSET_X, newSettings.foldedRotatedOffsetX)
            putInt(KEY_FOLDED_ROTATED_OFFSET_Y, newSettings.foldedRotatedOffsetY)
            putBoolean(KEY_AVOID_DND, newSettings.avoidDnd)
            putBoolean(KEY_VISIBLE_IN_DND, !newSettings.avoidDnd)
            putBoolean(KEY_START_ON_BOOT, newSettings.startOnBoot)
            putBoolean(KEY_SHOW_SPEED, newSettings.showSpeedInOverlay)
            putString(KEY_CENTER_PREF, newSettings.centerPreference.name)
            putString(KEY_CUTOUT_TYPE, newSettings.cutoutType.name)
            putInt(KEY_CUTOUT_SIZE, newSettings.cutoutSizeDp)
            putInt(KEY_CUTOUT_OFFSET_X, newSettings.cutoutOffsetXDp)
            putString(KEY_FOLD_SCREEN_MODE, newSettings.foldScreenMode.name)
            putInt(KEY_INNER_OFFSET_X, newSettings.innerOffsetX)
            putInt(KEY_INNER_OFFSET_Y, newSettings.innerOffsetY)
            putInt(KEY_INNER_ROTATED_OFFSET_X, newSettings.innerRotatedOffsetX)
            putInt(KEY_INNER_ROTATED_OFFSET_Y, newSettings.innerRotatedOffsetY)
            putString(KEY_CAMERA_CUTOUT_STYLE, newSettings.cameraCutoutStyle.name)
            putBoolean(KEY_SHOW_BATTERY_NUMBER, newSettings.showBatteryNumber)
            putBoolean(KEY_SHOW_TIME_ON_LEFT, newSettings.showTimeOnLeft)
            putInt(KEY_TIME_TEXT_SCALE_PERCENT, newSettings.timeTextScalePercent)
            putBoolean(KEY_SHOW_CELL_SIGNAL_DOTS, newSettings.showCellSignalDots)
            putBoolean(KEY_TIME_INDEPENDENT_MOVEMENT, newSettings.timeIndependentMovement)
            putInt(KEY_TIME_OFFSET_X, newSettings.timeOffsetX)
            putInt(KEY_TIME_OFFSET_Y, newSettings.timeOffsetY)
            putInt(KEY_TIME_FOLDED_ROTATED_OFFSET_X, newSettings.timeFoldedRotatedOffsetX)
            putInt(KEY_TIME_FOLDED_ROTATED_OFFSET_Y, newSettings.timeFoldedRotatedOffsetY)
            putInt(KEY_TIME_INNER_OFFSET_X, newSettings.timeInnerOffsetX)
            putInt(KEY_TIME_INNER_OFFSET_Y, newSettings.timeInnerOffsetY)
            putInt(KEY_TIME_INNER_ROTATED_OFFSET_X, newSettings.timeInnerRotatedOffsetX)
            putInt(KEY_TIME_INNER_ROTATED_OFFSET_Y, newSettings.timeInnerRotatedOffsetY)
            apply()
        }
    }

    fun setTimeTextScalePercent(scalePercent: Int) {
        updateSettings { it.copy(timeTextScalePercent = scalePercent.coerceIn(70, 200)) }
    }

    fun setShowCellSignalDots(show: Boolean) {
        updateSettings { it.copy(showCellSignalDots = show) }
    }

    fun setShowBatteryNumber(show: Boolean) {
        updateSettings { it.copy(showBatteryNumber = show) }
    }

    fun setShowTimeOnLeft(show: Boolean) {
        updateSettings { it.copy(showTimeOnLeft = show) }
    }

    fun setCameraCutoutStyle(style: com.example.model.CameraCutoutStyle) {
        updateSettings { it.copy(cameraCutoutStyle = style) }
    }

    fun setCutoutSettings(type: com.example.model.PhoneCutoutType, sizeDp: Int = 13, offsetXDp: Int = 0) {
        updateSettings {
            it.copy(
                cutoutType = type,
                cutoutSizeDp = sizeDp,
                cutoutOffsetXDp = offsetXDp
            )
        }
    }

    fun setCenterPreference(preference: com.example.model.CenterDisplayPreference) {
        updateSettings { it.copy(centerPreference = preference) }
    }

    fun setOverlayEnabled(enabled: Boolean) {
        updateSettings { it.copy(isOverlayEnabled = enabled) }
    }

    fun setColorMode(mode: IndicatorColorMode) {
        updateSettings { it.copy(colorMode = mode) }
    }

    fun setScalePercent(scale: Int) {
        updateSettings { it.copy(scalePercent = scale.coerceIn(25, 150)) }
    }

    fun setOpacityPercent(opacity: Int) {
        updateSettings { it.copy(opacityPercent = opacity.coerceIn(40, 100)) }
    }

    fun setPosition(preset: PositionPreset, offsetX: Int, offsetY: Int) {
        updateSettings {
            it.copy(
                positionPreset = preset,
                offsetX = offsetX,
                offsetY = offsetY
            )
        }
    }

    fun setAvoidDnd(avoid: Boolean) {
        updateSettings { it.copy(avoidDnd = avoid) }
    }

    fun setStartOnBoot(start: Boolean) {
        updateSettings { it.copy(startOnBoot = start) }
    }

    fun setShowSpeed(show: Boolean) {
        updateSettings { it.copy(showSpeedInOverlay = show) }
    }

    fun setFoldScreenMode(mode: com.example.model.FoldScreenMode) {
        updateSettings { it.copy(foldScreenMode = mode) }
    }

    fun setInnerPosition(offsetX: Int, offsetY: Int) {
        updateSettings { it.copy(positionPreset = PositionPreset.CUSTOM, innerOffsetX = offsetX, innerOffsetY = offsetY) }
    }

    fun setFoldedRotatedPosition(offsetX: Int, offsetY: Int) {
        updateSettings { it.copy(positionPreset = PositionPreset.CUSTOM, foldedRotatedOffsetX = offsetX, foldedRotatedOffsetY = offsetY) }
    }

    fun setInnerRotatedPosition(offsetX: Int, offsetY: Int) {
        updateSettings { it.copy(positionPreset = PositionPreset.CUSTOM, innerRotatedOffsetX = offsetX, innerRotatedOffsetY = offsetY) }
    }

    fun setPositionForState(isUnfolded: Boolean, isRotated: Boolean, offsetX: Int, offsetY: Int, preset: PositionPreset = PositionPreset.CUSTOM) {
        updateSettings { current ->
            when {
                isUnfolded && isRotated -> current.copy(positionPreset = preset, innerRotatedOffsetX = offsetX, innerRotatedOffsetY = offsetY)
                isUnfolded && !isRotated -> current.copy(positionPreset = preset, innerOffsetX = offsetX, innerOffsetY = offsetY)
                !isUnfolded && isRotated -> current.copy(positionPreset = preset, foldedRotatedOffsetX = offsetX, foldedRotatedOffsetY = offsetY)
                else -> current.copy(positionPreset = preset, offsetX = offsetX, offsetY = offsetY)
            }
        }
    }

    fun setTimeIndependentMovement(enabled: Boolean) {
        updateSettings { it.copy(timeIndependentMovement = enabled) }
    }

    fun setTimePositionForState(isUnfolded: Boolean, isRotated: Boolean, offsetX: Int, offsetY: Int) {
        updateSettings { current ->
            when {
                isUnfolded && isRotated -> current.copy(timeInnerRotatedOffsetX = offsetX, timeInnerRotatedOffsetY = offsetY)
                isUnfolded && !isRotated -> current.copy(timeInnerOffsetX = offsetX, timeInnerOffsetY = offsetY)
                !isUnfolded && isRotated -> current.copy(timeFoldedRotatedOffsetX = offsetX, timeFoldedRotatedOffsetY = offsetY)
                else -> current.copy(timeOffsetX = offsetX, timeOffsetY = offsetY)
            }
        }
    }

    fun resetTimePosition(isUnfolded: Boolean, isRotated: Boolean) {
        setTimePositionForState(isUnfolded, isRotated, 0, 0)
    }

    companion object {
        private const val PREFS_NAME = "ostatus_preferences"
        private const val KEY_OVERLAY_ENABLED = "overlay_enabled"
        private const val KEY_COLOR_MODE = "color_mode"
        private const val KEY_SCALE_PERCENT = "scale_percent"
        private const val KEY_OPACITY_PERCENT = "opacity_percent"
        private const val KEY_POSITION_PRESET = "position_preset"
        private const val KEY_OFFSET_X = "offset_x"
        private const val KEY_OFFSET_Y = "offset_y"
        private const val KEY_FOLDED_ROTATED_OFFSET_X = "folded_rotated_offset_x"
        private const val KEY_FOLDED_ROTATED_OFFSET_Y = "folded_rotated_offset_y"
        private const val KEY_AVOID_DND = "avoid_dnd"
        private const val KEY_VISIBLE_IN_DND = "visible_in_dnd"
        private const val KEY_START_ON_BOOT = "start_on_boot"
        private const val KEY_SHOW_SPEED = "show_speed"
        private const val KEY_CENTER_PREF = "center_pref"
        private const val KEY_CUTOUT_TYPE = "cutout_type"
        private const val KEY_CUTOUT_SIZE = "cutout_size"
        private const val KEY_CUTOUT_OFFSET_X = "cutout_offset_x"
        private const val KEY_FOLD_SCREEN_MODE = "fold_screen_mode"
        private const val KEY_INNER_OFFSET_X = "inner_offset_x"
        private const val KEY_INNER_OFFSET_Y = "inner_offset_y"
        private const val KEY_INNER_ROTATED_OFFSET_X = "inner_rotated_offset_x"
        private const val KEY_INNER_ROTATED_OFFSET_Y = "inner_rotated_offset_y"
        private const val KEY_CAMERA_CUTOUT_STYLE = "camera_cutout_style"
        private const val KEY_SHOW_BATTERY_NUMBER = "show_battery_number"
        private const val KEY_SHOW_TIME_ON_LEFT = "show_time_on_left"
        private const val KEY_TIME_TEXT_SCALE_PERCENT = "time_text_scale_percent"
        private const val KEY_SHOW_CELL_SIGNAL_DOTS = "show_cell_signal_dots"
        private const val KEY_TIME_INDEPENDENT_MOVEMENT = "time_independent_movement"
        private const val KEY_TIME_OFFSET_X = "time_offset_x"
        private const val KEY_TIME_OFFSET_Y = "time_offset_y"
        private const val KEY_TIME_FOLDED_ROTATED_OFFSET_X = "time_folded_rotated_offset_x"
        private const val KEY_TIME_FOLDED_ROTATED_OFFSET_Y = "time_folded_rotated_offset_y"
        private const val KEY_TIME_INNER_OFFSET_X = "time_inner_offset_x"
        private const val KEY_TIME_INNER_OFFSET_Y = "time_inner_offset_y"
        private const val KEY_TIME_INNER_ROTATED_OFFSET_X = "time_inner_rotated_offset_x"
        private const val KEY_TIME_INNER_ROTATED_OFFSET_Y = "time_inner_rotated_offset_y"

        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
