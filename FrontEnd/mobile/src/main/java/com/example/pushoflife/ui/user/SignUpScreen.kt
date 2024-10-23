package com.example.pushoflife.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pushoflife.MainScreenPreview
import com.example.pushoflife.R
import com.example.pushoflife.data.datastore.UserPreferences
import com.example.pushoflife.ui.theme.PushOfLifeTheme
import com.example.pushoflife.utils.GetLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun SignUpScreen(navController: NavController) {
    var user_phone by remember { mutableStateOf(TextFieldValue("")) } // 전화번호 상태
    var user_password1 by remember { mutableStateOf(TextFieldValue("")) } // 전화번호 상태
    var user_password2 by remember { mutableStateOf(TextFieldValue("")) } // 전화번호 상태
    val PretendardBold = FontFamily(
        Font(R.font.pretendard_bold, FontWeight.Bold) // res/font/Poppins-ExtraLight.ttf 파일 참조
    )

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
                text = stringResource(id = R.string.sign_up),
                fontSize = 28.sp,  // 글자 크기
                textAlign = TextAlign.Center,
                fontFamily = PretendardBold,
                modifier = Modifier
                    .weight(1f) // 남은 공간을 차지하도록 설정
                    .padding(end = 40.dp)

            )
        }
        Spacer(modifier = Modifier.height(16.dp))  // 간격 추가


        // 전화번호 입력 필드 (아이콘 포함)
        TextField(
            value = user_phone,
            onValueChange = { user_phone = it },
            label = { Text(text = stringResource(id = R.string.phone)) },  // "전화번호" 레이블
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.phoneicon),
                    contentDescription = "Phone Icon"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors( unfocusedContainerColor = MaterialTheme.colorScheme.background )

        )

        Spacer(modifier = Modifier.height(8.dp))  // 간격 추가
        // 비밀번호 입력 필드 (아이콘 포함)
        TextField(
            value = user_password1,
            onValueChange = { user_password1 = it },
            label = { Text(text = stringResource(id = R.string.password)) },  // "비밀번호" 레이블
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.passwordicon),
                    contentDescription = "Phone Icon"
                )
            },
            visualTransformation =  PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors( unfocusedContainerColor = MaterialTheme.colorScheme.background )
        )
        Spacer(modifier = Modifier.height(8.dp))  // 간격 추가
        // 비밀번호 확인 입력 필드 (아이콘 포함)
        TextField(
            value = user_password2,
            onValueChange = { user_password2 = it },
            label = { Text(text = stringResource(id = R.string.passwordconfirm)) },  // "비밀번호 확인" 레이블
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.passwordicon),
                    contentDescription = "Phone Icon"
                )
            },
            visualTransformation =  PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors( unfocusedContainerColor = MaterialTheme.colorScheme.background )
        )

        Spacer(modifier = Modifier.height(32.dp))  // 간격 추가

        // 저장 버튼
        Button(
            onClick = {


            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.sign_up))
        }
        Spacer(modifier = Modifier.height(5.dp))  // 간격 추가
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center

        ){
            Text(
                text = stringResource(id = R.string.sign_up_screen_to_login),
                fontSize = 16.sp,  // 글자 크기
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Text(
                    text = stringResource(id = R.string.login),
            fontSize = 16.sp,  // 글자 크기
            textAlign = TextAlign.Center,
                fontFamily = PretendardBold,
            modifier = Modifier

                .clickable {
                    // 로그인 페이지로 이동
                    navController.navigate("LoginScreen")
                }
            )
        }


    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    PushOfLifeTheme {
        val navController = rememberNavController() // 가짜 NavController 생성
        SignUpScreen(navController = navController) // 가짜 NavController 전달
    }
}
