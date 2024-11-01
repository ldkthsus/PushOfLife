package com.example.pushoflife.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.koin.androidx.viewmodel.ext.android.viewModel

class BleDeviceActivity : AppCompatActivity() {

    private val viewModel by viewModel<BleManagerViewModel>()
    private lateinit var bleDeviceManager: BleDeviceManager  // BleDeviceManager 인스턴스 생성

    // 권한 요청 런처 설정
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
            // 권한이 부여되면 Foreground Service를 통해 광고 시작
            startBleAdvertiseService()
        } else {
            Toast.makeText(this, "Permissions must be granted", Toast.LENGTH_SHORT).show()
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // BleDeviceManager 초기화 시 context 전달
        bleDeviceManager = BleDeviceManager(this)

        // 권한 확인 및 요청
        if (!hasPermissions(PERMISSIONS)) {
            requestPermissionLauncher.launch(PERMISSIONS)
        } else {
            // 권한이 이미 있는 경우 광고 시작
            startBleAdvertiseService()
        }

        // 스캔 결과를 관찰하여 BleDeviceManager에 저장
        initObserver()
    }

    // 권한이 있는 경우 Foreground Service를 통해 BLE 광고 시작
    private fun startBleAdvertiseService() {
        val serviceIntent = Intent(this, BleAdvertiseService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun initObserver() {
        viewModel.listUpdate.observe(this, {
            it.getContentIfNotHandled()?.let { scanResults ->
                scanResults.forEach { device ->
                    bleDeviceManager.addDevice(device)  // 스캔된 기기를 BleDeviceManager에 추가
                }
            }
        })
    }

    // 모든 기기에 메시지를 전송하는 함수
    private fun sendMessageToAllDevices(message: ByteArray) {
        bleDeviceManager.sendMessageToAllDevices(message)  // 모든 기기에 메시지 전송
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    // 권한 요청 응답 처리
    @Deprecated("Use requestPermissionLauncher instead")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ALL_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions must be granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        // 필요한 BLE 관련 권한
        val PERMISSIONS = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    }
}
