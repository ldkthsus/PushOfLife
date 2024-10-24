package com.example.pushoflife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pushoflife.ui.serviceguide.ServiceScreen
import com.example.pushoflife.ui.user.ProfileEditScreen
import com.example.pushoflife.ui.guidecpr.CprGuideScreen
import com.example.pushoflife.ui.theme.PushOfLifeTheme
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import kotlinx.coroutines.delay
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.pushoflife.ui.aedLocation.AedLocationScreen
import com.example.pushoflife.ui.user.LoginScreen
import com.example.pushoflife.ui.user.SignUpScreen
import android.content.Context
import android.util.Base64
import android.util.Log
import com.kakao.vectormap.KakaoMapSdk
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.contracts.contract

fun getKeyHash(context: Context): String? {
    try {
        val info = context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_SIGNATURES)
        for (signature in info.signatures) {
            val md = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
        }
    } catch (e: Exception) {
        Log.e("KeyHash", "Unable to get KeyHash.", e)
    }
    return null
}
// 폰트 정의
val PoppinsExtraLight = FontFamily(
    Font(R.font.poppins_extralight, FontWeight.ExtraLight)
)
val PretendardSemiBold = FontFamily(
    Font(R.font.pretendard_semibold, FontWeight.SemiBold)
)
val PretendardRegular = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PushOfLifeTheme {
                val navController = rememberNavController() // Navigation Controller 생성
                NavHost(navController = navController, startDestination = "main_screen") {
                    composable("main_screen") { MainScreen(navController) }  // 메인 화면
                    composable("service_screen") { ServiceScreen() }          // 서비스 가이드 화면
                    composable("profile_edit_screen") { ProfileEditScreen(navController) } // 프로필 수정 화면
                    composable("signUpScreen") { SignUpScreen(navController) }
                    composable("loginScreen") { LoginScreen(navController) }
                    composable("cpr_guide_screen") { CprGuideScreen(navController) } // CPR 가이드 스크린
                    composable("aed_location_screen") { AedLocationScreen(navController) } // CPR 가이드 스크린
                }
            }
        }
        val keyHash = getKeyHash(this)
        Log.d("KeyHash", keyHash ?: "KeyHash not found")
        // 로그 출력으로 값 확인
        Log.d("KakaoMapKey", "API Key: ${BuildConfig.KAKAO_MAP_KEY}")
        // 카카오 지도 SDK 초기화
        KakaoMapSdk.init(this, BuildConfig.KAKAO_MAP_KEY)
    }
}

