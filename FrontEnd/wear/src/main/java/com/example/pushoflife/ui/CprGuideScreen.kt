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
fun CprGuideScreen(feedbackList: List<Int>, onExitClick: () -> Unit) {
    // 모든 피드백이 "굿"일 때는 secondary 색상, 그렇지 않으면 error 색상 사용
    val allGood = feedbackList.all { it == 2 }
    val gradientColors = if (allGood) {
        listOf(wearColorPalette.secondary, Color.Black)  // 모든 피드백이 "굿"일 때
    } else {
        listOf(wearColorPalette.error, Color.Black)  // 피드백 중 하나라도 좋지 않을 때
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.radialGradient(colors = gradientColors))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val depthFeedback = when (feedbackList[0]) {
                1 -> "더 깊게."
                2 -> "깊이 굿!"
                else -> "측정 중..."
            }

            val frequencyFeedback = when (feedbackList[1]) {
                1 -> "느립니다."
                2 -> "빈도 굿!"
                3 -> "빠릅니다."
                else -> "측정 중..."
            }

            val angleFeedback = when (feedbackList[2]) {
                1 -> "손목 각도를 90도로 맞춰 주세요."
                2 -> "각도 굿!"
                else -> "측정 중..."
            }

            Text(text = depthFeedback, fontSize = 18.sp, color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
            Text(text = frequencyFeedback, fontSize = 18.sp, color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
            Text(text = angleFeedback, fontSize = 18.sp, color = Color.White, modifier = Modifier.padding(vertical = 8.dp))

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .width(120.dp)
                    .padding(bottom = 4.dp)
                    .clickable { onExitClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "종료", color = Color.White, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
