package com.example.pushoflife.utils

import android.content.Context
import android.content.Intent

object LaunchApp {
    // 앱을 강제로 실행하는 메서드
    fun launchApp(context: Context, targetActivity: Class<*>) {
        val intent = Intent(context, targetActivity).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }
}
// 실제 코드에서의 활용은 아래처럼
// import com.example.pushoflife.utils.LaunchApp
// LaunchApp.launchApp(this, SecondActivity::class.java)
