package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.data.local.SettingsManager
import com.example.data.local.NovaDatabase
import com.example.wallpaper.GLWallpaperService
import com.example.wallpaper.NovaGLRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class NovaWallpaperService : GLWallpaperService() {
    private val settingsManager: SettingsManager by inject()
    private val database: NovaDatabase by inject()
    private var renderer: NovaGLRenderer? = null
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_POWER_CONNECTED) {
                renderer?.triggerChargingAnimation(true)
            } else if (intent?.action == Intent.ACTION_POWER_DISCONNECTED) {
                renderer?.triggerChargingAnimation(false)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(powerReceiver, filter)
        
        serviceScope.launch {
            database.wallpaperDao().getCurrentWallpaper().collectLatest { wallpaper ->
                renderer?.setWallpaper(wallpaper)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(powerReceiver)
        serviceJob.cancel()
    }

    override fun createRenderer(): GLRenderer {
        val newRenderer = NovaGLRenderer(this, settingsManager)
        renderer = newRenderer
        return newRenderer
    }
}
