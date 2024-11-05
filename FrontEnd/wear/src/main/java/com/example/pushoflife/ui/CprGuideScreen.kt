package com.example.pushoflife.ui

import androidx.compose.foundation.layout.*
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CprGuideScreen(onExitClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // TextView 변환
        Text(
            text = "cpr 가이드 화면입니다~",
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Button 변환
        Button(
            onClick = onExitClick,
            modifier = Modifier
                .width(120.dp)
                .padding(bottom = 16.dp)
        ) {
            Text("종료")
        }
    }
}
