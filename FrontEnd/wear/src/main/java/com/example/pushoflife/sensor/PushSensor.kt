package com.example.pushoflife.sensor

class PushSensor {

    private val startThreshold = 10.0 // 압박 시작을 판별하는 임계값
    private val endThreshold = 2.0 // 압박 끝을 판별하는 임계값 (조정 필요)
    private val depthRange = 5.0 // 적절한 압박 깊이 (5cm를 기준으로 설정)
    private var isPressing = false
    private var pressDepth = 0.0

    private var previousVelocity = 0.0
    private var previousDistance = 0.0
    private var lastUpdateTime: Long = System.currentTimeMillis()

    fun calculateCPRFeedback(acceleration: Float): String {
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

            return when {
                depthDifference >= depthRange && distance >= depthRange ->
                    "적절한 깊이와 이동 거리로 CPR이 수행되고 있습니다."
                depthDifference < depthRange ->
                    "압박 깊이가 충분하지 않습니다."
                else ->
                    "압박 이동 거리가 부족합니다."
            }
        }

        previousVelocity = currentVelocity
        previousDistance = distance

        return "측정 중..."
    }
}
