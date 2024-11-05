package com.example.pushoflife.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SecondScreen(onNavigateToCprGuide: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onNavigateToCprGuide() }, // 클릭 시 콜백 실행
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "CPR을 시행하려면 터치",
            fontSize = 18.sp
        )
    }
}
