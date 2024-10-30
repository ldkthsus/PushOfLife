package com.example.pushoflife.sensor

import android.util.Log
class WristAngleSensor {
    fun processGyroscopeData(gyroscopeData: FloatArray) {
        val x = gyroscopeData[0]
        val y = gyroscopeData[1]
        val z = gyroscopeData[2]
        Log.d("WristAngleSensor", "Gyroscope data - x=$x, y=$y, z=$z")
    }

    fun processGravityData(gravityValues: FloatArray) {
        val x = gravityValues[0]
        val y = gravityValues[1]
        val z = gravityValues[2]
        Log.d("WristAngleGravitySensor", "Process Gravity data - x=$x, y=$y, z=$z")
    }
}