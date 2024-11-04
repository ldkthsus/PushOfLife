package com.example.pushoflife.network

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.example.pushoflife.SecondActivity
import org.json.JSONObject

class WatchListenerService : WearableListenerService() {

    override fun onCreate() {
        super.onCreate()
        Log.d("WatchListenerService", "Service is now waiting for messages...")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d("WatchListenerService", "Message received with path: ${messageEvent.path}")

        if (messageEvent.path == "/notification_path") {
            val message = String(messageEvent.data) // 폰에서 보낸 메시지 내용
            Log.d("WatchListenerService", "Message content: $message")
            // 알림 띄우기
            showNotification(message)
        } else if (messageEvent.path == "/emergency_alert") {
            val dataString = String(messageEvent.data)
            val json = JSONObject(dataString)

            val latitude = json.getDouble("latitude")
            val longitude = json.getDouble("longitude")

            Log.d("WatchApp", "Received latitude: $latitude, longitude: $longitude")
        }
    }

    private fun showNotification(message: String) {
        val notificationId = 1
        val channelId = "sensor_channel" // SensorForegroundService에서 생성한 알림 채널 ID

        val intent = Intent(this, SecondActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("새 알림")
            .setContentText(message)
            .setSmallIcon(com.example.pushoflife.R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }
}
