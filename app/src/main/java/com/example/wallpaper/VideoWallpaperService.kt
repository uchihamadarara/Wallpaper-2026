package com.example.wallpaper

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.local.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class VideoWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return VideoEngine()
    }

    inner class VideoEngine : Engine() {
        private var player: ExoPlayer? = null
        private lateinit var keyguardManager: KeyguardManager
        private var isCharging = false
        private var isLocked = false
        
        private var homeVideoPath: String? = null
        private var lockVideoPath: String? = null
        private var chargingVideoPath: String? = null
        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        
        // Broadcast Receiver to detect charging state
        private val powerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        isCharging = true
                        updateVideoSource()
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        isCharging = false
                        updateVideoSource()
                    }
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            
            val settingsManager = SettingsManager(applicationContext)
            scope.launch {
                settingsManager.activeHomeVideo.collect { path ->
                    homeVideoPath = path
                    updateVideoSource()
                }
            }
            scope.launch {
                settingsManager.activeLockVideo.collect { path ->
                    lockVideoPath = path
                    updateVideoSource()
                }
            }
            scope.launch {
                settingsManager.activeChargingVideo.collect { path ->
                    chargingVideoPath = path
                    updateVideoSource()
                }
            }
            
            // Register charging receiver
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            registerReceiver(powerReceiver, filter)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            setupPlayer()
            player?.setVideoSurfaceHolder(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                // Check if screen is locked when it becomes visible
                val currentlyLocked = keyguardManager.isKeyguardLocked
                if (isLocked != currentlyLocked) {
                    isLocked = currentlyLocked
                    updateVideoSource()
                }
                player?.play()
            } else {
                player?.pause()
            }
        }

        private fun setupPlayer() {
            if (player == null) {
                player = ExoPlayer.Builder(applicationContext).build().apply {
                    repeatMode = Player.REPEAT_MODE_ONE
                    volume = 0f
                }
                updateVideoSource()
            }
        }
        
        private fun updateVideoSource() {
            player?.let { p ->
                val videoUri = when {
                    isCharging && chargingVideoPath != null -> chargingVideoPath!!
                    isLocked && lockVideoPath != null -> lockVideoPath!!
                    homeVideoPath != null -> homeVideoPath!!
                    else -> "asset:///sample_video.mp4"
                }
                
                val mediaItem = MediaItem.fromUri(Uri.parse(videoUri))
                p.setMediaItem(mediaItem)
                p.prepare()
                p.play()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            player?.release()
            player = null
        }

        override fun onDestroy() {
            super.onDestroy()
            scope.cancel()
            unregisterReceiver(powerReceiver)
            player?.release()
            player = null
        }
    }
}
