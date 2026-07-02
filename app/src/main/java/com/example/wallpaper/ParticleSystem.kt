package com.example.wallpaper

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.random.Random

class ParticleSystem(private val maxParticles: Int) {
    private val vertexBuffer: FloatBuffer
    private val colorBuffer: FloatBuffer
    private val sizeLifeBuffer: FloatBuffer // x = size, y = life
    
    private val positions = FloatArray(maxParticles * 3)
    private val velocities = FloatArray(maxParticles * 3)
    private val colors = FloatArray(maxParticles * 4)
    private val sizeLife = FloatArray(maxParticles * 2)

    init {
        for (i in 0 until maxParticles) {
            resetParticle(i)
            // Stagger initial life so they don't all die at once
            sizeLife[i * 2 + 1] = Random.nextFloat()
        }

        vertexBuffer = ByteBuffer.allocateDirect(positions.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer()
        }

        colorBuffer = ByteBuffer.allocateDirect(colors.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer()
        }
        
        sizeLifeBuffer = ByteBuffer.allocateDirect(sizeLife.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer()
        }
    }

    private fun resetParticle(i: Int) {
        positions[i * 3] = Random.nextFloat() * 4f - 2f // x: -2 to 2
        positions[i * 3 + 1] = Random.nextFloat() * 4f - 2f // y: -2 to 2
        positions[i * 3 + 2] = Random.nextFloat() * 2f - 1f // z: -1 to 1 (depth)

        velocities[i * 3] = (Random.nextFloat() - 0.5f) * 0.05f
        velocities[i * 3 + 1] = Random.nextFloat() * 0.1f + 0.05f // Drift up
        velocities[i * 3 + 2] = (Random.nextFloat() - 0.5f) * 0.02f

        colors[i * 4] = Random.nextFloat() * 0.5f + 0.5f // R
        colors[i * 4 + 1] = Random.nextFloat() * 0.5f + 0.5f // G
        colors[i * 4 + 2] = 1.0f // B
        colors[i * 4 + 3] = Random.nextFloat() * 0.5f + 0.2f // Alpha

        sizeLife[i * 2] = Random.nextFloat() * 30f + 10f // Size
        sizeLife[i * 2 + 1] = 1.0f // Life (0.0 to 1.0)
    }

    fun triggerBurst() {
        for (i in 0 until maxParticles) {
            positions[i * 3] = Random.nextFloat() * 1f - 0.5f
            positions[i * 3 + 1] = Random.nextFloat() * 1f - 0.5f
            positions[i * 3 + 2] = Random.nextFloat() * 2f - 1f

            val speed = Random.nextFloat() * 0.5f + 0.2f
            val angle = Random.nextFloat() * Math.PI * 2
            velocities[i * 3] = (Math.cos(angle) * speed).toFloat()
            velocities[i * 3 + 1] = (Math.sin(angle) * speed).toFloat()
            velocities[i * 3 + 2] = (Random.nextFloat() - 0.5f) * speed

            sizeLife[i * 2 + 1] = 1.0f // reset life
        }
    }

    fun triggerLock() {
        for (i in 0 until maxParticles) {
            sizeLife[i * 2 + 1] = 0.0f // kill all particles
        }
    }

    fun update(dt: Float, isCharging: Boolean) {
        for (i in 0 until maxParticles) {
            // Update life
            sizeLife[i * 2 + 1] -= dt * 0.2f // Fade out speed
            
            if (sizeLife[i * 2 + 1] <= 0f) {
                resetParticle(i)
            }

            // Update position
            positions[i * 3] += velocities[i * 3] * dt
            positions[i * 3 + 1] += velocities[i * 3 + 1] * dt
            if (isCharging) {
                positions[i * 3 + 1] += velocities[i * 3 + 1] * dt * 2.0f // Faster going up when charging
            }
            positions[i * 3 + 2] += velocities[i * 3 + 2] * dt

            // Wrap around Y
            if (positions[i * 3 + 1] > 2.0f) {
                positions[i * 3 + 1] = -2.0f
            }
            
            // Adjust color for charging
            if (isCharging) {
                colors[i * 4] = 0.2f
                colors[i * 4 + 1] = 1.0f
                colors[i * 4 + 2] = 0.3f
            }
        }

        vertexBuffer.put(positions).position(0)
        colorBuffer.put(colors).position(0)
        sizeLifeBuffer.put(sizeLife).position(0)
    }

    fun draw(positionHandle: Int, colorHandle: Int, sizeLifeHandle: Int) {
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)

        GLES30.glEnableVertexAttribArray(colorHandle)
        GLES30.glVertexAttribPointer(colorHandle, 4, GLES30.GL_FLOAT, false, 0, colorBuffer)

        GLES30.glEnableVertexAttribArray(sizeLifeHandle)
        GLES30.glVertexAttribPointer(sizeLifeHandle, 2, GLES30.GL_FLOAT, false, 0, sizeLifeBuffer)

        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, maxParticles)

        GLES30.glDisableVertexAttribArray(positionHandle)
        GLES30.glDisableVertexAttribArray(colorHandle)
        GLES30.glDisableVertexAttribArray(sizeLifeHandle)
    }
}
