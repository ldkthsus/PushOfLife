package com.example.pushoflife.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.example.pushoflife.MyApplication
import com.example.pushoflife.data.datastore.EmergencyPreferences
import com.example.pushoflife.utils.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.*
import kotlin.concurrent.schedule

class BleRepository() {

    // Central 역할 및 BLE 관리자
    private val TAG = "BleRepository"
    private val context = MyApplication.applicationContext()
    private val bleManager: BluetoothManager =
        context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    private val bleAdapter: BluetoothAdapter? get() = bleManager.adapter
    private var bluetoothGattServer: BluetoothGattServer? = null
    private val deviceManager = BleDeviceManager(context)

    val isScanning = MutableLiveData(Event(false))
    val listUpdate = MutableLiveData<Event<List<BluetoothDevice>?>>()


    init {
        // 서버 초기화
        Log.d("BleRepository", "Context in BleRepository1: $context")
    }

    // 클라이언트 기능: 스캔 및 위치 데이터 전송
    @SuppressLint("MissingPermission")
    fun startScan(onComplete: () -> Unit) {
        Log.d("Repository", "repository에서 스캔 시작")
        Log.d("context", context.toString())
        if (!isPermissionGranted(Manifest.permission.BLUETOOTH_SCAN)) return

        val filters = emptyList<ScanFilter>()
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
        bleAdapter?.bluetoothLeScanner?.startScan(filters, settings, BLEScanCallback)
        isScanning.postValue(Event(true))

        // 일정 시간 후 스캔 중지 및 onComplete 호출
        Timer("SettingUp", false).schedule(5000) {
            stopScan()
            onComplete() // 스캔 중지 후 ViewModel의 onScanComplete 호출
        }
    }

    private val BLEScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val serviceUuid = UUID.fromString(SERVICE_UUID) // 대상 UUID 설정
            val scanRecord = result.scanRecord

            // 스캔된 장치의 서비스 UUID가 일치하는지 확인
            val matches = scanRecord?.serviceUuids?.any { it.uuid == serviceUuid } ?: false
            // 스캔된 기기의 모든 서비스 UUID를 로그로 출력
            scanRecord?.serviceUuids?.forEach { parcelUuid ->
                Log.d(TAG, "스캔된 기기의 UUID: ${parcelUuid.uuid}")
            }
            if (matches) {
                // UUID가 일치하는 경우에만 장치를 목록에 추가
                deviceManager.addDevice(result.device)
                listUpdate.postValue(Event(deviceManager.getDeviceList()))
                Log.d("BleRepository", "UUID가 일치하는 기기가 목록에 추가돼썽")
            } else {
                Log.d("BleRepository", "UUID가 달라서 필터링당함..")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bleAdapter?.bluetoothLeScanner?.stopScan(BLEScanCallback)
        isScanning.postValue(Event(false))
    }

    // 위치 정보를 모든 기기의 송신 특성에 작성
    @SuppressLint("MissingPermission")
    fun sendLocationToAllDevices(latitude: Double, longitude: Double) {
        Log.d("BleRepository", "기기에 메세지 보낼 거임")
        Log.d("BleRepository", latitude.toString() + longitude.toString())
        val locationData = ByteBuffer.allocate(16).apply {
            putDouble(latitude)
            putDouble(longitude)
        }.array() // 위치 정보를 ByteArray로 변환

        for (device in deviceManager.getDeviceList()) {
            device.connectGatt(context, false, object : BluetoothGattCallback() {

                override fun onConnectionStateChange(
                    gatt: BluetoothGatt,
                    status: Int,
                    newState: Int
                ) {
                    if (newState == BluetoothGatt.STATE_CONNECTED) {
                        Log.d("BleRepository", "gatt 연결 성공")
                        gatt.discoverServices() // 연결 성공 시 서비스 탐색 시작
                    } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                        gatt.close()
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        val writeCharacteristic = getWriteCharacteristic(gatt)
                        writeCharacteristic?.let {
                            it.value = locationData
                            gatt.writeCharacteristic(it) // 송신 특성에 위치 정보 쓰기
                            Log.d("BleRepository", "송신 특성에 위치 정보 적었씨유")
                        }
                    }
                }

                override fun onCharacteristicWrite(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    status: Int
                ) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d(TAG, "송신 데이터에 적었구 이제 연결 해제할 거양")
                        gatt.disconnect() // 데이터 전송 후 연결 해제
                    } else {
                        Log.e(TAG, "특성에 데이터 못씀,,, 나 지금 이 상태 : $status")
                    }
                }
            })
        }
    }

    // 송신 특성 가져오기
    fun getWriteCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
        val SERVICE_UUID = UUID.fromString(SERVICE_UUID)
        val WRITE_CHARACTERISTIC_UUID = UUID.fromString(CHARACTERISTIC_WRITE_UUID)

        val service = gatt.getService(SERVICE_UUID)
        return service?.getCharacteristic(WRITE_CHARACTERISTIC_UUID)
    }

    // 권한 체크 메서드
    private fun isPermissionGranted(permission: String): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
    }
}