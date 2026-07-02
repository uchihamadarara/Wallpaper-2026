package com.example.wallpaper

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Quad {
    private val vertexBuffer: FloatBuffer
    private val texCoordBuffer: FloatBuffer

    private val vertices = floatArrayOf(
        -1.0f,  1.0f, 0.0f, // Top left
        -1.0f, -1.0f, 0.0f, // Bottom left
         1.0f, -1.0f, 0.0f, // Bottom right
        
        -1.0f,  1.0f, 0.0f, // Top left
         1.0f, -1.0f, 0.0f, // Bottom right
         1.0f,  1.0f, 0.0f  // Top right
    )

    private val texCoords = floatArrayOf(
        0.0f, 0.0f, // Top left
        0.0f, 1.0f, // Bottom left
        1.0f, 1.0f, // Bottom right
        
        0.0f, 0.0f, // Top left
        1.0f, 1.0f, // Bottom right
        1.0f, 0.0f  // Top right
    )

    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }

        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(texCoords)
                position(0)
            }
        }
    }

    fun draw(positionHandle: Int, texCoordHandle: Int) {
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)

        GLES30.glEnableVertexAttribArray(texCoordHandle)
        GLES30.glVertexAttribPointer(texCoordHandle, 2, GLES30.GL_FLOAT, false, 0, texCoordBuffer)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6)

        GLES30.glDisableVertexAttribArray(positionHandle)
        GLES30.glDisableVertexAttribArray(texCoordHandle)
    }
}
