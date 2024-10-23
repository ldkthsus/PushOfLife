package com.example.pushoflife.utils

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.google.android.gms.location.*
import java.util.Locale

@Composable
fun GetLocation(context: Context): String {
    var address by remember { mutableStateOf("위치 정보를 불러오는 중...") }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                requestLocation(fusedLocationClient) { location ->
                    // Geocoder를 한국어 설정으로 초기화
                    val geocoder = Geocoder(context, Locale.KOREA)  // Locale을 KOREA로 설정
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        address = addresses[0].getAddressLine(0) // 도로명 주소
                    }
                }
            } else {
                address = "위치 권한이 없습니다."
            }
        }
    )

    // 권한 요청 실행
    LaunchedEffect(Unit) {
        locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    return address
}

fun requestLocation(fusedLocationClient: FusedLocationProviderClient, onLocationReceived: (Location) -> Unit) {
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        1000L // 위치 업데이트 간격
    ).build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val location = locationResult.lastLocation
            if (location != null) {
                onLocationReceived(location)
                fusedLocationClient.removeLocationUpdates(this) // 위치 정보를 받으면 콜백 종료
            }
        }
    }

    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
    )
}
