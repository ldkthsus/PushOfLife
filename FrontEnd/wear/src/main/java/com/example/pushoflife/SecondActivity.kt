package com.example.pushoflife

import android.os.Bundle
import androidx.activity.ComponentActivity

class SecondActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 간단한 레이아웃을 설정하여 텍스트를 표시
        setContentView(R.layout.second_screen)
    }
}
