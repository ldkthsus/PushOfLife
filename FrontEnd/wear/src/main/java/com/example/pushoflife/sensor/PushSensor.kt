package com.example.pushoflife.sensor

import android.util.Log
import com.example.pushoflife.sensor.IntegralAcceleration

class PushSensor {

    private var previousTime: Long = 0
    private var previousVelocity: Float = 0.0f

    // 빈도 측정을 위한 변수
    private var isIncreasing = false // 가속도 증감 여부 감지
    private var cycleCount = 0
    private var lastPeakTime: Long = 0L


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
        val displacement =
            previousVelocity * deltaTime + 0.5f * totalAcceleration * deltaTime * deltaTime

        // 속도 갱신
        previousVelocity = velocity

        // 새로 추가함 압박 빈도 측정 로직
        // integrateAcceleration 함수가 주기적으로 가속도 데이터를 받아 주기를 감지하고, 빈도에 대한 피드백을 제공하기 위해 필요
        trackCprCycle(totalAcceleration, currentTime)

        return Math.abs(displacement) // 변위(압박 깊이)를 반환
    }

    // CPR 압박이 유효한지 판단하는 함수 (5cm 기준, 추후 수정 가능)
    private fun isCprValid(pushDepth: Float): Boolean {
        val minValidDepth = 5.0f
        return pushDepth >= minValidDepth
    }

    // CPR 압박 빈도 측정 함수
    // 여기서 CPR 시행 여부를 판단할 수 있음, 추후 자동 가이드 음악 시작 및 종료 조건도 여기서 걸 수 있을 듯
    private fun trackCprCycle(totalAcceleration: Float, currentTime: Long) {
        // 가속도가 증가하는지, 감소하는지에 따라 시작/끝점 감지
        if (totalAcceleration > previousVelocity) {
            if (!isIncreasing) {
                isIncreasing = true
                // 새로운 사이클의 시작점을 감지
                cycleCount++
//                Log.d(
//                    "CPR_Cycle",
//                    "Cycle $cycleCount detected at ${currentTime - lastPeakTime} ms interval"
//                )

                // 3초마다 cycleCount 평가 및 피드백 제공
                if (currentTime - lastPeakTime >= 3000) {
                    frequencyFeedback(cycleCount) // 피드백 제공 함수 호출
                    cycleCount = 0 // 카운트 리셋
                    lastPeakTime = currentTime // 피드백 제공 시간 갱신
                }
            }
        } else {
            isIncreasing = false
        }
    }

    // CPR 빈도에 따라 피드백 제공
    private fun frequencyFeedback(cycleCount: Int) {
        val cprFrequencyFeedback = when {
            cycleCount >= 12 -> "빠름"
            cycleCount in 10..11 -> "정상 속도"
            else -> "느림"
        }
//        Log.d("CPR_Feedback", "$cprFrequencyFeedback cpr")
    }
}