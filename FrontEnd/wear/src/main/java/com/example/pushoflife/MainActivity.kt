package com.example.pushoflife

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var sensorServiceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Foreground Service 시작
        sensorServiceIntent = Intent(this, SensorForegroundService::class.java)
        startForegroundService(sensorServiceIntent)  // Foreground Service를 시작

        // 생명주기 감지 삭제 (더 이상 필요 없음)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(sensorServiceIntent)  // 앱이 종료될 때 서비스 중지
    }
}
