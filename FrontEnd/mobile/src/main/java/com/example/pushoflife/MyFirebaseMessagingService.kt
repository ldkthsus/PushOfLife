package com.example.pushoflife

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import android.util.Log
import com.example.pushoflife.data.datastore.EmergencyPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Build
import androidx.core.app.NotificationCompat

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "emergency_channel"
        private const val CHANNEL_NAME = "Emergency Alerts"
    }
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

            // 메시지 알림 내용이 있을 때 알림 표시
            remoteMessage.notification?.let { notification ->
                val title = notification.title ?: "구조 요청 알림"
                val body = notification.body ?: "근처에 도움이 필요한 환자가 발생했습니다."
                sendNotification(title, body)
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
    // 알림 채널 생성 함수 (Android 8.0 이상 필요)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "긴급 구조 요청 알림을 수신합니다."
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 알림을 생성하고 표시하는 함수
    private fun sendNotification(title: String, body: String) {
        createNotificationChannel() // 채널 생성 (Android 8.0 이상에서만 실행)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}