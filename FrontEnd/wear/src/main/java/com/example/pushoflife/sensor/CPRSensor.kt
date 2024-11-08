package com.example.pushoflife.sensor

import com.example.pushoflife.sensor.utils.GravitySensor
import com.example.pushoflife.sensor.utils.LinearAccelerationSensor
import kotlin.math.sqrt
import kotlin.math.sign
import kotlin.math.max
import kotlin.math.min
import android.content.Context
import android.location.GnssAntennaInfo.Listener
import com.example.pushoflife.CprGuideActivity
import android.util.Log
import java.util.Arrays

class CPRSensor(
    private val activity: CprGuideActivity,
    private val sampleRate: Float = 100f,
    private val movingAvgWindowSize: Int = 5
): GravitySensor.GravitySensorListener, LinearAccelerationSensor.LinearAccelerationListener {
    private var lastTimestamp: Long? = null  // 초기에는 null로 설정
    private var lastDepth: Float = 0f
    private var filteredAcc: Float = 0f
    private var gravityCalibration: Float = 0f
    private var isCalibrated = false
    private val alpha = 0.6f  // Low-pass filter coefficient
    private val maxDisplacement = 6f  // 최대 깊이 6cm로 제한
    private val minDisplacement = 0f
    private var lastDisplacement = 0f
    private val frequencyTimes = mutableListOf<Float>()
    private val depthList = mutableListOf<Float>()
    // 이동 평균 필터용 데이터 버퍼
    private val depthBuffer = ArrayDeque<Float>(movingAvgWindowSize)
    private var lastTime: Long? = null
    private var pussing = false
    private var depthPussing = false
    private var maxDepth = 0.0f
    // 칼만 필터 관련 변수들
    private var state = floatArrayOf(0f, 0f)  // 초기 상태 [displacement, velocity]
    private var P = arrayOf(floatArrayOf(1f, 0f), floatArrayOf(0f, 1f))  // 공분산 행렬
    private val Q = arrayOf(floatArrayOf(0.15f, 0f), floatArrayOf(0f, 0.15f))  // 프로세스 노이즈
    private val R = arrayOf(floatArrayOf(0.05f, 0f), floatArrayOf(0f, 0.05f))  // 측정 노이즈
    private var gravityList = floatArrayOf(0.0f, 0.0f, 0.0f)
    private var accelerometerList = floatArrayOf(0.0f, 0.0f, 0.0f)
    private var gravityValues = floatArrayOf(0.0f, 0.0f, 0.0f)
    private var accelerationValues = floatArrayOf(0.0f, 0.0f, 0.0f)


    fun initialize(context: Context) {
        GravitySensor.initialize(context)  // GravitySensor 초기화 (중복 초기화 방지)
        GravitySensor.addListener(this)    // 리스너로 현재 CPRSensor를 추가
        LinearAccelerationSensor.initialize(context, this)
    }
    override fun onGravityDataChanged(gravityValues: FloatArray) {
        this.gravityValues = gravityValues
        Log.d("gravity", "중력 ${Arrays.toString(this.gravityValues)}, ${Arrays.toString(gravityValues)}")
        if (accelerationValues != floatArrayOf(0.0f, 0.0f, 0.0f)) {
            processSensorData()
        }
    }

    override fun onLinearAccelerationDataChanged(accelerationValues: FloatArray) {
        this.accelerationValues = accelerationValues
        if (gravityValues != floatArrayOf(0.0f, 0.0f, 0.0f)) {
            processSensorData()
        }
    }

    private fun processSensorData() {
        gravityList = gravityValues
        accelerometerList = accelerationValues
        Log.d("왜 난?","중력 : ${Arrays.toString(gravityList)}, 가속도: ${Arrays.toString(accelerometerList)}")

        feedbackCPR(accelerometerList, gravityList)

    }



    // 중력 보정된 가속도 계산
    private fun calculateGravityCompensatedAcc(
        accelerometer: FloatArray,
        gravity: FloatArray
    ): Float {
        val dotProduct = accelerometer[0] * gravity[0] + accelerometer[1] * gravity[1] + accelerometer[2] * gravity[2]
        val gravityMagnitude = sqrt(gravity[0] * gravity[0] + gravity[1] * gravity[1] + gravity[2] * gravity[2])
        return dotProduct / gravityMagnitude
    }

    // 저주파 필터 적용
    private fun lowPassFilter(rawAcc: Float): Float {
        filteredAcc = alpha * rawAcc + (1 - alpha) * filteredAcc
        return filteredAcc
    }

    // 이동 평균 필터 적용
    private fun applyMovingAverageFilter(depth: Float): Float {
        // depthBuffer에 값 추가
        if (depthBuffer.size == movingAvgWindowSize) {
            depthBuffer.removeFirst()
        }
        depthBuffer.addLast(depth)

        // 평균 계산
        return depthBuffer.average().toFloat()
    }

    // 칼만 필터 적용
    private fun kalmanFilter(measurement: FloatArray): Float {
        val dt = 1f / sampleRate
        val F = arrayOf(floatArrayOf(1f, dt), floatArrayOf(0f, 1f))

        // 예측 단계
        val predictedState = floatArrayOf(
            F[0][0] * state[0] + F[0][1] * state[1],
            F[1][0] * state[0] + F[1][1] * state[1]
        )

        val predictedP = arrayOf(
            floatArrayOf(F[0][0] * P[0][0] + F[0][1] * P[1][0] + Q[0][0], F[0][0] * P[0][1] + F[0][1] * P[1][1]),
            floatArrayOf(F[1][0] * P[0][0] + F[1][1] * P[1][0], F[1][0] * P[0][1] + F[1][1] * P[1][1] + Q[1][1])
        )

        // 업데이트 단계
        val H = arrayOf(floatArrayOf(1f, 0f), floatArrayOf(0f, 1f))
        val S = arrayOf(
            floatArrayOf(predictedP[0][0] + R[0][0], predictedP[0][1] + R[0][1]),
            floatArrayOf(predictedP[1][0] + R[1][0], predictedP[1][1] + R[1][1])
        )

        val K = arrayOf(
            floatArrayOf(predictedP[0][0] / S[0][0], predictedP[0][1] / S[1][1]),
            floatArrayOf(predictedP[1][0] / S[0][0], predictedP[1][1] / S[1][1])
        )

        val y = floatArrayOf(measurement[0] - H[0][0] * predictedState[0], measurement[1] - H[1][1] * predictedState[1])
        state[0] = predictedState[0] + K[0][0] * y[0]
        state[1] = predictedState[1] + K[1][1] * y[1]
        P = arrayOf(
            floatArrayOf((1 - K[0][0]) * predictedP[0][0], (1 - K[0][1]) * predictedP[0][1]),
            floatArrayOf((1 - K[1][0]) * predictedP[1][0], (1 - K[1][1]) * predictedP[1][1])
        )

        return state[0]  // 변위 값 반환
    }

    // 압박 깊이 계산
    fun feedbackCPR(accelerometer: FloatArray, gravity: FloatArray) {
        val currentTimestamp = System.currentTimeMillis()

        // 초기 호출 시 타임스탬프 설정
        if (lastTimestamp == null) {
            lastTimestamp = currentTimestamp
            activity.updateDepthFeedback(0) // 첫 호출에서는 0 반환
            activity.updateFrequencyFeedback(0)
            return
        }
        // dt 계산 및 초기값 설정
        val dt = max((currentTimestamp - lastTimestamp!!) / 1000.0f, 0.001f)  // 최소값을 0.001초로 설정
        lastTimestamp = currentTimestamp

        // 중력 보정된 가속도 값 계산
        val accZ = calculateGravityCompensatedAcc(accelerometer, gravity)

        // 보정된 가속도 필터링
        val linearAcc = lowPassFilter(accZ)
        Log.d("깊이 가속도", "가속도 : $linearAcc")

        // 속도 계산
        val velocity = linearAcc * dt

        // 이동 평균 필터 적용
        val depthAfterMovingAvg = applyMovingAverageFilter(velocity)

        // 칼만 필터 적용
        val measurement = floatArrayOf(depthAfterMovingAvg, linearAcc)
        var displacement = kalmanFilter(measurement)

        // 변위 범위 제한
        displacement = max(min(displacement, maxDisplacement), minDisplacement)

        // 순간적인 변화 제한 (5cm 제한)
        if (kotlin.math.abs(displacement - lastDisplacement) > 0.05f) {
            displacement = lastDisplacement + sign(displacement - lastDisplacement) * 0.05f
        }

        lastDisplacement = displacement
        lastDepth = displacement
//        val temp_frequency = frequencyCPR(displacement*100)
//        val temp_depth = depthCPR(displacement*100)
//        val tempList = intArrayOf(temp_frequency, temp_depth)
//        // cm 단위로 변환하여 반환
//        return tempList// Convert to cm

        // 각 피드백을 직접 전달
        val depthFeedback = depthCPR(displacement * 100)
        val frequencyFeedback = frequencyCPR(displacement * 100)

        activity.updateDepthFeedback(depthFeedback)
        activity.updateFrequencyFeedback(frequencyFeedback)
    }

    private fun frequencyCPR(displacement: Float): Int {
        if (displacement > 4.0f && !pussing) {
            pussing = true
            val currentTime = System.currentTimeMillis()

            if (lastTime == null) {
                lastTime = currentTime
                return 0 // "측정중이에요"  // 첫 번째 호출 시 메시지 반환
            }

            val dt = (currentTime - lastTime!!) / 1000f
            if (dt < 1.0) {
                frequencyTimes.add(dt)  // frequencyTimes에 dt 값 추가
            }
            lastTime = currentTime

            if (frequencyTimes.size >= 10) {
                val avgDt = frequencyTimes.average()
                Log.d("빈도 측정","frequency : ${frequencyTimes.toString()}")
                frequencyTimes.clear()  // 평균을 계산 후 리스트 초기화

                return if (avgDt in 0.5..0.6) {
                    2 //"잘하고 있어요"  // 평균이 0.5초~0.6초일 때
                } else if (avgDt > 0.6 ){
                    1 //"느려요"  // 평균이 0.5초~0.6초 범위를 벗어날 때
                } else {
                    3 //"빨라요"
                }
            }
        } else if (displacement < 1f && pussing) {
            pussing = false
        }
        return 0 //"측정중이에요"  // 리스트가 다 채워지지 않았을 때
    }

    private fun depthCPR(displacement: Float): Int {
        if (displacement != 0.0f && depthPussing) {
            if (maxDepth < displacement) {
                maxDepth = displacement
            }
        } else if (displacement == 0.0f) {
            depthPussing = true
            if (maxDepth != 0.0f) {
                depthList.add(maxDepth)
                depthPussing = false
            }
            if (depthList.size >= 10) {
                val avgDepth = depthList.average()
                Log.d("빈도 측정","depth : ${depthList.toString()}")
                depthList.clear()
                return if (avgDepth > 5.0){
                    2 // 적당한 깊이로 잘하고 있어요
                } else {
                    1 // 깊이가 부족해요
                }
            }
            maxDepth = 0.0f
        }
        return 0 // "측정 중이에요"
    }
}