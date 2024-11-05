package com.example.pushoflife.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.example.pushoflife.MyApplication
import com.example.pushoflife.data.datastore.EmergencyPreferences
import com.example.pushoflife.utils.Event
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.ByteBuffer
import java.util.*
import kotlin.concurrent.schedule

class BleRepository (private val context: Context)  {


    // Central 역할 및 BLE 관리자
    private val TAG = "Central"
    //    private val context = MyApplication.applicationContext()
    private val bleManager: BluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    private val bleAdapter: BluetoothAdapter? get() = bleManager.adapter
    private var bleGatt: BluetoothGatt? = null
    private val deviceManager=BleDeviceManager(context)
    var scanResults: ArrayList<BluetoothDevice>? = ArrayList()

    var statusTxt: String = ""
    var txtRead: String = ""
    var isStatusChange: Boolean = false
    var isTxtRead: Boolean = false

    val requestEnableBLE = MutableLiveData<Event<Boolean>>()
    val isScanning = MutableLiveData(Event(false))
    val isConnect = MutableLiveData(Event(false))
    val listUpdate = MutableLiveData<Event<ArrayList<BluetoothDevice>?>>()
    val scrollDown = MutableLiveData<Event<Boolean>>()

    // BluetoothLeAdvertiser와 광고 콜백 설정
    private val bluetoothLeAdvertiser: BluetoothLeAdvertiser? = bleAdapter?.bluetoothLeAdvertiser
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "Advertisement started successfully")
            statusTxt = "Advertisement started successfully"
            isStatusChange = true
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.e(TAG, "Advertisement failed with error code: $errorCode")
            statusTxt = "Advertisement failed with error code: $errorCode"
            isStatusChange = true
        }
    }

    // 광고 시작 메서드
    @SuppressLint("MissingPermission")
    fun startAdvertising() {
        if (bluetoothLeAdvertiser == null) {
            Log.e(TAG, "BluetoothLeAdvertiser is not available")
            statusTxt = "BluetoothLeAdvertiser is not available"
            isStatusChange = true
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "BLUETOOTH_ADVERTISE permission is required.")
            statusTxt = "BLUETOOTH_ADVERTISE permission is required."
            isStatusChange = true
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED) // 광고 주기 설정: Balanced
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(UUID.fromString(SERVICE_STRING))) // 광고 데이터에 서비스 UUID 포함
            .build()

        bluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback)
        statusTxt = "Advertising started."
        isStatusChange = true
        Log.d(TAG, "Advertising started with Balanced mode.")
    }

    // 광고 중지 메서드
    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        if (bluetoothLeAdvertiser == null) {
            Log.e(TAG, "BluetoothLeAdvertiser is not available")
            statusTxt = "BluetoothLeAdvertiser is not available"
            isStatusChange = true
            return
        }

        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
        statusTxt = "Advertising stopped."
        isStatusChange = true
        Log.d(TAG, "Advertising stopped.")
    }

    fun getContext(): Context {
        return context
    }

    fun fetchReadText() = flow {
        while (true) {
            if (isTxtRead) {
                emit(txtRead)
                isTxtRead = false
            }
        }
    }.flowOn(Dispatchers.Default)

    fun fetchStatusText() = flow {
        while (true) {
            if (isStatusChange) {
                emit(statusTxt)
                isStatusChange = false
            }
        }
    }.flowOn(Dispatchers.Default)
    @SuppressLint("MissingPermission")
    fun sendLocationToAllDevices(latitude: Double, longitude: Double) {
        val locationData = ByteBuffer.allocate(16).apply {
            putDouble(latitude)
            putDouble(longitude)
        }.array() // 위치 정보를 ByteArray로 변환

        for (device in deviceManager.getDeviceList()) {
            device.connectGatt(context, false, object : BluetoothGattCallback() {

                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    if (newState == BluetoothGatt.STATE_CONNECTED) {
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
                        }
                    }
                }

                override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d(TAG, "Successfully wrote to characteristic")
                        gatt.disconnect() // 데이터 전송 후 연결 해제
                    } else {
                        Log.e(TAG, "Failed to write characteristic with status: $status")
                    }
                }
            })
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    fun startScan(onComplete:()->Unit) {
        if (bleAdapter == null || !bleAdapter?.isEnabled!!) {
            requestEnableBLE.postValue(Event(true))
            statusTxt = "Scanning Failed: BLE not enabled"
            isStatusChange = true
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "BLUETOOTH_SCAN permission is required.")
            return
        }

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(UUID.fromString(SERVICE_STRING)))
                .build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        bleAdapter?.bluetoothLeScanner?.startScan(filters, settings, BLEScanCallback)
        statusTxt = "Scanning...."
        isStatusChange = true
        isScanning.postValue(Event(true))

        Timer("SettingUp", false).schedule(3000) {
            stopScan()
            onComplete() // 스캔 중지 후 ViewModel의 onScanComplete 호출
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "BLUETOOTH_SCAN permission is required to stop scanning.")
                return // 권한이 없으면 스캔 중지를 수행하지 않음
            }
        }

        bleAdapter?.bluetoothLeScanner?.stopScan(BLEScanCallback)
        isScanning.postValue(Event(false))
        statusTxt = "Scan finished."
        isStatusChange = true
        scanResults = ArrayList()
        Log.d(TAG, "BLE Stop!, ${scanResults}")
    }

    private val BLEScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "BLUETOOTH_SCAN permission is required to scan devices.")
                    return
                }
            }

            Log.i(TAG, "Remote device name: ${result.device.name}")
            addScanResult(result)
        }

        private fun addScanResult(result: ScanResult) {
            val device = result.device
            if (scanResults?.any { it.address == device.address } == true) return
            scanResults?.add(device)
            statusTxt = "Added device: ${device.address}"
            isStatusChange = true
            listUpdate.postValue(Event(scanResults))
        }
    }


