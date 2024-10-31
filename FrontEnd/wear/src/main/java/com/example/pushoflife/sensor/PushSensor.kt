package com.example.pushoflife.sensor

import android.util.Log
import kotlin.math.abs

class PushSensor {

    // CPR 압박 시작/끝 기준값 설정
    private val noiseThreshold = 0.52f  // 미세한 진동을 필터링하는 노이즈 임계값
    private val startThreshold = 7f  // 압박 시작 판별 임계값
    private val endThreshold = -6f  // 압박 종료 판별 임계값
    private val depthRange = 0.05f  // 적절한 압박 깊이 (5cm 기준)

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
            isPressing = false
            endAcceleration = correctedAcceleration  // 압박 종료 시 가속도 저장
            endTime = currentTime  // 압박 종료 시 시간 저장

            // 깊이 계산 및 출력
            val timeDiff = (endTime - startTime) / 1000.0  // 시간 차이 계산
            val estimatedDepth = (startAcceleration - endAcceleration) * timeDiff * resistanceFactor
            Log.d("CPR Feedback", "압박 깊이: $estimatedDepth cm")

            // 초기화
            previousVelocity = 0.0
            previousDistance = 0.0
        }

        // 압박 중 거리 업데이트
        if (isPressing) {
            previousVelocity = currentVelocity
            previousDistance = distance
        }
    }
}
