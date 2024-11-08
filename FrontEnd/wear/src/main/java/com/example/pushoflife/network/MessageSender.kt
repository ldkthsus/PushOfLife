package com.example.pushoflife.network

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class MessageSender(private val context: Context) {

    fun sendMessages(nodeId: String, path: String, message: String) {
        val messageClient: MessageClient = Wearable.getMessageClient(context)

        messageClient.sendMessage(nodeId, path, message.toByteArray())
            .addOnSuccessListener {
                Log.d("MessageSender", "Message sent successfully to $nodeId")
            }
            .addOnFailureListener {
                Log.e("MessageSender", "Failed to send message", it)
            }
    }

    fun sendCPRStart(nodeId: String, path: String, message: String) {

    }

    fun sendCPREnd(nodeId: String, path: String, message: String) {

    }
}
