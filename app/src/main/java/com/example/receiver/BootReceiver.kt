package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.data.PreferencesManager
import com.example.service.OverlayIndicatorService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val prefs = PreferencesManager.getInstance(context)
            val settings = prefs.settingsFlow.value
            if (settings.startOnBoot && settings.isOverlayEnabled) {
                if (Settings.canDrawOverlays(context)) {
                    OverlayIndicatorService.start(context)
                }
            }
        }
    }
}
