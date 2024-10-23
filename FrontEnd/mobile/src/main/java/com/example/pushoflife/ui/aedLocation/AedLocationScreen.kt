package com.example.pushoflife.ui.aedLocation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pushoflife.ui.theme.PushOfLifeTheme
import com.example.pushoflife.R
import com.example.pushoflife.PretendardSemiBold
import com.example.pushoflife.ui.user.ProfileEditScreen


@Composable
fun AedLocationScreen(navController: NavController, modifier: Modifier = Modifier) {

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