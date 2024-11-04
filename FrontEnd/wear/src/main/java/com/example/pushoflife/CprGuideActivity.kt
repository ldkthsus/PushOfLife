package com.example.pushoflife

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.util.Log

class CprGuideActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cpr_guide_screen) // 나중에 레이아웃 파일 추가 예정
    }

    // T/F 값에 따라 피드백 처리
    fun provideAngleFeedback(isOptimalAngle: Boolean) {
        if (isOptimalAngle) {
            Log.d("CprGuideActivity", "각도 굿.")
            // 각도가 올바른 경우의 동작
        } else {
            Log.d("CprGuideActivity", "손목 각도를 90도로 맞춰 주세요.")
            // 각도가 잘못된 경우의 동작
        }
    }
}
