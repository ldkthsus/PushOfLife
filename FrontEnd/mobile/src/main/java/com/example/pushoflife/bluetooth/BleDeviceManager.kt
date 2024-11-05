package com.example.pushoflife.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import android.Manifest

class BleDeviceManager(private val context: Context) {

    // 스캔된 BLE 기기를 저장할 리스트
    private val deviceList: ArrayList<BluetoothDevice> = ArrayList()

    private val TAG = "BleDeviceManager" // 로그 태그 정의

    @SuppressLint("MissingPermission")
    fun addDevice(device: BluetoothDevice) {
        // Android 12(API 31) 이상인 경우에만 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "BLUETOOTH_CONNECT permission 승인해주세여")
                return
            }
        }

        // 중복된 기기가 들어가지 않도록 검사
        if (!deviceList.contains(device)) {
            deviceList.add(device)
            Log.d(TAG, "Added device: ${device.name} (${device.address})")
        }
    }

    // 스캔된 BLE 기기 리스트를 가져오는 메서드
    fun getDeviceList(): List<BluetoothDevice> = deviceList
}



