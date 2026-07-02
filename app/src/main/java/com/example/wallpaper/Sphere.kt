package com.example.wallpaper

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin

class Sphere(private val radius: Float, private val rings: Int, private val sectors: Int) {
    private val vertexBuffer: FloatBuffer
    private val normalBuffer: FloatBuffer
    private val texCoordBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val numIndices: Int

    init {
        val vertices = FloatArray((rings + 1) * (sectors + 1) * 3)
        val normals = FloatArray((rings + 1) * (sectors + 1) * 3)
        val texCoords = FloatArray((rings + 1) * (sectors + 1) * 2)
        val indices = ShortArray(rings * sectors * 6)

        var vIndex = 0
        var nIndex = 0
        var tIndex = 0
        var iIndex = 0

        for (r in 0..rings) {
            val v = r.toFloat() / rings
            val phi = v * Math.PI

            for (s in 0..sectors) {
                val u = s.toFloat() / sectors
                val theta = u * 2 * Math.PI

                val x = cos(theta) * sin(phi)
                val y = cos(phi)
                val z = sin(theta) * sin(phi)

                vertices[vIndex++] = (x * radius).toFloat()
                vertices[vIndex++] = (y * radius).toFloat()
                vertices[vIndex++] = (z * radius).toFloat()

                normals[nIndex++] = x.toFloat()
                normals[nIndex++] = y.toFloat()
                normals[nIndex++] = z.toFloat()

                texCoords[tIndex++] = u
                texCoords[tIndex++] = 1f - v // Invert V for standard texture mapping
            }
        }

        for (r in 0 until rings) {
            for (s in 0 until sectors) {
                val first = (r * (sectors + 1) + s).toShort()
                val second = (first + sectors + 1).toShort()

                indices[iIndex++] = first
                indices[iIndex++] = second
                indices[iIndex++] = (first + 1).toShort()

                indices[iIndex++] = second
                indices[iIndex++] = (second + 1).toShort()
                indices[iIndex++] = (first + 1).toShort()
            }
        }

        numIndices = indices.size

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }

        normalBuffer = ByteBuffer.allocateDirect(normals.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(normals)
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

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(indices)
                position(0)
            }
        }
    }

    fun draw(positionHandle: Int, normalHandle: Int, texCoordHandle: Int) {
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)

        if (normalHandle >= 0) {
            GLES30.glEnableVertexAttribArray(normalHandle)
            GLES30.glVertexAttribPointer(normalHandle, 3, GLES30.GL_FLOAT, false, 0, normalBuffer)
        }

        if (texCoordHandle >= 0) {
            GLES30.glEnableVertexAttribArray(texCoordHandle)
            GLES30.glVertexAttribPointer(texCoordHandle, 2, GLES30.GL_FLOAT, false, 0, texCoordBuffer)
        }

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, numIndices, GLES30.GL_UNSIGNED_SHORT, indexBuffer)

        GLES30.glDisableVertexAttribArray(positionHandle)
        if (normalHandle >= 0) GLES30.glDisableVertexAttribArray(normalHandle)
        if (texCoordHandle >= 0) GLES30.glDisableVertexAttribArray(texCoordHandle)
    }
}
