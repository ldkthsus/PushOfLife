package com.example.pushoflife.sensor

import android.util.Log
import kotlin.math.abs

class PushSensor {

    // CPR 압박 시작/끝 기준값 설정
    private val noiseThreshold = 0.52f  // 미세한 진동을 필터링하는 노이즈 임계값
    private val startThreshold = 8.5f  // 압박 시작 판별 임계값
    private val endThreshold = -4.0f  // 압박 종료 판별 임계값
    private val depthRange = 0.05f  // 적절한 압박 깊이 (5cm 기준)
    private val maxPressDuration = 300L  // 압박 최대 지속 시간 (0.4초)

    private var isPressing = false

    // 이동 거리 계산 변수
    private var previousVelocity = 0.0
    private var previousDistance = 0.0
    private var lastUpdateTime: Long = System.currentTimeMillis()

    // 저항 계수 변수
    private var resistanceFactor = 1.1f  // 저항 계수 기본값 설정

    // 압박 시작과 종료 데이터를 저장할 변수
    private var startAcceleration = 0.0f
    private var startTime: Long = 0
    private var endAcceleration = 0.0f
    private var endTime: Long = 0
    private var previousTime: Long = 0
    private var isIncreasing = false
    private var cycleCount = 0
    private var lastPeakTime: Long = 0L
    // CPR 피드백 계산 함수
    fun calculateCPRFeedback(acceleration: Float) {
        val correctedAcceleration = if (abs(acceleration) < noiseThreshold) 0.0f else acceleration * resistanceFactor

        // 현재 시간 및 경과 시간
        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - lastUpdateTime) / 1000.0
        lastUpdateTime = currentTime

        // 현재 속도 및 거리 계산
        val currentVelocity = previousVelocity + correctedAcceleration * deltaTime
        val distance = previousDistance + currentVelocity * deltaTime

        // 압박 시작과 종료 탐지
        if (correctedAcceleration > startThreshold && !isPressing) {
            // 압박 시작
            isPressing = true
            startAcceleration = correctedAcceleration  // 압박 시작 시 가속도 저장
            startTime = currentTime  // 압박 시작 시 시간 저장
            previousVelocity = 0.0
            previousDistance = 0.0
        } else if (correctedAcceleration < endThreshold && isPressing) {
            // 압박 종료
            completeCompression(correctedAcceleration, currentTime)
        } else if (isPressing && (currentTime - startTime) > maxPressDuration) {
            // 최대 압박 지속 시간 초과 시 강제 종료
            completeCompression(-0.2f, currentTime)  // 종료 가속도를 0으로 설정하여 종료 처리
        }

        // 압박 중 거리 업데이트
        if (isPressing) {
            previousVelocity = currentVelocity
            previousDistance = distance
        }
        trackCprCycle(acceleration, currentTime)
    }

    // 압박 종료 처리 함수
    private fun completeCompression(acceleration: Float, currentTime: Long) {
        isPressing = false
        endAcceleration = acceleration  // 압박 종료 시 가속도 저장
        endTime = currentTime  // 압박 종료 시 시간 저장

        // 깊이 계산 및 출력
        val timeDiff = (endTime - startTime) / 1000.0  // 시간 차이 계산
        val estimatedDepth = (startAcceleration - endAcceleration) * timeDiff * resistanceFactor
        Log.d("CPR Feedback", "압박 깊이: $estimatedDepth cm")

        // 초기화
        previousVelocity = 0.0
        previousDistance = 0.0
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
                Log.d(
                    "CPR_Cycle",
                    "Cycle $cycleCount detected at ${currentTime - lastPeakTime} ms interval"
                )

                // 3초마다 cycleCount 평가 및 피드백 제공
                if (currentTime - lastPeakTime >= 5000) {
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
            cycleCount > 12 -> "빠름"
            cycleCount in 9..12 -> "정상 속도"
            else -> "느림"
        }
        Log.d("CPR_Feedback", "$cprFrequencyFeedback cpr")
    }
}
