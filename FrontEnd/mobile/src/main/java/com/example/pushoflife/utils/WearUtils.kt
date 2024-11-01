package com.example.pushoflife.utils

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun isWearDeviceConnected(context: Context): Boolean {
    // Bluetooth 권한이 있는지 확인
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        // 권한이 없을 경우 false 반환
        return false
    }

    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter = bluetoothManager?.adapter

    if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
        val pairedDevices = bluetoothAdapter.bondedDevices
        for (device in pairedDevices) {
            // 장치 이름이 "Watch"를 포함하는지 확인하여 Wear OS 장치인지 추정
            if (device.name?.contains("Watch", ignoreCase = true) == true) {
                return true
            }
        }
    }

    // 패키지 매니저를 통해 Wear OS 시스템 패키지가 있는지 확인하여 Wear OS 장치 여부 확인
    return context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)
}
