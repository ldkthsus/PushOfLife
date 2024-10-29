package com.example.pushoflife.ui.aedLocation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.pushoflife.BuildConfig
import com.example.pushoflife.R
import com.google.android.gms.location.LocationServices
import com.skt.tmap.overlay.TMapMarkerItem
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.TMapGpsManager
import android.util.Log
import android.widget.ImageButton

class AedLocationActivity : AppCompatActivity(), TMapGpsManager.OnLocationChangedListener {

    private lateinit var tMapView: TMapView
    private lateinit var tMapGpsManager: TMapGpsManager
    private val initialLocation = TMapPoint(37.5665, 126.9780) // 서울 기본 위치
    private val locationPermissionRequestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aed_location)

        val tmapViewContainer = findViewById<FrameLayout>(R.id.tmapViewContainer)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val currentLocationButton = findViewById<ImageButton>(R.id.currentLocationButton)

        backButton.setOnClickListener {
            onBackPressed() // 뒤로 가기 실행
        }

        // 현재 위치 갱신 버튼 클릭 리스너 설정
        currentLocationButton.setOnClickListener {
            checkAndRequestLocationPermission()
        }

        // TMapView 초기화 및 설정
        tMapView = TMapView(this).apply {
            setSKTMapApiKey(BuildConfig.T_MAP_KEY)  // Tmap API 키 설정
        }
        tmapViewContainer.addView(tMapView) // TMapView를 FrameLayout에 추가

        // 지도 로딩 완료 후 위치 설정
        tMapView.setOnMapReadyListener {
            tMapView.setTrackingMode(true)  // 지도 로딩 완료 후 TrackingMode 설정
            tMapView.setSightVisible(true)
            tMapView.setZoomLevel(17)
            checkAndRequestLocationPermission()
        }

        // TMapGpsManager 초기화 및 설정
        tMapGpsManager = TMapGpsManager(this).apply {
            minTime = 1000 // 위치 갱신 주기 (밀리초)
            minDistance = 10f // 위치 갱신 거리 (미터)
            provider = TMapGpsManager.PROVIDER_GPS // GPS 공급자 설정
        }
    }

    private fun checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode
            )
        } else {
            tMapGpsManager.openGps() // GPS 시작
        }
    }

    // 위치가 변경될 때 호출되는 콜백 메서드
    override fun onLocationChange(location: android.location.Location) {
        val userLocation = TMapPoint(location.latitude, location.longitude)
        tMapView.setLocationPoint(userLocation.latitude, userLocation.longitude)
        tMapView.setCenterPoint(userLocation.latitude, userLocation.longitude, true)
        tMapView.setZoomLevel(17)

        // 현재 위치에 마커 추가
        val marker = TMapMarkerItem().apply {
            tMapPoint = userLocation
            name = "현재 위치"
            icon = ContextCompat.getDrawable(this@AedLocationActivity, R.drawable.aed_marker)?.toBitmap()
            id = "current_location_marker" // 마커 ID 설정
        }
        tMapView.addTMapMarkerItem(marker) // 마커 추가
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                tMapGpsManager.openGps()
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tMapGpsManager.closeGps() // Activity 종료 시 GPS 종료
    }
}