//
//    fun connectDevice(device: BluetoothDevice?) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
//            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            Log.e(TAG, "BLUETOOTH_CONNECT permission is required.")
//            return
//        }
//
//        statusTxt = "Connecting to ${device?.address}"
//        isStatusChange = true
//        bleGatt = device?.connectGatt(context, false, gattClientCallback)
//    }

//    private val gattClientCallback = object : BluetoothGattCallback() {
//
//        @SuppressLint("MissingPermission")
//        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                gatt.discoverServices() // 연결 성공 시 서비스 탐색 시작
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                disconnectGattServer()
//            }
//        }
//
//        @SuppressLint("MissingPermission")
//        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                val writeCharacteristic = getWriteCharacteristic(gatt)
//                writeCharacteristic?.let {
//                    it.value = locationData
//                    gatt.writeCharacteristic(it) // 송신 특성에 위치 정보 쓰기
//                }
//            }
//        }
//
//        // 특성이 변경될 때마다 자동으로 호출되는 메소드
//        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
//            val receivedData = characteristic.value // 수신된 데이터 읽기
//            logReceivedData(receivedData) // 로그에 출력
//        }

//        // 필요한 경우, onCharacteristicRead 메소드를 사용하여 수동으로 데이터를 읽을 수도 있습니다.
//        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                val receivedData = characteristic.value // 수신된 데이터 읽기
//                logReceivedData(receivedData) // 로그에 출력
//            }
//        }
//    }

    private fun logReceivedData(data: ByteArray) {
        if (data.size >= 16) {
            val latitude = ByteBuffer.wrap(data.sliceArray(0 until 8)).double
            val longitude = ByteBuffer.wrap(data.sliceArray(8 until 16)).double

            Log.d(TAG, "Received latitude: $latitude, longitude: $longitude")

            // JSON 형식으로 위도와 경도 묶기
            val json = JSONObject().apply {
                put("latitude", latitude)
                put("longitude", longitude)
            }
            val message = json.toString() // JSON 객체를 문자열로 변환

            // DataStore에 위도와 경도 저장
            CoroutineScope(Dispatchers.IO).launch {
                val emergencyPreferences = EmergencyPreferences(context)
                emergencyPreferences.saveLocation(latitude, longitude)
            }

            // 워치로 메시지 전송
            sendMessageToWatch(message)

        } else {
            Log.e(TAG, "Invalid data length: Expected 16 bytes, received ${data.size}")
        }
    }
    fun sendMessageToWatch(message: String) {
        val messageClient: MessageClient = Wearable.getMessageClient(context)
        val path = "/emergency_alert"

        val nodes = Tasks.await(Wearable.getNodeClient(context).connectedNodes)
        for (node in nodes) {
            messageClient.sendMessage(node.id, path, message.toByteArray())
        }
    }
    fun disconnectGattServer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "BLUETOOTH_CONNECT permission is required to disconnect from GATT server.")
                return
            }
        }

        Log.d(TAG, "Closing Gatt connection")
        bleGatt?.let {
            it.disconnect()
            it.close()
            bleGatt = null
        }
        statusTxt = "Disconnected"
        isStatusChange = true
        isConnect.postValue(Event(false))
    }

    fun getWriteCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
        val SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val CHARACTERISTIC_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

        val service = gatt.getService(SERVICE_UUID)
        if (service == null) {
            Log.e("BleRepository", "Service with UUID $SERVICE_UUID not found")
            return null
        }

        val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)
        if (characteristic == null) {
            Log.e("BleRepository", "Characteristic with UUID $CHARACTERISTIC_UUID not found")
        }

        return characteristic
    }
}