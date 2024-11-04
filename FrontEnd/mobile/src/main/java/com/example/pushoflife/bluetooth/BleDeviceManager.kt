package com.example.pushoflife.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Build // Build.VERSION_CODES.S를 사용하기 위한 import
import androidx.core.content.ContextCompat // ContextCompat 사용을 위한 import
import android.content.pm.PackageManager // PackageManager 사용을 위한 import
import android.util.Log // Log 사용을 위한 import
import android.Manifest // Manifest.permission 사용을 위한 import
import java.util.UUID


class BleDeviceManager(private val context: Context) {

    // 스캔된 BLE 기기를 저장할 리스트
    private val deviceList: ArrayList<BluetoothDevice> = ArrayList()

    private val TAG = "BleDeviceManager" // 로그 태그 정의


    // BLE 기기 리스트에 기기 추가하는 메서드
    fun addDevice(device: BluetoothDevice) {
        // 중복된 기기가 들어가지 않도록 검사
        if (!deviceList.contains(device)) {
            deviceList.add(device)
        }
    }

    // 스캔된 BLE 기기 리스트를 가져오는 메서드
    fun getDeviceList(): List<BluetoothDevice> {
        return deviceList
    }

    fun connectAndSendData(device: BluetoothDevice, message: ByteArray) {
        // Android 12 이상에서 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "BLUETOOTH_CONNECT permission is required to connect and send data.")
                return // 권한이 없으면 메서드 종료
            }
        }

        // 기기와 BLE 연결 설정
        val bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "BLUETOOTH_CONNECT permission is required.")
                        return // 권한이 없으면 메서드 종료
                    }
                }

                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    // 연결 성공 시 서비스 탐색
                    gatt.discoverServices()
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    // 연결이 끊어진 경우 처리
                    gatt.close()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "BLUETOOTH_CONNECT permission is required.")
                        return // 권한이 없으면 메서드 종료
                    }
                }

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // 원하는 서비스와 특성을 찾은 후 메시지 전송
                    val service = gatt.getService(UUID.fromString("YOUR_SERVICE_UUID"))
                    val characteristic = service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_COMMAND_STRING))

                    characteristic?.let {
                        it.value = message
                        gatt.writeCharacteristic(it)
                    }
                }
            }


            override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // 데이터 전송 성공 처리
                    println("Data sent successfully")
                }
            }
        })
    }


    // 기기 리스트에 저장된 모든 기기에게 메시지 보내기
    fun sendMessageToAllDevices(message: ByteArray) {
        for (device in deviceList) {
            connectAndSendData(device, message)
        }
    }
}
