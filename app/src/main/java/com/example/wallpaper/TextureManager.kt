package com.example.wallpaper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue

class TextureManager {
    private val client = OkHttpClient()
    
    // Queue of bitmaps waiting to be bound to GL textures (must run on GL thread)
    private val pendingTextures = ConcurrentLinkedQueue<TextureTask>()
    
    // Cache of already loaded texture IDs specific to this context
    private val textureCache = mutableMapOf<String, Int>()

    class TextureTask(val url: String, val bitmap: Bitmap, val callback: (Int) -> Unit)

    fun loadTexture(url: String, callback: (Int) -> Unit) {
        if (textureCache.containsKey(url)) {
            callback(textureCache[url]!!)
            return
        }

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TextureManager", "Failed to download texture: $url", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) return
                
                response.body?.byteStream()?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        pendingTextures.add(TextureTask(url, bitmap, callback))
                    }
                }
            }
        })
    }

    /**
     * MUST be called from the GL rendering thread (e.g., inside onDrawFrame or onSurfaceChanged)
     */
    fun processPendingTextures() {
        while (pendingTextures.isNotEmpty()) {
            val task = pendingTextures.poll() ?: continue
            val textureId = createGLTexture(task.bitmap)
            task.bitmap.recycle() // Free memory
            
            if (textureId != 0) {
                textureCache[task.url] = textureId
                task.callback(textureId)
            }
        }
    }

    private fun createGLTexture(bitmap: Bitmap): Int {
        val textureIds = IntArray(1)
        GLES30.glGenTextures(1, textureIds, 0)
        
        if (textureIds[0] == 0) {
            Log.e("TextureManager", "Could not generate a new OpenGL texture object.")
            return 0
        }
        
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0])
        
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
        
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0) // Unbind
        
        return textureIds[0]
    }
}
