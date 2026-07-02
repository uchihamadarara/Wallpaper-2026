package com.example.wallpaper

import android.content.Context
import android.opengl.GLES30
import android.opengl.Matrix
import android.os.SystemClock
import com.example.data.local.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.sin

class NovaGLRenderer(
    private val context: Context,
    private val settingsManager: SettingsManager
) : GLWallpaperService.GLRenderer {

    private var layerProgram = 0
    private var particleProgram = 0
    private var chargingProgram = 0

    // Charging Shader handles
    private var cPosHandle = 0
    private var cTexCoordHandle = 0
    private var cMvpHandle = 0
    private var cTimeHandle = 0
    private var cAlphaHandle = 0
    private var currentChargingAnim = 0f

    // Layer Shader handles
    private var lPosHandle = 0
    private var lTexCoordHandle = 0
    private var lMvpHandle = 0
    private var lOffsetHandle = 0
    private var lScaleHandle = 0
    private var lTexHandle = 0
    private var lAlphaHandle = 0
    private var lColorTintHandle = 0
    private var lBlurHandle = 0

    private var lScrollHandle = 0

    // Particle Shader handles
    private var pPosHandle = 0
    private var pColorHandle = 0
    private var pSizeLifeHandle = 0
    private var pMvpHandle = 0

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private val gyroscopeManager = GyroscopeManager(context)
    
    private lateinit var quad: Quad
    private lateinit var particleSystem: ParticleSystem
    private val textureManager = TextureManager()
    private var postProcessor: PostProcessor? = null
    
    private var parallaxMultiplier = 1.0f
    private var isBatterySaver = false
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private var unlockTime = 0L
    private var lockTime = 0L
    private var isVisible = false
    private var currentUnlockAnim = 0f
    
    private var launcherOffsetX = 0f
    private var launcherOffsetY = 0f
    
    private var isCharging = false
    private var lastTime = 0L

    data class Layer(
        val textureUrl: String, 
        val depthMultiplier: Float, 
        val scale: Float, 
        var textureId: Int = 0,
        val scrollSpeedX: Float = 0f,
        val scrollSpeedY: Float = 0f,
        val isAdditive: Boolean = false,
        val alpha: Float = 1f
    )

    // Standard 2.5D Parallax layers
    private var layers = mutableListOf(
        Layer("https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Land_ocean_ice_2048.jpg/1024px-Land_ocean_ice_2048.jpg", 0.1f, 1.1f), // BG
        Layer("https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Clouds_2048.jpg/1024px-Clouds_2048.jpg", 0.3f, 1.2f, scrollSpeedX = 0.02f), // MG (Clouds/Fog slowly moving)
        Layer("https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Clouds_2048.jpg/1024px-Clouds_2048.jpg", 0.5f, 1.3f, scrollSpeedX = -0.01f, scrollSpeedY = 0.01f, isAdditive = true, alpha = 0.5f), // Fast Fog (additive)
        Layer("", 0.6f, 1.4f) // FG
    )
    
    private var isSurfaceCreated = false

    fun setWallpaper(entity: com.example.data.local.WallpaperEntity?) {
        if (entity?.layersJson != null) {
            try {
                val jsonArray = org.json.JSONArray(entity.layersJson)
                val newLayers = mutableListOf<Layer>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    newLayers.add(
                        Layer(
                            textureUrl = obj.optString("url", ""),
                            depthMultiplier = obj.optDouble("depth", 0.1).toFloat(),
                            scale = obj.optDouble("scale", 1.1).toFloat(),
                            scrollSpeedX = obj.optDouble("scrollX", 0.0).toFloat(),
                            scrollSpeedY = obj.optDouble("scrollY", 0.0).toFloat(),
                            isAdditive = obj.optBoolean("additive", false),
                            alpha = obj.optDouble("alpha", 1.0).toFloat()
                        )
                    )
                }
                layers = newLayers
                // If surface is already created, load textures immediately
                if (isSurfaceCreated) {
                    loadLayerTextures()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadLayerTextures() {
        layers.forEach { layer ->
            if (layer.textureUrl.isNotEmpty()) {
                textureManager.loadTexture(layer.textureUrl) { id ->
                    layer.textureId = id
                }
            }
        }
    }

    fun triggerChargingAnimation(charging: Boolean) {
        isCharging = charging
    }

    override fun onSurfaceCreated() {
        unlockTime = SystemClock.uptimeMillis()
        lastTime = System.nanoTime()
        
        GLES30.glDisable(GLES30.GL_DEPTH_TEST) // Depth sorting is done by drawing order in 2.5D
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        
        val lVS = loadShader(GLES30.GL_VERTEX_SHADER, LayerShader.vertexShaderCode)
        val lFS = loadShader(GLES30.GL_FRAGMENT_SHADER, LayerShader.fragmentShaderCode)
        layerProgram = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, lVS)
            GLES30.glAttachShader(it, lFS)
            GLES30.glLinkProgram(it)
        }
        
        val pVS = loadShader(GLES30.GL_VERTEX_SHADER, ParticleShader.vertexShaderCode)
        val pFS = loadShader(GLES30.GL_FRAGMENT_SHADER, ParticleShader.fragmentShaderCode)
        particleProgram = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, pVS)
            GLES30.glAttachShader(it, pFS)
            GLES30.glLinkProgram(it)
        }
        
        val cVS = loadShader(GLES30.GL_VERTEX_SHADER, ChargingRingShader.vertexShaderCode)
        val cFS = loadShader(GLES30.GL_FRAGMENT_SHADER, ChargingRingShader.fragmentShaderCode)
        chargingProgram = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, cVS)
            GLES30.glAttachShader(it, cFS)
            GLES30.glLinkProgram(it)
        }
        
        quad = Quad()
        particleSystem = ParticleSystem(150)
        
        gyroscopeManager.register()
        
        scope.launch {
            settingsManager.parallaxStrength.collectLatest { strength ->
                parallaxMultiplier = strength / 50f
            }
        }
        
        scope.launch {
            settingsManager.batterySaver.collectLatest { saver ->
                isBatterySaver = saver
            }
        }
        
        isSurfaceCreated = true
        loadLayerTextures()
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        if (postProcessor == null) {
            postProcessor = PostProcessor(width, height)
        } else {
            postProcessor?.resize(width, height)
        }
        
        GLES30.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height.toFloat()
        
        // Orthographic projection for 2D layers
        Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, -1f, 10f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame() {
        textureManager.processPendingTextures()
        
        postProcessor?.beginCapture()
        
        val currentTime = System.nanoTime()
        val dt = if (lastTime > 0) (currentTime - lastTime) / 1e9f else 0.016f
        lastTime = currentTime
        
        GLES30.glClearColor(0.02f, 0.02f, 0.03f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        gyroscopeManager.update()
        
        val targetUnlockAnim = if (isVisible) 1f else 0f
        currentUnlockAnim += (targetUnlockAnim - currentUnlockAnim) * (dt * 4f).coerceAtMost(1f)
        
        val unlockZoom = (1f - currentUnlockAnim) * 0.3f
        val unlockAlpha = currentUnlockAnim
        
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        val timeOfDay = hour + minute / 60f
        
        var tintR = 1f; var tintG = 1f; var tintB = 1f
        if (timeOfDay < 5f) {
            tintR = 0.5f; tintG = 0.6f; tintB = 0.9f
        } else if (timeOfDay < 7f) {
            val t = (timeOfDay - 5f) / 2f
            tintR = 0.5f * (1-t) + 1.0f * t
            tintG = 0.6f * (1-t) + 0.9f * t
            tintB = 0.9f * (1-t) + 0.8f * t
        } else if (timeOfDay < 17f) {
            tintR = 1.0f; tintG = 1.0f; tintB = 1.0f
        } else if (timeOfDay < 19f) {
            val t = (timeOfDay - 17f) / 2f
            tintR = 1.0f * (1-t) + 1.0f * t
            tintG = 1.0f * (1-t) + 0.7f * t
            tintB = 1.0f * (1-t) + 0.4f * t
        } else if (timeOfDay < 21f) {
            val t = (timeOfDay - 19f) / 2f
            tintR = 1.0f * (1-t) + 0.5f * t
            tintG = 0.7f * (1-t) + 0.6f * t
            tintB = 0.4f * (1-t) + 0.9f * t
        } else {
            tintR = 0.5f; tintG = 0.6f; tintB = 0.9f
        }
        
        // Breathing animation
        val breathing = sin((SystemClock.uptimeMillis() % 10000) / 10000.0 * Math.PI * 2).toFloat() * 0.02f

        // Draw Layers
        GLES30.glUseProgram(layerProgram)
        lPosHandle = GLES30.glGetAttribLocation(layerProgram, "vPosition")
        lTexCoordHandle = GLES30.glGetAttribLocation(layerProgram, "vTexCoord")
        lMvpHandle = GLES30.glGetUniformLocation(layerProgram, "uMVPMatrix")
        lOffsetHandle = GLES30.glGetUniformLocation(layerProgram, "uOffset")
        lScaleHandle = GLES30.glGetUniformLocation(layerProgram, "uScale")
        lScrollHandle = GLES30.glGetUniformLocation(layerProgram, "uScroll")
        lTexHandle = GLES30.glGetUniformLocation(layerProgram, "uTexture")
        lAlphaHandle = GLES30.glGetUniformLocation(layerProgram, "uAlpha")
        lColorTintHandle = GLES30.glGetUniformLocation(layerProgram, "uColorTint")
        lBlurHandle = GLES30.glGetUniformLocation(layerProgram, "uBlur")
        
        GLES30.glUniformMatrix4fv(lMvpHandle, 1, false, mvpMatrix, 0)
        
        val timeSecs = SystemClock.uptimeMillis() / 1000f
        
        layers.forEach { layer ->
            if (layer.textureId != 0) {
                if (layer.isAdditive) {
                    GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE)
                } else {
                    GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
                }
                
                GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, layer.textureId)
                GLES30.glUniform1i(lTexHandle, 0)
                
                // Gyro offset + Parallax Multiplier + Launcher Offset
                val offsetX = gyroscopeManager.currentRoll * layer.depthMultiplier * parallaxMultiplier + launcherOffsetX * layer.depthMultiplier
                val offsetY = gyroscopeManager.currentPitch * layer.depthMultiplier * parallaxMultiplier + launcherOffsetY * layer.depthMultiplier
                
                GLES30.glUniform2f(lOffsetHandle, offsetX, offsetY)
                GLES30.glUniform2f(lScrollHandle, layer.scrollSpeedX * timeSecs, layer.scrollSpeedY * timeSecs)
                
                // Scale with breathing and unlock burst
                val finalScale = layer.scale + breathing + unlockZoom * layer.depthMultiplier
                GLES30.glUniform1f(lScaleHandle, finalScale)
                
                GLES30.glUniform1f(lAlphaHandle, layer.alpha * unlockAlpha)
                
                val layerBlur = (1f - currentUnlockAnim) * 10f + (if (layer.depthMultiplier < 0.2f) 2f else 0f)
                GLES30.glUniform1f(lBlurHandle, layerBlur)
                GLES30.glUniform3f(lColorTintHandle, tintR, tintG, tintB)
                
                quad.draw(lPosHandle, lTexCoordHandle)
            }
        }
        
        // Restore blend func for particles
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE) // Additive blending for glow

        
        GLES30.glUseProgram(particleProgram)
        pPosHandle = GLES30.glGetAttribLocation(particleProgram, "vPosition")
        pColorHandle = GLES30.glGetAttribLocation(particleProgram, "vColor")
        pSizeLifeHandle = GLES30.glGetAttribLocation(particleProgram, "vSizeLife")
        pMvpHandle = GLES30.glGetUniformLocation(particleProgram, "uMVPMatrix")
        
        val pViewMatrix = FloatArray(16)
        val pMvp = FloatArray(16)
        val pOffsetX = gyroscopeManager.currentRoll * parallaxMultiplier * 1.5f
        val pOffsetY = gyroscopeManager.currentPitch * parallaxMultiplier * 1.5f
        
        Matrix.setLookAtM(pViewMatrix, 0, pOffsetX, pOffsetY, 3f, pOffsetX, pOffsetY, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(pMvp, 0, projectionMatrix, 0, pViewMatrix, 0)
        
        GLES30.glUniformMatrix4fv(pMvpHandle, 1, false, pMvp, 0)
        
        particleSystem.update(dt.coerceAtMost(0.05f), isCharging)
        particleSystem.draw(pPosHandle, pColorHandle, pSizeLifeHandle)
        
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        
        val targetChargingAnim = if (isCharging) 1f else 0f
        currentChargingAnim += (targetChargingAnim - currentChargingAnim) * (dt * 5f).coerceAtMost(1f)
        
        if (currentChargingAnim > 0.01f) {
            GLES30.glUseProgram(chargingProgram)
            cPosHandle = GLES30.glGetAttribLocation(chargingProgram, "vPosition")
            cTexCoordHandle = GLES30.glGetAttribLocation(chargingProgram, "vTexCoord")
            cMvpHandle = GLES30.glGetUniformLocation(chargingProgram, "uMVPMatrix")
            cTimeHandle = GLES30.glGetUniformLocation(chargingProgram, "uTime")
            cAlphaHandle = GLES30.glGetUniformLocation(chargingProgram, "uAlpha")
            
            val cScale = 0.5f + (1f - currentChargingAnim) * 0.5f // scales down when appearing
            val cMvp = FloatArray(16)
            val cMvp2 = FloatArray(16)
            Matrix.setIdentityM(cMvp2, 0)
            Matrix.scaleM(cMvp2, 0, cScale, cScale, 1f)
            Matrix.multiplyMM(cMvp, 0, projectionMatrix, 0, cMvp2, 0)

            GLES30.glUniformMatrix4fv(cMvpHandle, 1, false, cMvp, 0)
            GLES30.glUniform1f(cTimeHandle, timeSecs)
            GLES30.glUniform1f(cAlphaHandle, currentChargingAnim * unlockAlpha)
            
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE)
            quad.draw(cPosHandle, cTexCoordHandle)
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        }
        
        val bloomIntensity = 1.0f + (1f - currentUnlockAnim) * 2.0f
        postProcessor?.endCaptureAndRender(bloomIntensity)
    }

    override fun onVisibilityChanged(visible: Boolean) {
        isVisible = visible
        if (visible) {
            unlockTime = SystemClock.uptimeMillis()
            lastTime = System.nanoTime()
            gyroscopeManager.register()
            if (::particleSystem.isInitialized) {
                particleSystem.triggerBurst()
            }
        } else {
            lockTime = SystemClock.uptimeMillis()
            gyroscopeManager.unregister()
            if (::particleSystem.isInitialized) {
                particleSystem.triggerLock()
            }
        }
    }

    override fun onOffsetsChanged(xOffset: Float, yOffset: Float) {
        launcherOffsetX = (xOffset - 0.5f) * 2f
        launcherOffsetY = (yOffset - 0.5f) * 2f
    }

    override fun getTargetFPS(): Int {
        return if (isBatterySaver) 30 else 60
    }

    override fun onDestroy() {
        gyroscopeManager.unregister()
        job.cancel()
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES30.glCreateShader(type).also { shader ->
            GLES30.glShaderSource(shader, shaderCode)
            GLES30.glCompileShader(shader)
        }
    }
}
