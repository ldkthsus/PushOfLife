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
import com.example.pushoflife.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.*
import kotlin.concurrent.schedule

class BleRepository {

    // Central 역할 및 BLE 관리자
    private val TAG = "Central"
    private val context = MyApplication.applicationContext()
    private val bleManager: BluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    private val bleAdapter: BluetoothAdapter? get() = bleManager.adapter
    private var bleGatt: BluetoothGatt? = null

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

    @RequiresApi(Build.VERSION_CODES.M)
    fun startScan() {
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

        Timer("SettingUp", false).schedule(3000) { stopScan() }
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
        Log.d(TAG, "BLE Stop!")
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



    fun connectDevice(device: BluetoothDevice?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission is required.")
            return
        }

        statusTxt = "Connecting to ${device?.address}"
        isStatusChange = true
        bleGatt = device?.connectGatt(context, false, gattClientCallback)
    }

    private val gattClientCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "BLUETOOTH_CONNECT permission is required to connect to GATT server.")
                    disconnectGattServer()
                    return
                }
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                statusTxt = "Connected"
                isStatusChange = true
                Log.d(TAG, "Connected to GATT server")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGattServer()
            }
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