@Composable
fun MainScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    // 현재 카드 인덱스를 저장하는 상태
    var currentCardIndex by remember { mutableStateOf(1) }

    // 3초마다 카드 인덱스를 변경하는 LaunchedEffect
    LaunchedEffect(key1 = currentCardIndex) {
        while (true) {
            delay(4000L)  // 4초마다 실행
            currentCardIndex = (currentCardIndex % 3) + 1  // 1 -> 2 -> 3 순환
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 20.dp, top = 40.dp, end = 20.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단의 'Push of Life' 텍스트와 이미지
        Row(
            verticalAlignment = Alignment.CenterVertically,  // 수직으로 가운데 정렬
            modifier = Modifier
                .fillMaxWidth(),
        ) {

            Text(
                text = stringResource(id = R.string.app_name),
                fontFamily = PoppinsExtraLight,
                fontSize = 30.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f),
                letterSpacing = (-1).sp
            )

            // 이미지 클릭 시 프로필 수정 화면으로 이동
            Image(
                painter = painterResource(id = R.drawable.profileicon),  // VectorDrawable 사용
                contentDescription = "Profile Icon",
                modifier = Modifier
                    .size(35.dp)  // 이미지 크기 설정
                    .clickable { navController.navigate("profile_edit_screen") },  // 클릭 이벤트 처리
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Divider(
            color = Color.Gray,            // 선 색상
            thickness = 0.5.dp,              // 선 두께
            modifier = Modifier.padding(vertical = 8.dp)  // 위아래 여백
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 서비스 가이드 컴포넌트
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { navController.navigate("service_screen") }  // 스크린 이동
        ) {
            Image(
                painter = painterResource(id = R.drawable.watch_guide),
                contentDescription = "워치 가이드",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Text(
                text = "PushOfLife 워치 가이드",
                fontSize = 20.sp,
                fontFamily = PretendardSemiBold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(top = 15.dp, start = 15.dp)
            )

        }

        Spacer(modifier = Modifier.height(10.dp))

        // AED 컴포넌트
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { navController.navigate("aed_location_screen") },
        ) {
            Image(
                painter = painterResource(id = R.drawable.aed_location),
                contentDescription = "AED 위치",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Text(
                text = "AED 위치",
                fontSize = 20.sp,
                fontFamily = PretendardSemiBold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(top = 15.dp, start = 15.dp)
            )

        }

        Spacer(modifier = Modifier.height(10.dp))

        // CPR 가이드 컴포넌트
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { navController.navigate("cpr_guide_screen") }  // 스크린 이동
        ) {
            Image(
                painter = painterResource(id = R.drawable.cpr_guide),
                contentDescription = "CPR 가이드",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Text(
                text = "CPR, 어떻게 하나요?",
                fontSize = 20.sp,
                fontFamily = PretendardSemiBold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(top = 15.dp, start = 15.dp)
            )

        }

        Spacer(modifier = Modifier.height(10.dp))

        // 상태에 따라 다른 카드 컴포넌트를 렌더링
        when (currentCardIndex) {
            1 -> CardComponent1()
            2 -> CardComponent2()
            3 -> CardComponent3()
        }
    }
}

// 카드 컴포넌트 1
@Composable
fun CardComponent1() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color = Color(0xFFE7CAD5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 텍스트
            Text(
                text = "CPR 압박 위치",
                fontSize = 20.sp,
                fontFamily = PretendardSemiBold,
                letterSpacing = (-0.5).sp,
                color = Color(0xFF3A3A3C),
                textAlign = TextAlign.Center,
            )

            // 중앙 이미지
            Image(
                painter = painterResource(id = R.drawable.card_cprwhere),
                contentDescription = "CPR 가이드",
                modifier = Modifier
                    .width(270.dp)
                    .height(170.dp)
            )

            // 하단 텍스트
            Text(
                text = "명치에 세 손가락을 얹고 \n 얼굴과 가장 가까운 손가락의 위치를 압박하세요!",
                fontSize = 14.sp,
                fontFamily = PretendardRegular,
                letterSpacing = (-0.5).sp,
                color = Color(0xFF3A3A3C),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// 카드 컴포넌트 2
@Composable
fun CardComponent2() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color = Color(0xFFD5DCFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 텍스트
            Text(
                text = "AED, 왜 사용해야 할까요?",
                fontSize = 20.sp,
                fontFamily = PretendardSemiBold,
                letterSpacing = (-0.5).sp,
                color = Color(0xFF3A3A3C),
                textAlign = TextAlign.Center,
            )

            // 중앙 이미지
            Image(
                painter = painterResource(id = R.drawable.card_aedwhy),
                contentDescription = "AED 설명",
                modifier = Modifier
                    .width(270.dp)
                    .height(170.dp)
            )

            // 하단 텍스트
            Text(
                text = "심장 발작 시 전기 충격을 주어\n정상 리듬으로 되돌리기 위해 사용됩니다",
                fontSize = 14.sp,
                fontFamily = PretendardRegular,
                letterSpacing = (-0.5).sp,
                color = Color(0xFF3A3A3C),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// 카드 컴포넌트 3
@Composable
fun CardComponent3() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color = Color(0xFFFFC1C1))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 텍스트
            Text(
                text = "CPR, 왜 필요할까요?",
                fontSize = 20.sp,
                fontFamily = PretendardSemiBold,
                letterSpacing = (-0.5).sp,
                color = Color(0xFF3A3A3C),
                textAlign = TextAlign.Center,
            )

            // 중앙 이미지
            Image(
                painter = painterResource(id = R.drawable.card_cprwhy),
                contentDescription = "CPR 설명",
                modifier = Modifier
                    .width(270.dp)
                    .height(170.dp)
            )

            // 하단 텍스트
            Text(
                text = "심장과 뇌에 산소가 포함된 혈액을 공급해주는 CPR!\n 뇌에 피를 공급해 뇌 손상을 방지하려는 목적입니다",
                fontSize = 14.sp,
                fontFamily = PretendardRegular,
                letterSpacing = (-0.5).sp,
                color = Color(0xFF3A3A3C),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PushOfLifeTheme {
        MainScreen(navController = rememberNavController())
    }
}