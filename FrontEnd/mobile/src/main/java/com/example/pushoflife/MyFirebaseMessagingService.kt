package com.example.pushoflife

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import android.util.Log
import com.example.pushoflife.data.datastore.EmergencyPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d("FCM Log Token : ", "Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "From: ${remoteMessage.from}")
        Log.d("FCM", "===========================================메시지받음=========================================")


// Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("notificationData", "Message data payload: ${remoteMessage.data}")
            // 위도와 경도 데이터가 있는 경우에만 저장
            val latitude = remoteMessage.data["latitude"]?.toDoubleOrNull()
            val longitude = remoteMessage.data["longitude"]?.toDoubleOrNull()

            if (latitude != null && longitude != null) {
                // DataStore에 위치 정보 저장
                saveLocationToDataStore(latitude, longitude)
            }


        }
        // Check if message contains a notification payload.
        remoteMessage.notification?.let { notification ->
            try{
                Log.d("title", "${notification.title}")
                Log.d("body", "${notification.body}")

            }catch (e: Exception) {
                Log.d("FCM", "Error processing notification: ${e.message}")
            }


        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // 위치 정보를 DataStore에 저장하는 함수
    private fun saveLocationToDataStore(latitude: Double, longitude: Double) {
        val emergencyPreferences = EmergencyPreferences(applicationContext)

        // Coroutine을 사용하여 비동기적으로 DataStore에 위치 정보 저장
        CoroutineScope(Dispatchers.IO).launch {
            emergencyPreferences.saveLocation(latitude, longitude)
            Log.d("MyFirebaseMessagingService", "Location saved: latitude=$latitude, longitude=$longitude")
        }
    }
}