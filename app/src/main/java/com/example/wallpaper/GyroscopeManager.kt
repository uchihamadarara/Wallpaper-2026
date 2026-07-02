package com.example.wallpaper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class GyroscopeManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Filtered target values relative to base
    private var targetPitch = 0f
    private var targetRoll = 0f

    // Base orientation for slow recentering
    private var basePitch = Float.NaN
    private var baseRoll = Float.NaN
    private val recenterAlpha = 0.02f

    // Current physics state
    var currentPitch = 0f
        private set
    var currentRoll = 0f
        private set

    // Velocity for spring physics
    private var velocityPitch = 0f
    private var velocityRoll = 0f

    // Spring constants (Mass-Spring-Damper)
    private val stiffness = 80f
    private val damping = 12f
    private val mass = 1f

    // Low-pass filter for raw input
    private val alpha = 0.3f

    // Time tracking
    private var lastTime = 0L

    fun register() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        lastTime = System.nanoTime()
    }

    fun unregister() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var rawPitch = 0f
        var rawRoll = 0f

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)

            rawPitch = orientation[1]
            rawRoll = orientation[2]
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val norm = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            if (norm > 0) {
                rawPitch = Math.asin((-y / norm).toDouble()).toFloat()
                rawRoll = Math.asin((x / norm).toDouble()).toFloat()
            }
        }

        if (basePitch.isNaN()) {
            basePitch = rawPitch
            baseRoll = rawRoll
        }

        // Slowly drift base to raw to naturally recenter the wallpaper over time
        basePitch += recenterAlpha * (rawPitch - basePitch)
        baseRoll += recenterAlpha * (rawRoll - baseRoll)

        val relPitch = rawPitch - basePitch
        val relRoll = rawRoll - baseRoll

        // Low-pass filter the relative values to reduce jitter
        targetPitch += alpha * (relPitch - targetPitch)
        targetRoll += alpha * (relRoll - targetRoll)
        
        // Constrain extreme values
        val maxAngle = 0.5f
        targetPitch = targetPitch.coerceIn(-maxAngle, maxAngle)
        targetRoll = targetRoll.coerceIn(-maxAngle, maxAngle)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun update() {
        val currentTime = System.nanoTime()
        val dt = if (lastTime > 0) (currentTime - lastTime) / 1e9f else 0.016f
        lastTime = currentTime

        // Clamp dt to avoid huge physics explosion if rendering was paused
        val clampedDt = dt.coerceAtMost(0.05f)

        // Spring physics: F = -k * x - c * v
        val forcePitch = -stiffness * (currentPitch - targetPitch) - damping * velocityPitch
        val accelerationPitch = forcePitch / mass
        velocityPitch += accelerationPitch * clampedDt
        currentPitch += velocityPitch * clampedDt

        val forceRoll = -stiffness * (currentRoll - targetRoll) - damping * velocityRoll
        val accelerationRoll = forceRoll / mass
        velocityRoll += accelerationRoll * clampedDt
        currentRoll += velocityRoll * clampedDt
    }
}
