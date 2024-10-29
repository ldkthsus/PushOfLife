package com.example.pushoflife.sensor

import android.util.Log
import com.example.pushoflife.sensor.IntegralAcceleration

class PushSensor {

    private var previousTime: Long = 0
    private var previousVelocity: Float = 0.0f


    // 가속도를 시간에 대해 적분하여 변위를 계산하는 함수 (속도를 적분하고 변위 계산)
    fun integrateAcceleration(totalAcceleration: Float): Float {
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
        val velocity = previousVelocity + totalAcceleration * deltaTime

        // 변위를 계산 (속도를 시간에 대해 적분)
        val displacement = previousVelocity * deltaTime + 0.5f * totalAcceleration * deltaTime * deltaTime

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
