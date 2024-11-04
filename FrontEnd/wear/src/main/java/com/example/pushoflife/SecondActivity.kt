package com.example.pushoflife

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.activity.ComponentActivity

class SecondActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 화면 밝기를 최대로 설정
        window.attributes = window.attributes.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        }

        // 화면이 절대 꺼지지 않도록 설정
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 레이아웃 설정
        setContentView(R.layout.second_screen)

        // 레이아웃 터치 시 CprGuideActivity로 이동
        val layout = findViewById<LinearLayout>(R.id.second_screen_layout)
        layout.setOnClickListener{
            val intent = Intent(this, CprGuideActivity::class.java)
            startActivity(intent)
        }
    }
}
