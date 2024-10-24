package com.example.pushoflife.ui.serviceguide

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pushoflife.ui.theme.PushOfLifeTheme
import com.example.pushoflife.R

@Composable
fun ServiceScreen(navController: NavController) {
    val PretendardBold = FontFamily(
        Font(R.font.pretendard_semibold, FontWeight.SemiBold) // res/font/Poppins-ExtraLight.ttf 파일 참조
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp), // 좌우 여백 추가
        verticalAlignment = Alignment.CenterVertically // 수직 가운데 정렬

    ) {
        // 뒤로 가기 버튼
        IconButton(
            onClick = { navController.popBackStack() }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.backicon),
                contentDescription = "뒤로 가기"
            )
        }

        // 타이틀 텍스트
        Text(
            text = stringResource(id = R.string.service_screen_text),
            fontSize = 24.sp,  // 글자 크기
            textAlign = TextAlign.Center,
            fontFamily = PretendardBold,
            modifier = Modifier
                .weight(1f) // 남은 공간을 차지하도록 설정
                .padding(end = 40.dp)

        )
    }

}

@Preview(showBackground = true)
@Composable
fun ServiceScreenPreview() {
    PushOfLifeTheme {
        val navController = rememberNavController()
        ServiceScreen(navController=navController)
    }
}