
package com.example.pushoflife

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var sensorServiceIntent: Intent

    // 권한 요청 런처 설정
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // 권한이 승인된 경우 처리할 로직
            } else {
                // 권한이 거부된 경우 처리할 로직
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 안드로이드 13 이상에서 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // 권한 요청
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Foreground Service 시작
        sensorServiceIntent = Intent(this, SensorForegroundService::class.java)
        startForegroundService(sensorServiceIntent)  // Foreground Service를 시작
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(sensorServiceIntent)  // 앱이 종료될 때 서비스 중지
    }
}

