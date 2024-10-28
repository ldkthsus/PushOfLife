package com.example.pushoflife.utils

import android.util.Base64
import com.example.pushoflife.network.TwilioRetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object TwilioUtils {
    fun sendSmsWithRetrofit(toPhoneNumber: String, fromPhoneNumber: String, messageBody: String, accountSid: String, authToken: String) {
        val credentials = "$accountSid:$authToken"
        val authHeader = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        // Twilio API 호출
        TwilioRetrofitClient.apiService.sendSms(
            authHeader,
            accountSid,
            toPhoneNumber,
            fromPhoneNumber,
            messageBody
        ).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    println("SMS success")
                } else {
                    println("SMS fail: ${response.code()} ${response}, ${toPhoneNumber}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                println("SMS 전송 오류: ${t.message}")
            }
        })
    }
}
