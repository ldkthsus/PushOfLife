package com.example.pushoflife.ui.aedLocation

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun AedLocationScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // 권한 요청 런처 설정
    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                // 권한이 없을 경우 처리
                Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            }
        }
    )

    // 권한 요청
    LaunchedEffect(Unit) {
        locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Remember MapView and handle lifecycle
    val mapView = remember { MapView(context) }

    DisposableEffect(Unit) {
        // Start the map view
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API가 정상적으로 종료될 때 호출
            }

            override fun onMapError(error: Exception) {
                // 지도 사용 중 에러가 발생할 때 호출
                Toast.makeText(context, "지도 초기화 에러: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                // 지도 인증 후 정상적으로 지도가 출력될 때 호출
            }
        })

        onDispose {
            mapView.finish()  // MapView의 라이프사이클 종료 처리
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
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


        // Kakao MapView 추가 및 생명주기 처리
        AndroidView(
            factory = { ctx: Context ->
                mapView
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
