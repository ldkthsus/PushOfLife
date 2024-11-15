package com.example.pushoflife.sensor.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

object HeartRateSensor : SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var heartRateSensor: Sensor? = null
    private var currentHeartRate: Float = 0.0f  // 가장 최근의 심박수 값 저장
    private var listener: HeartRateListener? = null  // 리스너 추가
    // HeartRateSensor 초기화 및 리스너 등록

    // 리스너 인터페이스 정의
    interface HeartRateListener {
        fun onHeartRateDataChanged(hearRate: Float)
    }

    fun initialize(context: Context, listener: HeartRateListener) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        this.listener = listener  // 리스너 설정
        heartRateSensor?.let {
            sensorManager?.registerListener(this, it, 100000)
        }
    }

    // 센서 데이터가 업데이트될 때마다 호출
    override fun onSensorChanged(event: SensorEvent) {
        currentHeartRate = event.values[0]
        //Log.d("HeartRateSensor", "Current Heart Rate: $currentHeartRate")

        // 리스너를 통해 외부에 데이터 전달
        listener?.onHeartRateDataChanged(getCurrentHeartRate())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // 리스너 해제 메서드
    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    // 외부에서 현재 심박수 값을 얻을 수 있는 메서드
    fun getCurrentHeartRate(): Float = currentHeartRate
}
