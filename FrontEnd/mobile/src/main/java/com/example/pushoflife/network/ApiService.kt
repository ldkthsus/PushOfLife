package com.example.pushoflife.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Twilio SMS 전송 요청
    @FormUrlEncoded
    @POST("Accounts/{AccountSid}/Messages.json")
    fun sendSms(
        @Header("Authorization") authHeader: String, // 인증 헤더
        @Path("AccountSid") accountSid: String,
        @Field("To") toPhoneNumber: String,
        @Field("From") fromPhoneNumber: String,
        @Field("Body") messageBody: String
    ): Call<Void>

    // 기존의 다른 API 요청들
//    @GET("POL/test")
//    fun getTestData(@Query("id") id: Int): Call<TestResponse>

//    @POST("POL/users/join")
//    fun signUpUser(@Body signUpRequest: SignUpRequest): Call<Void>
}
