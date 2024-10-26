package com.example.pushoflife.sensor

class FallSensor {
    fun checkFall(totalAccelation: Float): Boolean {

        return totalAccelation > 25
    }

    fun calculateAcceleration(linearAcceleration: FloatArray, gravity: FloatArray): Float {
        val dotProduct = linearAcceleration[0] * gravity[0] +
                linearAcceleration[1] * gravity[1] +
                linearAcceleration[2] * gravity[2]
        // 중력 벡터의 크기
        val gravityMagnitude = Math.sqrt((gravity[0] * gravity[0] + gravity[1] * gravity[1] + gravity[2] * gravity[2]).toDouble()).toFloat()

        // 중력 방향으로 가해지는 가속도 성분
        return dotProduct / gravityMagnitude
    }

    fun checkHeart(heartRate: Float): Boolean{
        return heartRate < 30
    }
}