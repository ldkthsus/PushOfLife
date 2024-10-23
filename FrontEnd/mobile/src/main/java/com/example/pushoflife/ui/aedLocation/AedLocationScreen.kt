package com.example.pushoflife.ui.aedLocation

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pushoflife.R
import com.example.pushoflife.PretendardSemiBold
import com.example.pushoflife.ui.theme.PushOfLifeTheme
import com.kakao.vectormap.MapView
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun AedLocationScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 뒤로 가기 버튼
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.backicon),
                    contentDescription = "뒤로 가기",
                    modifier = Modifier
                        .size(25.dp) // 아이콘 크기
                )
            }

            // 타이틀 텍스트
            Text(
                text = "AED 위치",
                fontSize = 24.sp,  // 글자 크기
                textAlign = TextAlign.Center,
                fontFamily = PretendardSemiBold,
                modifier = Modifier
                    .weight(1f) // 남은 공간을 차지하도록 설정
                    .padding(end = 40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))  // 간격 추가

        // Kakao MapView 추가
        AndroidView(
            factory = { ctx: Context ->
                MapView(ctx).apply {
                    // 줌 컨트롤 활성화 등 기본 옵션 설정 가능
                }
            },
            modifier = Modifier
                .fillMaxSize()  // 맵이 전체 화면을 차지하게 설정
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AedLocationScreenPreview() {
    PushOfLifeTheme {
        val navController = rememberNavController()
        AedLocationScreen(navController = navController)
    }
}
