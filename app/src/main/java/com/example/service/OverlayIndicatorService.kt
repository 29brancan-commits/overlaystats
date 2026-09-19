package com.example.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.PreferencesManager
import com.example.data.StatusMonitor
import com.example.model.CameraCutoutStyle
import com.example.model.DeviceStatus
import com.example.model.OverlaySettings
import com.example.model.PhoneCutoutType
import com.example.model.PositionPreset
import com.example.ui.components.DuoOverlayView
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class OverlayIndicatorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var windowManager: WindowManager? = null
    private var overlayView: DuoOverlayView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private lateinit var prefsManager: PreferencesManager
    private lateinit var statusMonitor: StatusMonitor

    private var currentSettings = OverlaySettings()
    private var currentStatus = DeviceStatus()

    private var cameraManager: CameraManager? = null
    private var isCameraActive = false

    private val cameraCallback = object : CameraManager.AvailabilityCallback() {
        override fun onCameraUnavailable(cameraId: String) {
            super.onCameraUnavailable(cameraId)
            isCameraActive = true
            overlayView?.setCameraInUse(true)
        }

        override fun onCameraAvailable(cameraId: String) {
            super.onCameraAvailable(cameraId)
            isCameraActive = false
            overlayView?.setCameraInUse(false)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefsManager = PreferencesManager.getInstance(this)
        statusMonitor = StatusMonitor.getInstance(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager

        try {
            cameraManager = getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            cameraManager?.registerAvailabilityCallback(cameraCallback, null)
        } catch (_: Exception) {}

        startForegroundNotification()
        observeState()
    }

    private fun startForegroundNotification() {
        val channelId = "ostatus_service_channel"
        val channelName = "O.status Overlay"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows O.status indicator overlay service status"
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }

        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("O.status Active")
            .setContentText("Duo status indicator running")
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun observeState() {
        serviceScope.launch {
            combine(statusMonitor.statusFlow, prefsManager.settingsFlow) { status, settings ->
                Pair(status, settings)
            }.collect { (status, settings) ->
                currentStatus = status
                currentSettings = settings
                updateOverlay()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun updateOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            removeOverlayView()
            return
        }

        if (!currentSettings.isOverlayEnabled) {
            removeOverlayView()
            return
        }

        // Avoid DND logic
        if (currentSettings.avoidDnd && currentStatus.isDndActive) {
            overlayView?.visibility = View.GONE
            return
        } else {
            overlayView?.visibility = View.VISIBLE
        }

        if (overlayView == null) {
            initOverlayView()
        } else {
            overlayView?.updateData(currentStatus, currentSettings)
            updateOverlayPosition()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOverlayView() {
        val view = DuoOverlayView(this).apply {
            setCameraInUse(isCameraActive)
            updateData(currentStatus, currentSettings)
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val (initX, initY) = calculateWindowPosition(currentSettings)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            x = initX
            y = initY
        }

        // Attach listener for hardware display cutout detection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.setOnApplyWindowInsetsListener { _, insets ->
                if (currentSettings.positionPreset == PositionPreset.WRAP_CUTOUT) {
                    updateOverlayPosition()
                }
                insets
            }
        }

        try {
            windowManager?.addView(view, params)
            overlayView = view
            layoutParams = params
        } catch (_: Exception) {
        }
    }

    private fun getHardwareCutoutBounds(): android.graphics.Rect? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = getSystemService(WindowManager::class.java)
            val cutout = wm?.currentWindowMetrics?.windowInsets?.displayCutout
            val rects = cutout?.boundingRects
            if (!rects.isNullOrEmpty()) {
                return rects.minByOrNull { it.top }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val insets = overlayView?.rootWindowInsets ?: return null
            val cutout = insets.displayCutout ?: return null
            val rects = cutout.boundingRects
            if (!rects.isNullOrEmpty()) {
                return rects.minByOrNull { it.top }
            }
        }
        return null
    }

    private data class OverlayGeometry(
        val density: Float,
        val scale: Float,
        val baseDimPx: Float,
        val ringRadiusPx: Float,
        val timeOffsetPx: Float,
        val viewCx: Float,
        val viewCy: Float
    )

    private fun getOverlayGeometry(settings: OverlaySettings): OverlayGeometry {
        val dm = resources.displayMetrics
        val density = dm.density
        val scale = (settings.scalePercent / 100f).coerceIn(0.25f, 1.5f)
        val baseDimPx = 54f * density * scale
        val ringRadiusPx = baseDimPx / 2f
        val timeScale = (settings.timeTextScalePercent / 100f).coerceIn(0.7f, 2.0f)
        val timePillWBase = 64f * timeScale
        val timeOffsetPx = if (settings.showTimeOnLeft) ((timePillWBase + 6f) * density * scale) else 0f
        val isWrapCutout = settings.positionPreset == PositionPreset.WRAP_CUTOUT
        val circleWPx = if (isWrapCutout && settings.cameraCutoutStyle == CameraCutoutStyle.FLANKING_WINGS) {
            baseDimPx + (40f * density * scale)
        } else {
            baseDimPx
        }
        val viewCx = timeOffsetPx + (circleWPx / 2f)
        val viewCy = ringRadiusPx
        return OverlayGeometry(
            density = density,
            scale = scale,
            baseDimPx = baseDimPx,
            ringRadiusPx = ringRadiusPx,
            timeOffsetPx = timeOffsetPx,
            viewCx = viewCx,
            viewCy = viewCy
        )
    }

    private fun calculateTargetCenter(settings: OverlaySettings, geom: OverlayGeometry): Pair<Float, Float> {
        val density = geom.density
        val isUnfolded = isDeviceUnfolded()
        val isRotated = isDeviceRotated()

        val (targetXDp, targetYDp) = settings.getCurrentOffset(isUnfolded, isRotated)
        return (targetXDp * density) to (targetYDp * density)
    }

    private fun calculateWindowPosition(settings: OverlaySettings): Pair<Int, Int> {
        val dm = resources.displayMetrics
        val screenW = dm.widthPixels
        val screenH = dm.heightPixels
        val geom = getOverlayGeometry(settings)
        val (targetCenterXPx, targetCenterYPx) = calculateTargetCenter(settings, geom)

        val windowX = (targetCenterXPx - geom.viewCx).roundToInt()
        val windowY = (targetCenterYPx - geom.viewCy).roundToInt()

        // Clamp safely so the indicator cannot disappear off-screen while allowing
        // the ring center to align with the camera cutout near the top edge
        val minWinX = -geom.viewCx.toInt()
        val maxWinX = (screenW - (geom.viewCx + geom.ringRadiusPx).toInt()).coerceAtLeast(minWinX)
        val minWinY = -geom.viewCy.toInt()
        val maxWinY = (screenH - (geom.viewCy + geom.ringRadiusPx).toInt()).coerceAtLeast(minWinY)
        val clampedX = windowX.coerceIn(minWinX, maxWinX)
        val clampedY = windowY.coerceIn(minWinY, maxWinY)
        return clampedX to clampedY
    }

    private fun updateOverlayPosition() {
        val p = layoutParams ?: return
        val v = overlayView ?: return
        val (winX, winY) = calculateWindowPosition(currentSettings)
        p.x = winX
        p.y = winY

        try {
            v.requestLayout()
            windowManager?.updateViewLayout(v, p)
        } catch (_: Exception) {
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // Detect Galaxy Z Fold fold/unfold screen width transition and rotation
        updateOverlayPosition()
    }

    private fun isDeviceRotated(): Boolean {
        return resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    }

    private fun isDeviceUnfolded(): Boolean {
        val dm = resources.displayMetrics
        val widthDp = dm.widthPixels / dm.density
        val heightDp = dm.heightPixels / dm.density
        val smallestWidthDp = kotlin.math.min(widthDp, heightDp)
        return when (currentSettings.foldScreenMode) {
            com.example.model.FoldScreenMode.COVER_SCREEN -> false
            com.example.model.FoldScreenMode.INNER_SCREEN -> true
            com.example.model.FoldScreenMode.AUTO_DETECT -> smallestWidthDp >= 600
        }
    }

    private fun removeOverlayView() {
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {
            }
            overlayView = null
            layoutParams = null
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // One-shot recovery after app task is removed
        super.onTaskRemoved(rootIntent)
        if (currentSettings.isOverlayEnabled) {
            val restartServiceIntent = Intent(applicationContext, OverlayIndicatorService::class.java).apply {
                setPackage(packageName)
            }
            val restartPendingIntent = PendingIntent.getService(
                applicationContext,
                1,
                restartServiceIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmService = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmService?.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            cameraManager?.unregisterAvailabilityCallback(cameraCallback)
        } catch (_: Exception) {}
        serviceScope.cancel()
        removeOverlayView()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, OverlayIndicatorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, OverlayIndicatorService::class.java)
            context.stopService(intent)
        }
    }
}
