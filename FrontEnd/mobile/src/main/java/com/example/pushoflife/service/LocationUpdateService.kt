// LocationUpdatesService.kt
package com.example.pushoflife.service

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

class LocationUpdatesService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationUpdatePendingIntent: PendingIntent

    override fun onCreate() {
        super.onCreate()
        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // 마지막 위치를 강제로 한 번 가져오기
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    Log.d("LocationUpdatesService", "Forced location: Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                }
            }
        } else {
            Log.e("LocationUpdatesService", "Location permission not granted for last location.")
        }

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000) // 5초 간격
            .setMinUpdateIntervalMillis(2000) // 최소 2초 간격
            .build()
        // LocationUpdatesService.kt에서 PendingIntent 설정 코드 확인
        val intent = Intent(this, LocationUpdatesReceiver::class.java).apply {
            action = "ACTION_PROCESS_UPDATES"
        }
        locationUpdatePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LocationUpdatesService", "Service onStartCommand called")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdatePendingIntent)
            Log.d("LocationUpdatesService", "Location updates started.")
        } else {
            Log.e("LocationUpdatesService", "Location permission not granted.")
            stopSelf() // 권한이 없으면 서비스 중지
        }
        return START_STICKY
    }

    companion object {
        fun scheduleLocationUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, LocationUpdatesReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                120000, // 2분마다
                pendingIntent
            )
            Log.d("LocationUpdatesService", "Scheduled location updates every 2 minutes.")
        }
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
