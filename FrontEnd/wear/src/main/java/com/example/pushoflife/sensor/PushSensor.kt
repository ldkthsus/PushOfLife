package com.example.pushoflife.sensor

import android.util.Log
import com.example.pushoflife.sensor.IntegralAcceleration

class PushSensor {

    private var previousTime: Long = 0
    private var previousVelocity: Float = 0.0f

    // CPR 압박 깊이를 계산하는 함수
    fun processlinearAccelerationSensorData(linearAccelerationSensorData: FloatArray) {
        // 가속도계 벡터 (x, y, z)
        val accelerationVector = floatArrayOf(linearAccelerationSensorData[0], linearAccelerationSensorData[1], linearAccelerationSensorData[2])

        // 중력 벡터 (0, 0, -1)로 가정
        val gravityVector = floatArrayOf(0.0f, 0.0f, -9.81f)

        // 내적을 이용해 압박 깊이 계산을 위한 가속도 성분 계산
        val a_n = calculateNormalizedAcceleration(accelerationVector, gravityVector)

        // 가속도 성분을 적분하여 변위를 계산
        val pushDepth = integrateAcceleration(a_n)

        //Log.d("PushSensor", "CPR 압박 깊이(내적 및 적분 계산): $pushDepth")

        if (isCprValid(pushDepth)) {
            //Log.d("PushSensor", "유효한 CPR 압박입니다.")
        } else {
            //Log.d("PushSensor", "CPR 압박이 충분하지 않습니다.")
        }
    }

    // 가속도 벡터와 중력 벡터의 내적을 계산하고, 중력 벡터의 크기로 나누어 정규화된 가속도 성분을 계산하는 함수
    private fun calculateNormalizedAcceleration(accelerationVector: FloatArray, gravityVector: FloatArray): Float {
        // 두 벡터의 내적 계산
        val dotProduct = dot(accelerationVector, gravityVector)
        // 중력 벡터의 크기를 계산
        val gravityMagnitude = magnitude(gravityVector)
        // 내적을 중력 크기로 나눠서 정규화된 가속도 성분을 반환
        return dotProduct / gravityMagnitude
    }

    // 두 벡터의 내적을 계산하는 함수
    private fun dot(vectorA: FloatArray, vectorB: FloatArray): Float {
        return vectorA[0] * vectorB[0] + vectorA[1] * vectorB[1] + vectorA[2] * vectorB[2]
    }

    // 벡터의 크기를 계산하는 함수 (중력 벡터의 크기 계산에 사용)
    private fun magnitude(vector: FloatArray): Float {
        return Math.sqrt((vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]).toDouble()).toFloat()
    }

    // 가속도를 시간에 대해 적분하여 변위를 계산하는 함수 (속도를 적분하고 변위 계산)
    private fun integrateAcceleration(acceleration: Float): Float {
        // 현재 시간
        val currentTime = System.currentTimeMillis()
        if (previousTime == 0L) {
            previousTime = currentTime
            return 0.0f // 첫 번째 데이터에서는 변위가 없다고 가정
        }

        // 시간 간격 계산 (초 단위로 변환)
        val deltaTime = (currentTime - previousTime) / 1000.0f
        previousTime = currentTime

        // 속도를 적분 (가속도를 시간에 대해 적분)
        val velocity = previousVelocity + acceleration * deltaTime

        // 변위를 계산 (속도를 시간에 대해 적분)
        val displacement = previousVelocity * deltaTime + 0.5f * acceleration * deltaTime * deltaTime

        // 속도 갱신
        previousVelocity = velocity

        return Math.abs(displacement) // 변위(압박 깊이)를 반환
    }

    // CPR 압박이 유효한지 판단하는 함수 (5cm 기준, 추후 수정 가능)
    private fun isCprValid(pushDepth: Float): Boolean {
        val minValidDepth = 5.0f
        return pushDepth >= minValidDepth
    }
}
