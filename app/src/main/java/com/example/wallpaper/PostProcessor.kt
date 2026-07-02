package com.example.wallpaper

import android.opengl.GLES30

class PostProcessor(private var width: Int, private var height: Int) {
    
    private var baseFbo = 0
    private var baseTexture = 0
    private var depthBuffer = 0
    
    private var blurFbo = 0
    private var blurTexture = 0
    
    private lateinit var quad: Quad
    
    private var extractProgram = 0
    private var blurProgram = 0
    private var combineProgram = 0
    
    private val blurScale = 0.25f // Render blur at 1/4 resolution for performance

    init {
        quad = Quad()
        initShaders()
        setupFBOs(width, height)
    }
    
    private fun initShaders() {
        val quadVS = """
            #version 300 es
            in vec4 vPosition;
            in vec2 vTexCoord;
            out vec2 fTexCoord;
            void main() {
                gl_Position = vPosition;
                fTexCoord = vTexCoord;
            }
        """.trimIndent()
        
        val extractFS = """
            #version 300 es
            precision mediump float;
            in vec2 fTexCoord;
            uniform sampler2D uTexture;
            out vec4 fragColor;
            void main() {
                vec4 color = texture(uTexture, fTexCoord);
                // Extract bright parts
                float brightness = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
                if(brightness > 0.8) {
                    fragColor = vec4(color.rgb, 1.0);
                } else {
                    fragColor = vec4(0.0, 0.0, 0.0, 1.0);
                }
            }
        """.trimIndent()
        
        val combineFS = """
            #version 300 es
            precision mediump float;
            in vec2 fTexCoord;
            uniform sampler2D uBaseTexture;
            uniform sampler2D uBlurTexture;
            uniform float uBloomIntensity;
            out vec4 fragColor;
            void main() {
                vec4 base = texture(uBaseTexture, fTexCoord);
                vec4 bloom = texture(uBlurTexture, fTexCoord);
                fragColor = base + bloom * uBloomIntensity; // Additive blend with adjustable intensity
            }
        """.trimIndent()
        
        val blurFS = """
            #version 300 es
            precision mediump float;
            in vec2 fTexCoord;
            uniform sampler2D uTexture;
            uniform vec2 uTexelSize;
            uniform vec2 uDirection; // (1,0) for horizontal, (0,1) for vertical
            out vec4 fragColor;
            
            // Simple 5-tap gaussian
            const float weight[5] = float[](0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);
            
            void main() {
                vec3 result = texture(uTexture, fTexCoord).rgb * weight[0];
                for(int i = 1; i < 5; ++i) {
                    result += texture(uTexture, fTexCoord + uDirection * uTexelSize * float(i)).rgb * weight[i];
                    result += texture(uTexture, fTexCoord - uDirection * uTexelSize * float(i)).rgb * weight[i];
                }
                fragColor = vec4(result, 1.0);
            }
        """.trimIndent()
        
        extractProgram = loadShader(quadVS, extractFS)
        blurProgram = loadShader(quadVS, blurFS)
        combineProgram = loadShader(quadVS, combineFS)
    }
    
    private fun loadShader(vs: String, fs: String): Int {
        val vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER).also {
            GLES30.glShaderSource(it, vs)
            GLES30.glCompileShader(it)
        }
        val fragmentShader = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER).also {
            GLES30.glShaderSource(it, fs)
            GLES30.glCompileShader(it)
        }
        return GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertexShader)
            GLES30.glAttachShader(it, fragmentShader)
            GLES30.glLinkProgram(it)
        }
    }
    
    fun resize(w: Int, h: Int) {
        if (width == w && height == h) return
        width = w
        height = h
        cleanupFBOs()
        setupFBOs(w, h)
    }
    
    private fun setupFBOs(w: Int, h: Int) {
        val fbos = IntArray(2)
        val textures = IntArray(2)
        val rbos = IntArray(1)
        
        GLES30.glGenFramebuffers(2, fbos, 0)
        GLES30.glGenTextures(2, textures, 0)
        GLES30.glGenRenderbuffers(1, rbos, 0)
        
        baseFbo = fbos[0]
        baseTexture = textures[0]
        depthBuffer = rbos[0]
        
        blurFbo = fbos[1]
        blurTexture = textures[1]
        
        // Base FBO
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, baseFbo)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, baseTexture)
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, w, h, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, baseTexture, 0)
        
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, depthBuffer)
        GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT16, w, h)
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, depthBuffer)
        
        // Blur FBO
        val blurW = (w * blurScale).toInt()
        val blurH = (h * blurScale).toInt()
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, blurFbo)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, blurTexture)
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, blurW, blurH, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, blurTexture, 0)
        
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }
    
    fun beginCapture() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, baseFbo)
        GLES30.glViewport(0, 0, width, height)
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
    }
    
    fun endCaptureAndRender(bloomIntensity: Float = 1.5f) {
        // 1. Extract Brights to Blur FBO
        val blurW = (width * blurScale).toInt()
        val blurH = (height * blurScale).toInt()
        
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, blurFbo)
        GLES30.glViewport(0, 0, blurW, blurH)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        
        GLES30.glUseProgram(extractProgram)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, baseTexture)
        GLES30.glUniform1i(GLES30.glGetUniformLocation(extractProgram, "uTexture"), 0)
        
        val posHandleEx = GLES30.glGetAttribLocation(extractProgram, "vPosition")
        val texHandleEx = GLES30.glGetAttribLocation(extractProgram, "vTexCoord")
        quad.draw(posHandleEx, texHandleEx)
        
        // Skip actual 2-pass blur to save battery, just rendering scaled down and blending it is a cheap glow
        // To do true blur we need 2 blur FBOs to ping pong, but since we scaled down to 1/4, 
        // the linear interpolation on upscale gives a soft glow.
        
        // 2. Render to Screen
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glViewport(0, 0, width, height)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        
        GLES30.glUseProgram(combineProgram)
        
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, baseTexture)
        GLES30.glUniform1i(GLES30.glGetUniformLocation(combineProgram, "uBaseTexture"), 0)
        
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, blurTexture)
        GLES30.glUniform1i(GLES30.glGetUniformLocation(combineProgram, "uBlurTexture"), 1)
        
        GLES30.glUniform1f(GLES30.glGetUniformLocation(combineProgram, "uBloomIntensity"), bloomIntensity)
        
        val posHandleComb = GLES30.glGetAttribLocation(combineProgram, "vPosition")
        val texHandleComb = GLES30.glGetAttribLocation(combineProgram, "vTexCoord")
        quad.draw(posHandleComb, texHandleComb)
    }
    
    private fun cleanupFBOs() {
        if (baseFbo != 0) {
            GLES30.glDeleteFramebuffers(2, intArrayOf(baseFbo, blurFbo), 0)
            GLES30.glDeleteTextures(2, intArrayOf(baseTexture, blurTexture), 0)
            GLES30.glDeleteRenderbuffers(1, intArrayOf(depthBuffer), 0)
            baseFbo = 0
            blurFbo = 0
        }
    }
}
