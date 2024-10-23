package com.example.pushoflife.sensor

class FallSensor {
    fun checkFall(accelerationX: Float, accelerationY: Float, accelerationZ: Float): Boolean {

        val magnitude = Math.sqrt(
            (accelerationX * accelerationX + accelerationY * accelerationY + accelerationZ * accelerationZ).toDouble()
        )

        return magnitude > 25
    }

    fun checkHeart(heartRate: Float): Boolean{
        return heartRate < 30
    }
}