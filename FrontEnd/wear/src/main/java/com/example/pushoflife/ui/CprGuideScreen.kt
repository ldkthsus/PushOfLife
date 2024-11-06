package com.example.pushoflife.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pushoflife.theme.wearColorPalette
@Composable
fun CprGuideScreen(angleFeedback: Boolean,totalAcceleration:Float, onExitClick: () -> Unit) {
    // 그라데이션 색상 설정
    val gradientColors = if (angleFeedback) {
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
            Text(
                text = if (angleFeedback) "각도 굿!" else "손목 각도를 90도로 맞춰 주세요.",
                fontSize = 18.sp,
                color = Color.White, // 텍스트 색상
                modifier = Modifier
                    .padding(vertical = 24.dp),
            )
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
