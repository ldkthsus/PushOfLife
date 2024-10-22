package com.example.pushoflife.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pushoflife.R
import com.example.pushoflife.ui.theme.PushOfLifeTheme

@Composable
fun ProfileEditScreen() {
    var name by remember { mutableStateOf(TextFieldValue("")) }  // 사용자 이름 상태
    var birthdate by remember { mutableStateOf(TextFieldValue("")) } // 생년월일 상태
    var gender by remember { mutableStateOf(TextFieldValue("")) } // 성별 상태
    var disease by remember { mutableStateOf(TextFieldValue("")) } // 질환 상태
    var hospital by remember { mutableStateOf(TextFieldValue("")) } // 병원 상태
    var pill by remember { mutableStateOf(TextFieldValue("")) } // 복용 약 상태
    var phone by remember { mutableStateOf(TextFieldValue("")) } // 전화번호 상태

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // 타이틀
        Text(
            text = stringResource(id = R.string.profile_edit_screen_text),
            fontSize = 32.sp,  // 글자 크기
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))  // 간격 추가

        // 이름 입력 필드 (아이콘 포함)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(text = stringResource(id = R.string.name)) },  // "이름" 레이블
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.peopleicon),
                    contentDescription = "Name Icon"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))  // 간격 추가

        // 생년월일 입력 필드 (아이콘 포함)
        OutlinedTextField(
            value = birthdate,
            onValueChange = { birthdate = it },
            label = { Text(text = stringResource(id = R.string.age)) },  // "생년월일" 레이블
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.dateicon),
                    contentDescription = "Birthdate Icon"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))  // 간격 추가

        // 성별 입력 필드 (아이콘 포함)
        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text(text = stringResource(id = R.string.gender)) },  // "성별" 레이블
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.gendericon),
                    contentDescription = "Gender Icon"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))  // 간격 추가

        // 질환 입력 필드 (아이콘 포함)
        OutlinedTextField(
            value = disease,
            onValueChange = { disease = it },
            label = { Text(text = stringResource(id = R.string.disease)) },  // "질환" 레이블
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.bandageicon),
                    contentDescription = "Disease Icon"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))  // 간격 추가

        // 병원 입력 필드 (아이콘 포함)
        OutlinedTextField(
            value = hospital,
            onValueChange = { hospital = it },
            label = { Text(text = stringResource(id = R.string.hospital)) },  // "병원" 레이블
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.hospitalicon),
                    contentDescription = "Hospital Icon"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))  // 간격 추가

        // 복용 약 입력 필드 (아이콘 포함)
        OutlinedTextField(
            value = pill,
            onValueChange = { pill = it },
            label = { Text(text = stringResource(id = R.string.pill)) },  // "복용 약" 레이블
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.pillicon),
                    contentDescription = "Pill Icon"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))  // 간격 추가

        // 전화번호 입력 필드 (아이콘 포함)
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(text = stringResource(id = R.string.phone)) },  // "전화번호" 레이블
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.phoneicon),
                    contentDescription = "Phone Icon"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))  // 간격 추가

        // 저장 버튼
        Button(
            onClick = {
                // 저장 로직 처리
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.save))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileEditScreenPreview() {
    PushOfLifeTheme {
        ProfileEditScreen()
    }
}
