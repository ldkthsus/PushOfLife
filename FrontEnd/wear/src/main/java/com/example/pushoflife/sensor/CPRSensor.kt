package com.example.pushoflife.sensor

import android.util.Log
import kotlin.math.abs
import kotlin.math.sqrt

class CPRSensor {
    private var lastTime: Long = 0
    private var velocity = 0.0f
    private var displacement = 0.0f
    private var lastFusedAcc: Float = 0.0f

    // 최적화를 위한 조정된 칼만 필터 파라미터
    private var kalmanState = floatArrayOf(0.0f, 0.0f)
    private var kalmanP = arrayOf(floatArrayOf(1.0f, 0.0f), floatArrayOf(0.0f, 1.0f))
    private val Q = arrayOf(floatArrayOf(0.012f, 0.0f), floatArrayOf(0.0f, 0.012f))// Process noise
    private val R = arrayOf(floatArrayOf(0.003f, 0.0f), floatArrayOf(0.0f, 0.003f))// Measurement noise
    private val H = arrayOf(floatArrayOf(1.0f, 0.0f), floatArrayOf(0.0f, 1.0f))

    // 필터 및 임계값 조정
    private val alphaComplementary = 0.75f
    private val maxVelocity = 40f
    private val maxDisplacement = 4f
    private val maxAcceleration = 8.0f // 실측 환경에 맞춘 임계값

    fun feedbackCPR(accelerometer: FloatArray, gravity: FloatArray?): Float {
        if (gravity == null) {
            Log.d("CPRSensorLog", "중력 센서 값이 필요합니다.")
            return 0.0f
        }

        val currentTime = System.currentTimeMillis()
        if (lastTime == 0L) {
            lastTime = currentTime
            return 0.0f
        }

        var dt = (currentTime - lastTime).toFloat() / 1000.0f
        lastTime = currentTime
        if (dt <= 0.0001f) dt = 0.01f  // dt 안정화

        // 중력 보정 후 가속도 계산
        val gravityAcc = calculateGravityCompensatedAcc(accelerometer, gravity)
        var linearAcc = highPassFilter(gravityAcc, alphaComplementary)

        // 비정상적인 가속도 값 제한
        if (linearAcc > maxAcceleration) linearAcc = maxAcceleration
        else if (linearAcc < -maxAcceleration) linearAcc = -maxAcceleration

        // 로그: 가속도 정보
        Log.d("CPRSensorLog", "보정 후 가속도 (linearAcc): $linearAcc")

        // 칼만 필터 적용
        kalmanFilter(linearAcc, dt)

        // 변위 조정 후 반환
        displacement = kalmanState[0].coerceIn(-maxDisplacement, maxDisplacement)
        Log.d("CPRSensorLog", "최종 변위 (displacement): $displacement")

        return displacement
    }

    private fun highPassFilter(input: Float, alpha: Float): Float {
        lastFusedAcc = alpha * (lastFusedAcc + input - lastFusedAcc)
        Log.d("CPRSensorLog", "고역 필터 적용 후 가속도 (lastFusedAcc): $lastFusedAcc")
        return lastFusedAcc
    }

    private fun calculateGravityCompensatedAcc(accelerometer: FloatArray, gravity: FloatArray): Float {
        val dotProduct = accelerometer[0] * gravity[0] + accelerometer[1] * gravity[1] + accelerometer[2] * gravity[2]
        val gravityMagnitude = sqrt(gravity[0] * gravity[0] + gravity[1] * gravity[1] + gravity[2] * gravity[2])
        val gravityCompensatedAcc = dotProduct / gravityMagnitude
        Log.d("CPRSensorLog", "중력 보정 계산된 가속도 (gravityCompensatedAcc): $gravityCompensatedAcc")
        return gravityCompensatedAcc
    }

    private fun kalmanFilter(input: Float, dt: Float) {
        if (dt <= 0.0001f) return  // 매우 작은 dt 값일 때 처리하지 않음

        val F = arrayOf(floatArrayOf(1.0f, dt), floatArrayOf(0.0f, 1.0f))
        val predictedState = floatArrayOf(
            F[0][0] * kalmanState[0] + F[0][1] * kalmanState[1],
            F[1][0] * kalmanState[0] + F[1][1] * kalmanState[1]
        )

        val predictedP = arrayOf(
            floatArrayOf(F[0][0] * kalmanP[0][0] + F[0][1] * kalmanP[1][0] + Q[0][0], F[0][0] * kalmanP[0][1] + F[0][1] * kalmanP[1][1] + Q[0][1]),
            floatArrayOf(F[1][0] * kalmanP[0][0] + F[1][1] * kalmanP[1][0] + Q[1][0], F[1][0] * kalmanP[0][1] + F[1][1] * kalmanP[1][1] + Q[1][1])
        )

        val S = arrayOf(
            floatArrayOf(predictedP[0][0] + R[0][0], predictedP[0][1] + R[0][1]),
            floatArrayOf(predictedP[1][0] + R[1][0], predictedP[1][1] + R[1][1])
        )

        // NaN 방지 - S 행렬 요소가 0이거나 NaN일 경우 초기화
        if (S[0][0].isNaN() || S[1][1].isNaN() || S[0][0] == 0f || S[1][1] == 0f) {
            kalmanP = arrayOf(floatArrayOf(1.0f, 0.0f), floatArrayOf(0.0f, 1.0f))
            kalmanState = floatArrayOf(0.0f, 0.0f)
            return
        }

        val K = arrayOf(
            floatArrayOf(predictedP[0][0] / S[0][0], predictedP[0][1] / S[1][1]),
            floatArrayOf(predictedP[1][0] / S[0][0], predictedP[1][1] / S[1][1])
        )

        val measurement = floatArrayOf(input * dt, input)
        val y = floatArrayOf(
            measurement[0] - predictedState[0],
            measurement[1] - predictedState[1]
        )

        kalmanState[0] = predictedState[0] + K[0][0] * y[0] + K[0][1] * y[1]
        kalmanState[1] = predictedState[1] + K[1][0] * y[0] + K[1][1] * y[1]

        // 공분산 업데이트
        kalmanP[0][0] -= K[0][0] * predictedP[0][0]
        kalmanP[0][1] -= K[0][1] * predictedP[0][1]
        kalmanP[1][0] -= K[1][0] * predictedP[1][0]
        kalmanP[1][1] -= K[1][1] * predictedP[1][1]

        // 업데이트 후 칼만 상태와 공분산 행렬을 로그로 출력
        Log.d("CPRSensorLog", "업데이트 후 칼만 상태 (kalmanState): ${kalmanState.contentToString()}")
        Log.d("CPRSensorLog", "업데이트 후 공분산 행렬 (kalmanP): ${kalmanP.contentDeepToString()}")
    }

}
