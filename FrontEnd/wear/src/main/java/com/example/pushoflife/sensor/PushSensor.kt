package com.example.pushoflife.sensor

import android.util.Log

class PushSensor {

    // CPR 압박 시작/끝 기준값 설정
    private val startThreshold = 10.0 // 압박 시작을 판별하는 임계값
    private val endThreshold = 2.0 // 압박 끝을 판별하는 임계값
    private val depthRange = 5.0 // 적절한 압박 깊이 (5cm 기준)

    private var isPressing = false
    private var pressDepth = 0.0

    // 이동 거리 계산 변수
    private var previousTime: Long = 0
    private var previousVelocity = 0.0
    private var previousDistance = 0.0
    private var lastUpdateTime: Long = System.currentTimeMillis()

    // 빈도 측정을 위한 변수
    private var isIncreasing = false // 가속도 증감 여부 감지
    private var cycleCount = 0
    private var lastPeakTime: Long = 0L

    // CPR 피드백을 위한 함수
    fun calculateCPRFeedback(acceleration: Float) {
        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - lastUpdateTime) / 1000.0
        lastUpdateTime = currentTime

        val currentVelocity = previousVelocity + acceleration.toDouble() * deltaTime
        val distance = previousDistance + currentVelocity * deltaTime

        if (acceleration > startThreshold && !isPressing) {
            // 압박 시작
            isPressing = true
            pressDepth = acceleration.toDouble()
            previousVelocity = 0.0
            previousDistance = 0.0
        } else if (acceleration < endThreshold && isPressing) {
            // 압박 끝, 깊이 계산
            val depthDifference = Math.abs(pressDepth - acceleration.toDouble())
            isPressing = false
            when {
                depthDifference >= depthRange && distance >= depthRange ->
                    Log.d("깊이 측정","적절한 깊이와 이동 거리로 CPR이 수행되고 있습니다.")
                depthDifference < depthRange ->
                    Log.d("깊이 측정","압박 깊이가 충분하지 않습니다.")
                else ->
                    Log.d("깊이 측정","압박 깊이가 충분하지 않습니다.")
            }
        }

        previousVelocity = currentVelocity
        previousDistance = distance

        // 빈도 계산을 위한 함수 호출
        trackCprCycle(acceleration, currentTime)

        //return //"측정 중..."
    }

    // CPR 압박 빈도 측정 함수
    private fun trackCprCycle(totalAcceleration: Float, currentTime: Long) {
        if (totalAcceleration > previousVelocity) {
            if (!isIncreasing) {
                isIncreasing = true
                cycleCount++

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
        Log.d("깊이 측정", "$cprFrequencyFeedback cpr")
    }
}
