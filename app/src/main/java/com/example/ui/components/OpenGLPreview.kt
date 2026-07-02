package com.example.ui.components

import android.content.Context
import android.opengl.GLSurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.local.SettingsManager
import com.example.wallpaper.NovaGLRenderer
import org.koin.compose.koinInject

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@Composable
fun OpenGLPreview(modifier: Modifier = Modifier, wallpaper: com.example.data.local.WallpaperEntity? = null) {
    val context = LocalContext.current
    val settingsManager: SettingsManager = koinInject()
    val lifecycleOwner = LocalLifecycleOwner.current

    val renderer = remember { NovaGLRenderer(context, settingsManager) }

    val glSurfaceView = remember {
        GLSurfaceView(context).apply {
            setEGLContextClientVersion(3)
            setEGLConfigChooser(8, 8, 8, 8, 16, 0)
            
            setRenderer(object : GLSurfaceView.Renderer {
                override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                    renderer.onSurfaceCreated()
                }

                override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                    renderer.onSurfaceChanged(width, height)
                }

                override fun onDrawFrame(gl: GL10?) {
                    renderer.onDrawFrame()
                }
            })
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            
            // Allow triggering visibility events
            renderer.onVisibilityChanged(true)
        }
    }
    
    androidx.compose.runtime.LaunchedEffect(wallpaper) {
        glSurfaceView.queueEvent {
            renderer.setWallpaper(wallpaper)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    glSurfaceView.onResume()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    glSurfaceView.onPause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Ideally notify renderer to unregister sensors, etc.
        }
    }

    AndroidView(
        factory = { glSurfaceView },
        modifier = modifier
    )
}
