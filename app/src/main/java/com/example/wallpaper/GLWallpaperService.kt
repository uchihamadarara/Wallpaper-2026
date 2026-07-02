package com.example.wallpaper

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.opengl.GLES30
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.util.Log

abstract class GLWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return GLWallpaperEngine()
    }

    abstract fun createRenderer(): GLRenderer

    inner class GLWallpaperEngine : Engine() {
        private var eglDisplay = EGL14.EGL_NO_DISPLAY
        private var eglContext = EGL14.EGL_NO_CONTEXT
        private var eglSurface = EGL14.EGL_NO_SURFACE
        private var eglConfig: EGLConfig? = null

        private var renderer: GLRenderer? = null
        private var isDrawing = false
        private var isVisible = false

        private var renderThread: Thread? = null

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            renderer = createRenderer()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisible = visible
            if (visible) {
                renderer?.onVisibilityChanged(true)
                resumeRendering()
            } else {
                pauseRendering()
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            startEglThread()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            renderer?.onSurfaceChanged(width, height)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            stopEglThread()
        }

        override fun onDestroy() {
            super.onDestroy()
            renderer?.onDestroy()
        }

        override fun onOffsetsChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset)
            renderer?.onOffsetsChanged(xOffset, yOffset)
        }

        private fun startEglThread() {
            isDrawing = true
            renderThread = Thread {
                initEGL()
                renderer?.onSurfaceCreated()
                while (isDrawing) {
                    if (isVisible) {
                        renderer?.onDrawFrame()
                        EGL14.eglSwapBuffers(eglDisplay, eglSurface)
                    } else {
                        Thread.sleep(100)
                    }
                    val targetFps = renderer?.getTargetFPS() ?: 60
                    val delay = 1000L / targetFps
                    Thread.sleep(delay)
                }
                destroyEGL()
            }.apply { start() }
        }

        private fun stopEglThread() {
            isDrawing = false
            renderThread?.join()
            renderThread = null
        }

        private fun resumeRendering() {
            // Wake up if needed
        }

        private fun pauseRendering() {
            // Sleep if needed
        }

        private fun initEGL() {
            eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val version = IntArray(2)
            EGL14.eglInitialize(eglDisplay, version, 0, version, 1)

            val configAttribs = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 16,
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(eglDisplay, configAttribs, 0, configs, 0, 1, numConfigs, 0)
            eglConfig = configs[0]

            val contextAttribs = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL14.EGL_NONE
            )
            eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)

            eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surfaceHolder.surface, intArrayOf(EGL14.EGL_NONE), 0)
            EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
        }

        private fun destroyEGL() {
            EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroySurface(eglDisplay, eglSurface)
            EGL14.eglDestroyContext(eglDisplay, eglContext)
            EGL14.eglTerminate(eglDisplay)
        }
    }

    interface GLRenderer {
        fun onSurfaceCreated()
        fun onSurfaceChanged(width: Int, height: Int)
        fun onDrawFrame()
        fun onVisibilityChanged(visible: Boolean) {}
        fun onOffsetsChanged(xOffset: Float, yOffset: Float) {}
        fun onDestroy()
        fun getTargetFPS(): Int = 60
    }
}
