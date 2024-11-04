package com.example.pushoflife.bluetooth

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.pushoflife.R
import java.util.UUID

class BleAdvertiseService : Service() {
    private lateinit var bluetoothLeAdvertiser: BluetoothLeAdvertiser
    private lateinit var advertiseCallback: AdvertiseCallback

    override fun onCreate() {
        super.onCreate()
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothLeAdvertiser = bluetoothManager.adapter.bluetoothLeAdvertiser
        startAdvertising()
    }

    @SuppressLint("MissingPermission")
    private fun startAdvertising() {
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
            .setContentText("Advertising is running in the background.")
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
}
