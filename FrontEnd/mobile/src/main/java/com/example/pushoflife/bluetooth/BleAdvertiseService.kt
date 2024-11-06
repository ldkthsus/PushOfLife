package com.example.pushoflife.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.pushoflife.MyApplication
import com.example.pushoflife.R
import com.example.pushoflife.data.datastore.EmergencyPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.UUID

class BleAdvertiseService : Service() {
    private lateinit var bluetoothLeAdvertiser: BluetoothLeAdvertiser
    private lateinit var advertiseCallback: AdvertiseCallback
    private var bluetoothGattServer: BluetoothGattServer? = null
    private val context = MyApplication.applicationContext()
    private val bleManager: BluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    private val TAG = "BleAdvertiseService"

    override fun onCreate() {
        super.onCreate()
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothLeAdvertiser = bluetoothManager.adapter.bluetoothLeAdvertiser
        Log.d("song", "백그라운드에서 광고 중")

        // Notification Channel 생성
        createNotificationChannel()

        startAdvertising()

        startServer()
    }

    @SuppressLint("MissingPermission")
    private fun startAdvertising() {
        Log.d("song", "광고 하려고 데이터 빌드")

        val advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.d("BleAdvertiseService", "Advertising started successfully")
                Log.d("song", "광고 보냄!") // 광고할 때마다 로그 출력
            }

            override fun onStartFailure(errorCode: Int) {
                Log.e("song", "광고 보내기 실패애,,, 나 지금 이 상태야 : $errorCode")
            }
        }

        fun startBLEAdvertising() {
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build()

            val serviceUuid = UUID.fromString(SERVICE_UUID)
            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(ParcelUuid(serviceUuid))
                .build()

            bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback)
        }

        fun stopAndRestartAdvertising() {
            bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
            startBLEAdvertising() // 다시 광고 시작
        }

        // Handler를 사용하여 1초마다 광고를 중지하고 재시작
        val handler = Handler(Looper.getMainLooper())
        val advertisingRunnable = object : Runnable {
            override fun run() {
                stopAndRestartAdvertising()
                handler.postDelayed(this, 2000) // 1초 후에 다시 실행
            }
        }

        handler.post(advertisingRunnable) // 초기 실행
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, "BLE_ADVERTISE_CHANNEL")
            .setContentTitle("Push Of Life")
            .setContentText("백그라운드에서 광고중이예요...")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        startForeground(1, notification)
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "BLE_ADVERTISE_CHANNEL",
                "BLE Advertising Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for BLE advertising background service"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    // 서버 기능: 광고 및 송신 특성 변경 감지
    @SuppressLint("MissingPermission")
    fun startServer() {
        bluetoothGattServer = bleManager.openGattServer(context, gattServerCallback)?.apply {
            // constants에 정의된 UUID 사용
            val service = BluetoothGattService(UUID.fromString(SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY)

            // 쓰기 특성 생성
            val writeCharacteristic = BluetoothGattCharacteristic(
                UUID.fromString(CHARACTERISTIC_WRITE_UUID),
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )

            // 서비스에 특성 추가
            Log.d(TAG, "송신 특성 및 서비스 추가 중...")
            Log.d(TAG, "데이터 받을려고 GATT 서버에 서비스 설정해씨유" + service)
            service.addCharacteristic(writeCharacteristic)

            // GATT 서버에 서비스 추가
            addService(service)
            Log.d(TAG, "BLE GATT Server 시작 및 서비스 추가 완료.")
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        // 클라이언트의 연결 상태가 변경될 때 호출되는 메서드
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "클라이언트가 GATT 서버에 연결되었습니다: ${device.address}")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "클라이언트가 GATT 서버에서 연결이 해제되었습니다: ${device.address}")
                    // 연결이 끊어지면 서버를 닫거나 리소스를 해제할 수 있음
                }
            }
        }

        // 클라이언트가 특성에 데이터를 쓰려고 할 때 호출되는 메서드
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            if (characteristic.uuid == UUID.fromString(CHARACTERISTIC_WRITE_UUID)) {
                // 클라이언트가 보낸 데이터를 읽어 처리
                val latitude = ByteBuffer.wrap(value.sliceArray(0 until 8)).double
                val longitude = ByteBuffer.wrap(value.sliceArray(8 until 16)).double
                Log.d(TAG, "위치 데이터 수신 - 위도: $latitude, 경도: $longitude")

                // 위치 데이터 저장 (비동기 처리)
                CoroutineScope(Dispatchers.IO).launch {
                    val emergencyPreferences = EmergencyPreferences(context)
                    emergencyPreferences.saveLocation(latitude, longitude)
                }
                Log.d(TAG, "위치 데이터 저장 완료")

                // 응답 전송 (권한 확인 후)
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                    Log.d(TAG, "응답 데이터 전송 완료")
                } else {
                    Log.e(TAG, "BLUETOOTH_CONNECT 권한이 없습니다.")
                }
            }
        }
    }
}
