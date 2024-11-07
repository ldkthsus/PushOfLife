package com.example.pushoflife.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import java.util.Arrays
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.example.pushoflife.theme.wearColorPalette

var screenColor = false

@Composable
fun CprGuideScreen(totalAcceleration:Float, cprFeedback: IntArray, onExitClick: () -> Unit) {
    // 그라데이션 색상 설정
    Log.d("로그", "${Arrays.toString(cprFeedback)}")
    if ((cprFeedback[0] == 1)||(cprFeedback[1] == 1)||(cprFeedback[2] == 0)||(cprFeedback[1] == 3)) {
        screenColor = false
    } else if ((cprFeedback[0] == 0)&&(cprFeedback[1] == 0)&&(cprFeedback[2] == 0)) {
        Log.d("로그", "$screenColor")
    } else {
        screenColor = true
    }
    val gradientColors = if (screenColor) {
        listOf(wearColorPalette.secondary, Color.Black)
    } else {
        listOf(wearColorPalette.error, Color.Black)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.radialGradient(colors = gradientColors)) // 중앙에서 시작하는 원형 그라데이션
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
//            Text(
//                fontSize = 18.sp,
//                color = Color.White, // 텍스트 색상
//                modifier = Modifier
//                    .padding(vertical = 24.dp),
//            )
            Text(
                    text = "$totalAcceleration",
            fontSize = 18.sp,
            color = Color.White, // 텍스트 색상
            modifier = Modifier
                .padding(vertical = 24.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .width(120.dp)
                    .padding(bottom = 4.dp)
                    .clickable { onExitClick() }, // 클릭 이벤트 처리
                contentAlignment = Alignment.Center // 가운데 정렬 설정
            ) {
                Text(
                    text = "종료",
                    color = Color.White, // 원하는 텍스트 색상 설정
                    modifier = Modifier.padding(8.dp) // 텍스트 주변 패딩
                )
            }
        }
    }
}
