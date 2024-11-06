package com.example.pushoflife.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context.BLUETOOTH_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.example.pushoflife.MyApplication
import com.example.pushoflife.utils.Event
import kotlinx.coroutines.delay
import java.nio.ByteBuffer
import java.util.*
import kotlin.concurrent.schedule

class BleRepository {

    private val TAG = "BleRepository"
    private val context = MyApplication.applicationContext()
    private val bleManager: BluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    private val bleAdapter: BluetoothAdapter? get() = bleManager.adapter
    private var bluetoothGattServer: BluetoothGattServer? = null
    private val deviceManager = BleDeviceManager(context)

    private var bleGatt: BluetoothGatt? = null
    private val connectedDevices = mutableSetOf<String>()

    val isScanning = MutableLiveData(Event(false))
    val listUpdate = MutableLiveData<Event<List<BluetoothDevice>?>>()

    init {
        Log.d("BleRepository", "사용 중인 CONTEXT : $context")
    }

    @SuppressLint("MissingPermission")
    fun startScan(onComplete: () -> Unit) {
        Log.d(TAG, "REPOSITORY에서 스캔 시작이야요")
        if (!isPermissionGranted(Manifest.permission.BLUETOOTH_SCAN)) return

        val filters = emptyList<ScanFilter>()
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
        bleAdapter?.bluetoothLeScanner?.startScan(filters, settings, BLEScanCallback)
        isScanning.postValue(Event(true))

        Timer("스캔 타이머", false).schedule(5000) {
            stopScan()
            onComplete()
        }
    }

    private val BLEScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val serviceUuid = UUID.fromString(SERVICE_UUID)
            val scanRecord = result.scanRecord

            val matches = scanRecord?.serviceUuids?.any { it.uuid == serviceUuid } ?: false
            scanRecord?.serviceUuids?.forEach { parcelUuid ->
                Log.d(TAG, "발견한 기기의 UUID : ${parcelUuid.uuid}")
            }
            if (matches) {
                deviceManager.addDevice(result.device)
                listUpdate.postValue(Event(deviceManager.getDeviceList()))
                Log.d(TAG, "UUID가 일치하는 기기 발견 ! LIST에 기기 추가함 !")
            } else {
                Log.d(TAG, "UUID가 달라서 필터링 당함")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!isPermissionGranted(Manifest.permission.BLUETOOTH_SCAN)) return
        bleAdapter?.bluetoothLeScanner?.stopScan(BLEScanCallback)
        isScanning.postValue(Event(false))
    }

    @SuppressLint("MissingPermission")
    suspend fun sendLocationToAllDevices(latitude: Double, longitude: Double) {
        Log.d(TAG, "LIST에 담긴 기기들에게 메세지 보내유")
        val locationData = ByteBuffer.allocate(16).apply {
            putDouble(latitude)
            putDouble(longitude)
        }.array()

        for (device in deviceManager.getDeviceList()) {
            if (connectedDevices.contains(device.address)) {
                Log.d(TAG, "이미 연결 중인 기기임: ${device.address}")
                continue
            }

            if (!isPermissionGranted(Manifest.permission.BLUETOOTH_CONNECT)) {
                Log.e(TAG, "연결 권한이 없습니다: ${device.address}")
                continue
            }

            connectedDevices.add(device.address)
            delay(1000)

            Log.d(TAG, "현재 연결 시도하는 기기 : ${device.address}")
            bleGatt = connectGattCompat(object : BluetoothGattCallback() {

                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    Log.d(TAG, "기기의 연결 상태가 변경됨 : ${gatt.device.address}, status: $status, newState: $newState")

                    if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.d(TAG, "GATT 연결 성공: ${gatt.device.address}")
                        gatt.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.d(TAG, "GATT 연결 해제: ${gatt.device.address}")
                        cleanupGattConnection(gatt)
                    } else if (status == 133) {
                        Log.e(TAG, "GATT 연결 실패 - 상태 코드: 133")
                        cleanupGattConnection(gatt)
                        retryConnectionWithDelay(gatt.device, this) // 적절한 콜백 인스턴스 전달

                    } else {
                        Log.e(TAG, "GATT 연결 실패 - 상태 코드: $status")
                        cleanupGattConnection(gatt)
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d(TAG, "서비스 발견 성공: ${gatt.device.address}")
                        val writeCharacteristic = getWriteCharacteristic(gatt)
                        writeCharacteristic?.let {
                            it.value = locationData
                            gatt.writeCharacteristic(it)
                            Log.d(TAG, "송신 특성에 위치 정보 작성 완료")
                        }
                    } else {
                        Log.e(TAG, "서비스 발견 실패 - 상태 코드: $status")
                    }
                }

                override fun onCharacteristicWrite(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    status: Int
                ) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d(TAG, "데이터 작성 성공: ${gatt.device.address}")
                        gatt.disconnect()
                    } else {
                        Log.e(TAG, "특성에 데이터 작성 실패 - 상태 코드: $status")
                    }
                }
            }, device, true)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectGattCompat(
        bluetoothGattCallback: BluetoothGattCallback,
        device: BluetoothDevice,
        autoConnect: Boolean
    ): BluetoothGatt? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !isPermissionGranted(Manifest.permission.BLUETOOTH_CONNECT)) {
                Log.e(TAG, "연결 권한이 없습니다.")
                return null
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d("song", "버전 높아서 매개변수 넣어줌")
                device.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                device.connectGatt(context, false, bluetoothGattCallback)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "권한이 없어 GATT 연결에 실패했습니다.", e)
            null
        }
    }

    private fun cleanupGattConnection(gatt: BluetoothGatt) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !isPermissionGranted(Manifest.permission.BLUETOOTH_CONNECT)) {
                Log.e(TAG, "연결 해제 권한이 없습니다.")
                return
            }

            gatt.disconnect()
            gatt.close()
            connectedDevices.remove(gatt.device.address)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException 발생: 권한 문제로 연결 해제 실패", e)
        }
    }

    private fun retryConnectionWithDelay(device: BluetoothDevice, callback: BluetoothGattCallback) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !isPermissionGranted(Manifest.permission.BLUETOOTH_CONNECT)) {
                Log.e(TAG, "재연결 권한이 없습니다.")
                return
            }

            Handler(Looper.getMainLooper()).postDelayed({
                connectGattCompat(callback, device, true)
            }, 5000) // 5초 후에 재연결 시도
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException 발생: 권한 문제로 재연결 실패", e)
        }
    }

    fun getWriteCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
        val service = gatt.getService(UUID.fromString(SERVICE_UUID))
        return service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_WRITE_UUID))
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
