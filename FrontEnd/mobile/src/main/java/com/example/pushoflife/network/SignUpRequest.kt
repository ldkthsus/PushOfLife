package com.example.pushoflife.network

data class SignUpRequest(
    val user_name: String?,
    val password1: String,
    val password2: String,
    val user_birthday: Int?,
    val user_gender: String?,
    val user_disease: String?,
    val user_phone: String,
    val user_protector: String?
)