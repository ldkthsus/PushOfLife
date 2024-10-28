package com.example.pushoflife

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import android.util.Log

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
            // Notify를 띄우는 코드 작성 필요


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

}