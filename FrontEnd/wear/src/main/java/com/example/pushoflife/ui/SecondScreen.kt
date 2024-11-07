package com.example.pushoflife.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.pushoflife.R


val PretendardSemiBold = FontFamily(
    Font(R.font.pretendard_semibold, FontWeight.SemiBold)
)


@Composable
fun SecondScreen(onNavigateToCprGuide: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .clickable { onNavigateToCprGuide() }, // 클릭 시 콜백 실행
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "CPR을\n시행하려면\n터치",
            fontSize = 28.sp,
            fontFamily = PretendardSemiBold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}
