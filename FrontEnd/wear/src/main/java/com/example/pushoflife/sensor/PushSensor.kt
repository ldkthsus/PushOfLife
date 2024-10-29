package com.example.pushoflife.sensor

import android.util.Log
import com.example.pushoflife.sensor.WatchVibrator

class PushSensor {

    private var previousTime: Long = 0
    private var previousVelocity: Float = 0.0f
    private var previousDisplacement: Float = 0.0f // 변위를 저장할 변수
    private var maxAcceleration = Float.MIN_VALUE
    private var minAccelerationList = mutableListOf<Float>() // 최소값들을 저장
    private var cycleStartTime: Long = 0 // 현재 주기의 시작 시간
    private var depthExceeded = false // 현재 주기 내 minValidDepth 초과 여부
    // CPR 압박이 유효한지 판단하는 함수 (5cm 기준, 주기 종료 시 호출)
    fun checkDepthValidityOnCycleEnd(totalAcceleration: Float) {
        // 주기 내 변위가 유효한 압박 깊이를 넘겼는지 확인
        if (isEndOfCycle(totalAcceleration)) {
            if (depthExceeded) {
                Log.d("PushSensor", "유효한 압박 깊이입니다: 변위가 기준 깊이를 넘었습니다.")
            } else {
                Log.d("PushSensor", "유효한 압박 깊이를 넘지 못했습니다.")
                WatchVibrator.vibrate() // 유효하지 않은 압박 깊이일 때 진동 발생 // 유효하지 않은 압박 깊이일 때 진동 발생  // 유효한 압박 깊이를 넘지 못했을 때 진동 콜백 호출
            }
            // 주기 종료 후 상태 초기화
            depthExceeded = false
            resetVelocityForNewCycle()
        } else {
            // 주기 내 변위 계산
            val displacement = integrateAcceleration(totalAcceleration)
            if (displacement >= 5.0f) { // 변위가 minValidDepth 이상일 때
                depthExceeded = true
            }
        }
    }

    // 가속도를 적분하여 변위를 계산하는 함수
    private fun integrateAcceleration(totalAcceleration: Float): Float {
        val currentTime = System.currentTimeMillis()
        if (previousTime == 0L) {
            previousTime = currentTime
            cycleStartTime = currentTime // 첫 번째 주기 시작 시간 설정
            return 0.0f
        }

        // 시간 간격 계산 (초 단위)
        val deltaTime = (currentTime - previousTime) / 1000.0f
        previousTime = currentTime

        // 최대값 및 주기 설정
        if (totalAcceleration > maxAcceleration) {
            maxAcceleration = totalAcceleration
            resetVelocityForNewCycle() // 새 주기가 시작되면 속도 초기화
        }

        // 최소값 찾기
        if (totalAcceleration < maxAcceleration) {
            minAccelerationList.add(totalAcceleration)
        }

        // 두 번째로 작은 최소값 기준으로 속도 초기화
        if (shouldResetVelocityAtMin()) {
            previousVelocity = 0.0f
            minAccelerationList.clear()
        }

        // 속도를 적분하여 변위를 계산
        val velocity = previousVelocity + totalAcceleration * deltaTime
        val displacement = previousVelocity * deltaTime + 0.5f * totalAcceleration * deltaTime * deltaTime
        previousVelocity = velocity

        return Math.abs(displacement)
    }

    // 새 주기를 시작할 때 속도와 변위를 초기화
    private fun resetVelocityForNewCycle() {
        previousVelocity = 0.0f
        previousDisplacement = 0.0f // 변위 초기화
        maxAcceleration = Float.MIN_VALUE
        minAccelerationList.clear()
    }

    // 주기 내에서 두 번째로 작은 최소값을 기준으로 속도 초기화할지 확인
    private fun shouldResetVelocityAtMin(): Boolean {
        if (minAccelerationList.size >= 2) {
            minAccelerationList.sort()
            return true
        }
        return false
    }

    // 새로운 최대값이 나왔을 때 주기 종료로 판단
    private fun isEndOfCycle(totalAcceleration: Float): Boolean {
        return totalAcceleration > maxAcceleration
    }
}
