package com.example.pushoflife.sensor

import android.util.Log
import android.content.Context
import kotlin.math.acos
import kotlin.math.sqrt
import com.example.pushoflife.CprGuideActivity
import com.example.pushoflife.sensor.utils.GravitySensor

class WristAngleSensor (private val activity: CprGuideActivity) : GravitySensor.GravitySensorListener {
    private val fixedF = 0.0
    private val fixedG = 0.0
    private val fixedH = 9.80665 // 중력

    override fun onGravityDataChanged(gravityValues: FloatArray) {
        val x = gravityValues[0].toDouble()
        val y = gravityValues[1].toDouble()
        val z = gravityValues[2].toDouble()

        // 3D 벡터 각도 계산
        val angle = calculateAngle(x, y, z)

        // CprGuideActivity로 전달
        val isOptimalAngle = angle in 80.0 .. 100.0
        activity.provideAngleFeedback(isOptimalAngle)
    }

    // 3D 벡터 각도 계산 함수
    private fun calculateAngle(c: Double, d: Double, e: Double): Double {
        val s = (c * fixedF) + (d * fixedG) + (e * fixedH)
        val t = sqrt((c * c) + (d * d) + (e * e))
        val r = sqrt((fixedF * fixedF) + (fixedG * fixedG) + (fixedH * fixedH))
        val u = s / (t * r)
        val inv = Math.toDegrees(acos(u))

        return roundToTwoDecimals(inv)
    }

    // 소수점 두 자리로 반올림
    private fun roundToTwoDecimals(value: Double): Double {
        return (Math.round(value * 100) / 100.0)
    }

    fun initialize(context: Context) {
        GravitySensor.initialize(context, this)
    }

    fun stop() {
        GravitySensor.stop()
    }
}