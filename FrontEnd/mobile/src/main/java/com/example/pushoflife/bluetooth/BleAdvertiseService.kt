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
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
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
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(UUID.fromString(SERVICE_UUID)))
            .build()

        advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.d("BleAdvertiseService", "Advertising started successfully")
            }

            override fun onStartFailure(errorCode: Int) {
                Log.e("BleAdvertiseService", "Advertising failed with error code: $errorCode")
            }
        }

        bluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, "BLE_ADVERTISE_CHANNEL")
            .setContentTitle("BLE Advertising")
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
            val service = BluetoothGattService(UUID.fromString(SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY)

            val writeCharacteristic = BluetoothGattCharacteristic(
                UUID.fromString(CHARACTERISTIC_WRITE_UUID),
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )

            Log.d(TAG, "송신 특성에 값 넣으러 간다")
            service.addCharacteristic(writeCharacteristic)
            addService(service)
            Log.d(TAG, "BLE GATT Server 시작 and service added.")
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        override fun onCharacteristicWriteRequest(device: BluetoothDevice, requestId: Int, characteristic: BluetoothGattCharacteristic, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray) {
            if (characteristic.uuid == UUID.fromString(CHARACTERISTIC_WRITE_UUID)) {
                val latitude = ByteBuffer.wrap(value.sliceArray(0 until 8)).double
                val longitude = ByteBuffer.wrap(value.sliceArray(8 until 16)).double
                Log.d(TAG, "위치 데이터 받았당 - Latitude: $latitude, Longitude: $longitude")

                // 위치 데이터 저장
                CoroutineScope(Dispatchers.IO).launch {
                    val emergencyPreferences = EmergencyPreferences(context)
                    emergencyPreferences.saveLocation(latitude, longitude)
                }
                Log.d(TAG, "위치 데이터 넣어유")

                // 응답 전송 전에 권한 확인
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                    Log.d(TAG, "응답 데이터까지 제조 완룡")
                } else {
                    Log.e(TAG, "BLUETOOTH_CONNECT permission not granted")
                }

            }
        }
    }
}
