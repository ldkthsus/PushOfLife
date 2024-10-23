import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pushoflife.R
import com.example.pushoflife.ui.theme.PushOfLifeTheme

// Pretendard 폰트 정의
val PretendardSemiBold = FontFamily(
    Font(R.font.pretendard_semibold, FontWeight.SemiBold)
)

@Composable
fun CprGuideScreen(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { 5 }) // Pager 상태 관리
    val coroutineScope = rememberCoroutineScope() // CoroutineScope 사용

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 20.dp, top = 40.dp, end = 20.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단의 'CPR 가이드' 텍스트
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "CPR 가이드",
                fontFamily = PretendardSemiBold,
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                letterSpacing = (-1).sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pager - 5개의 카드를 슬라이드로 구현
        HorizontalPager(
            state = pagerState, // Pager 상태 설정
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color = when (page) {
                        0 -> Color(0xFFE7CAD5)
                        1 -> Color(0xFFDECBD3)
                        2 -> Color(0xFFD0E1F9)
                        3 -> Color(0xFFFFC1C1)
                        else -> Color(0xFFD1E8E2)
                    })
            ) {
                Text(
                    text = "카드 ${page + 1}",
                    fontSize = 20.sp,
                    fontFamily = PretendardSemiBold,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(top = 15.dp, start = 15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 페이지 인디케이터
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(5) { index ->
                val color = if (pagerState.currentPage == index) {
                    Color(0xFF757575)
                } else {
                    Color.LightGray
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .padding(horizontal = 4.dp)
                        .background(color = color, shape = RoundedCornerShape(50))
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun CprGuideScreenPreview() {
    PushOfLifeTheme {
        CprGuideScreen()
    }
}
