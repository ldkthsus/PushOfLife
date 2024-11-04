package com.example.pushoflife.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationResult

class LocationUpdatesReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("LocationUpdatesReceiver", "onReceive called") // 확인용 로그

        if (intent.action == "ACTION_PROCESS_UPDATES") {
            Log.d("LocationUpdatesReceiver", "Correct action received") // 액션 확인

            // 위치 정보를 가져옵니다.
            val locationResult = LocationResult.extractResult(intent)
            if (locationResult != null) {
                Log.d("LocationUpdatesReceiver", "LocationResult received") // 위치 결과가 성공적으로 추출되었는지 확인

                locationResult.lastLocation?.let { location ->
                    Log.d("LocationUpdatesReceiver", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                } ?: Log.e("LocationUpdatesReceiver", "Location data is null")
            } else {
                Log.e("LocationUpdatesReceiver", "LocationResult is null")
            }
        } else {
            Log.e("LocationUpdatesReceiver", "Unexpected action: ${intent.action}")
        }
    }
}
