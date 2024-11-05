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

        // MAC 주소를 기준으로 중복 기기 검사
        if (deviceList.none { it.address == device.address }) {
            deviceList.add(device)
            Log.d(TAG, "방금 기기가 추가 되었어용 : ${device.name} (${device.address})")
        } else {
            Log.d(TAG, "기기 중복으로 추가하지 않음: ${device.address}")
        }
    }


    // 스캔된 BLE 기기 리스트를 가져오는 메서드
    fun getDeviceList(): List<BluetoothDevice> = deviceList
}



