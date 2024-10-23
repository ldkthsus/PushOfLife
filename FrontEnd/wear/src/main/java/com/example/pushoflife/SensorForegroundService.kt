package com.example.pushoflife

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import com.example.pushoflife.sensor.FallSensor

class SensorForegroundService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    private lateinit var sensorManager: SensorManager
    private val fallSensor = FallSensor()
    private var possibleFall = false
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var heartRateSensor: Sensor? = null

    override fun onCreate() {
        super.onCreate()

        // Foreground 알림 생성
        createNotificationChannel()
        val notification: Notification = Notification.Builder(this, "sensor_channel")
            .setContentTitle("Sensor Service")
            .setContentText("백그라운드에서 센서 데이터를 수집 중")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()

        startForeground(1, notification)

        // SensorManager 초기화
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        // 센서 리스너 등록
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        heartRateSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // 백그라운드 작업 시작 (센서 데이터 수집은 센서 이벤트에서 처리됨)
        startBackgroundTask()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY  // 서비스가 종료되지 않도록 유지
    }

    // 센서 이벤트 리스너
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                //Log.d("SensorData", "Accelerometer: x=$x, y=$y, z=$z")
                if (fallSensor.checkFall(x, y, z)) {
                    Log.d("FallDetection", "낙상감지됨")
                    possibleFall = true
                    // 5초 후 possibleFall을 false로 설정
                    serviceScope.launch {
                        delay(5000L)  // 5초 대기
                        possibleFall = false
                        Log.d("possibleFall", possibleFall.toString())
                        Log.d("FallDetection", "possibleFall 초기화됨")
                    }
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                val x = String.format("%.2f", event.values[0])
                val y = String.format("%.2f", event.values[1])
                val z = String.format("%.2f", event.values[2])
                //Log.d("SensorData", "Gyroscope: x=$x, y=$y, z=$z")
            }
            Sensor.TYPE_HEART_RATE -> {
                val heartRate = String.format("%.2f", event.values[0])
                Log.d("SensorData", "Heart Rate: $heartRate bpm")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 정확도 변경 시 처리할 내용이 있을 경우 여기에 추가
    }

    private fun startBackgroundTask() {
        serviceScope.launch {
            while (isActive) {
                delay(5000L)  // 필요에 따라 백그라운드 작업 수행 (예: 주기적으로 상태 확인)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)  // 센서 리스너 해제
        serviceScope.cancel()  // 백그라운드 작업 취소
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sensor_channel",
                "Sensor Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
