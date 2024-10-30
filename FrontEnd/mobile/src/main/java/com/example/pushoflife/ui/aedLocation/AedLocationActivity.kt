package com.example.pushoflife.ui.aedLocation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.pushoflife.BuildConfig
import com.example.pushoflife.R
import com.example.pushoflife.network.RetrofitClient
import com.google.android.gms.location.LocationServices
import com.skt.tmap.TMapGpsManager
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.overlay.TMapMarkerItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.pushoflife.network.MapBoundsResponse
import android.os.Handler
import android.os.Looper
import com.example.pushoflife.network.AedDetailResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.skt.tmap.poi.TMapPOIItem


class AedLocationActivity : AppCompatActivity(), TMapGpsManager.OnLocationChangedListener, TMapView.OnClickListenerCallback {

    private lateinit var tMapView: TMapView
    private lateinit var tMapGpsManager: TMapGpsManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocationMarker: TMapMarkerItem? = null // 현재 위치 마커를 저장
    private val locationPermissionRequestCode = 100 // 요청 코드 값 설정
    private val handler = Handler(Looper.getMainLooper())
    private var lastCenterPoint: TMapPoint? = null
    private var lastZoomLevel: Float? = null

    // 3초 딜레이를 위한 Runnable
    private val delayedApiRequest = Runnable {
        lastCenterPoint?.let { centerPoint ->
            lastZoomLevel?.let { zoomLevel ->
                logMapBounds(zoomLevel, centerPoint) // 3초 후 API 요청
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aed_location)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val tmapViewContainer = findViewById<FrameLayout>(R.id.tmapViewContainer)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val currentLocationButton = findViewById<ImageButton>(R.id.currentLocationButton)

        backButton.setOnClickListener {
            onBackPressed()
        }

        currentLocationButton.setOnClickListener {
            checkAndRequestLocationPermission()
            updateUserLocationOnMap()
        }

        tMapView = TMapView(this).apply {
            setSKTMapApiKey(BuildConfig.T_MAP_KEY)
        }
        tmapViewContainer.addView(tMapView)

        tMapView.setOnMapReadyListener {
            tMapView.setZoomLevel(17)
            checkAndRequestLocationPermission()
        }

        tMapGpsManager = TMapGpsManager(this).apply {
            minTime = 1000
            minDistance = 10f
        }

        // 줌/스크롤 변경 시마다 지연된 API 요청 실행
        tMapView.setOnDisableScrollWithZoomLevelListener { fl, tMapPoint -> onMapChanged(fl, tMapPoint) }
        tMapView.setOnEnableScrollWithZoomLevelListener { fl, tMapPoint -> onMapChanged(fl, tMapPoint) }

        // 마커 클릭 이벤트 리스너 등록
        tMapView.setOnClickListenerCallback(this)
    }

    // TMapView.OnClickListenerCallback 인터페이스의 onPressDown 메서드 구현
    override fun onPressDown(
        markerList: ArrayList<TMapMarkerItem>?,
        poiList: ArrayList<TMapPOIItem>?,
        point: TMapPoint?,
        pointf: PointF?
    ) {
        // 필요한 경우에만 구현
    }

    // TMapView.OnClickListenerCallback 인터페이스의 onPressUp 메서드 구현
    override fun onPressUp(
        markerList: ArrayList<TMapMarkerItem>?,
        poiList: ArrayList<TMapPOIItem>?,
        point: TMapPoint?,
        pointf: PointF?
    ) {
        markerList?.firstOrNull()?.let { marker ->
            // 지도 중심 이동
            tMapView.setCenterPoint(marker.tMapPoint.latitude, marker.tMapPoint.longitude, true)
            val aedId = marker.id.toIntOrNull()
            aedId?.let { id ->
                Log.d("AED Marker Clicked", "Clicked AED ID: $id")

                // API 요청 보내기
                RetrofitClient.apiService.sendAedDetail(id).enqueue(object : Callback<AedDetailResponse> {
                    override fun onResponse(call: Call<AedDetailResponse>, response: Response<AedDetailResponse>) {
                        if (response.isSuccessful) {
                            response.body()?.let { aedDetail ->
                                // 응답 값 콘솔에 출력
                                Log.d("AED Detail", """
                                Address: ${aedDetail.aed_address}
                                Place: ${aedDetail.aed_place}
                                Location: ${aedDetail.aed_location}
                                Latitude: ${aedDetail.aed_latitude}
                                Longitude: ${aedDetail.aed_longitude}
                                Number: ${aedDetail.aed_number}
                            """.trimIndent())
                            }
                            response.body()?.let { aedDetail ->
                                // 응답 값을 이용해 BottomSheetDialogFragment 표시
                                val bottomSheet = AEDDetailBottomSheet(
                                    address = aedDetail.aed_address,
                                    place = aedDetail.aed_place,
                                    location = aedDetail.aed_location,
                                    number = aedDetail.aed_number
                                )
                                bottomSheet.show(supportFragmentManager, "AEDDetailBottomSheet")
                            }
                        } else {
                            Log.d("AED Detail API", "요청 실패: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<AedDetailResponse>, t: Throwable) {
                        Log.d("AED Detail API", "네트워크 오류: ${t.message}")
                    }
                })
            }
        }
    }



    // 현재 위치를 지도에 업데이트하는 함수
    private fun updateUserLocationOnMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLocation = TMapPoint(location.latitude, location.longitude)

                    // 지도 중심을 사용자 위치로 설정
                    tMapView.setCenterPoint(userLocation.latitude, userLocation.longitude, true)

                    // 현재 위치 마커 추가 또는 업데이트
                    if (userLocationMarker == null) {
                        userLocationMarker = TMapMarkerItem().apply {
                            tMapPoint = userLocation
                            name = "현재 위치"
                            icon = ContextCompat.getDrawable(this@AedLocationActivity, R.drawable.user_location)?.toBitmap()
                            id = "current_location_marker"
                        }
                        tMapView.addTMapMarkerItem(userLocationMarker)
                    } else {
                        userLocationMarker?.tMapPoint = userLocation
                        tMapView.refreshDrawableState()
                    }
                } else {
                    Toast.makeText(this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_LONG).show()
        }
    }

    private fun onMapChanged(zoomLevel: Float, centerPoint: TMapPoint) {
        handler.removeCallbacks(delayedApiRequest)
        lastCenterPoint = centerPoint
        lastZoomLevel = zoomLevel
        handler.postDelayed(delayedApiRequest, 1000)
    }

    private fun checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode
            )
        } else {
            tMapGpsManager.openGps()
        }
    }

    override fun onLocationChange(location: android.location.Location) {
        val userLocation = TMapPoint(location.latitude, location.longitude)

        // 사용자 위치로 맵 중심 설정
        tMapView.setCenterPoint(userLocation.latitude, userLocation.longitude, true)

        if (userLocationMarker == null) {
            userLocationMarker = TMapMarkerItem().apply {
                tMapPoint = userLocation
                name = "현재 위치"
                icon = ContextCompat.getDrawable(this@AedLocationActivity, R.drawable.user_location)?.toBitmap()
                id = "current_location_marker"
            }
            tMapView.addTMapMarkerItem(userLocationMarker)
        } else {
            userLocationMarker?.tMapPoint = userLocation
            tMapView.refreshDrawableState()
        }

        onMapChanged(tMapView.zoomLevel.toFloat(), tMapView.centerPoint)
    }

    // AED 마커 API 호출
    private fun logMapBounds(zoomLevel: Float, centerPoint: TMapPoint) {
        val latitudeOffset = calculateLatitudeOffset(zoomLevel.toInt())
        val longitudeOffset = calculateLongitudeOffset(zoomLevel.toInt())

        val northEastLat = centerPoint.latitude + latitudeOffset
        val northEastLon = centerPoint.longitude + longitudeOffset
        val southWestLat = centerPoint.latitude - latitudeOffset
        val southWestLon = centerPoint.longitude - longitudeOffset

        Log.d("CurrentBounds", "NE: ($northEastLat, $northEastLon), SW: ($southWestLat, $southWestLon)")

        // GET 요청을 쿼리 파라미터와 함께 호출
        val call = RetrofitClient.apiService.sendMapBounds(
            norLatitude = northEastLat,
            norLongitude = northEastLon,
            souLatitude = southWestLat,
            souLongitude = southWestLon
        )

        call.enqueue(object : Callback<List<MapBoundsResponse>> {
            override fun onResponse(call: Call<List<MapBoundsResponse>>, response: Response<List<MapBoundsResponse>>) {
                if (response.isSuccessful) {
                    val aedLocations = response.body()

                    // 모든 AED 마커를 지우고, 사용자 위치 마커는 유지
                    tMapView.removeAllTMapMarkerItem()
                    userLocationMarker?.let { tMapView.addTMapMarkerItem(it) }

                    aedLocations?.forEach { location ->
                        // AED 정보 로그 출력
                        Log.d("AED Info", "Address: ${location.aed_address}, Place: ${location.aed_place}, Location: ${location.aed_location}")

                        // AED 마커 추가
                        addAedMarker(location.aed_latitude, location.aed_longitude, location.aed_id, location.aed_place, location.aed_location)
                    }
                } else {
                    Log.d("MapBoundsAPI", "요청 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<MapBoundsResponse>>, t: Throwable) {
                Log.d("MapBoundsAPI", "네트워크 오류: ${t.message}")
            }
        })
    }



    private fun addAedMarker(latitude: Double, longitude: Double, aedId: Int, place: String, location: String) {
        val aedMarker = TMapMarkerItem().apply {
            tMapPoint = TMapPoint(latitude, longitude)
            name = "$place ($location)"
            icon = ContextCompat.getDrawable(this@AedLocationActivity, R.drawable.aed_marker)?.toBitmap()
            id = aedId.toString()  // aed_id를 마커 ID로 설정
        }
        tMapView.addTMapMarkerItem(aedMarker)
    }


    // Latitude 및 Longitude 계산 메서드
    private fun calculateLatitudeOffset(zoomLevel: Int): Double {
        return when (zoomLevel) {
            17 -> 0.002
            16 -> 0.004
            15 -> 0.008
            14 -> 0.016
            else -> 0.032
        }
    }

    private fun calculateLongitudeOffset(zoomLevel: Int): Double {
        return when (zoomLevel) {
            17 -> 0.003
            16 -> 0.006
            15 -> 0.012
            14 -> 0.024
            else -> 0.048
        }
    }

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
        handler.removeCallbacks(delayedApiRequest)
        tMapGpsManager.closeGps()
    }
}