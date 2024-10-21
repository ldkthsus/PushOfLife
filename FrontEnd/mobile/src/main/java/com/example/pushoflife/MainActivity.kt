package com.example.pushoflife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pushoflife.ui.theme.PushOfLifeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PushOfLifeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    // Column을 사용하여 상단과 하단에 텍스트를 배치
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,  // 상단과 하단을 각각 나누어 배치
        horizontalAlignment = Alignment.CenterHorizontally  // 수평 중앙 정렬
    ) {
        // 상단의 'Push of Life' 텍스트
        Text(
            text = stringResource(id = R.string.main_title),
            fontSize = 32.sp,  // 글자 크기
            textAlign = TextAlign.Center
        )

        // 하단의 'components' 텍스트
        Text(
            text = stringResource(id = R.string.main_components),
            fontSize = 24.sp,  // 글자 크기
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PushOfLifeTheme {
        MainScreen()
    }
}