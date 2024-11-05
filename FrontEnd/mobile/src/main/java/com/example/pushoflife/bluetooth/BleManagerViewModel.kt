package com.example.pushoflife.bluetooth

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushoflife.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class BleManagerViewModel(private val bleRepository: BleRepository) : ViewModel() {

//    val statusTxt: LiveData<String> = bleRepository.fetchStatusText()
//    val readTxt: LiveData<String> = bleRepository.fetchReadText()
    val _isScanning: LiveData<Event<Boolean>> = bleRepository.isScanning
//    val _isConnect: LiveData<Event<Boolean>> = bleRepository.isConnect
    val listUpdate: MutableLiveData<Event<List<BluetoothDevice>?>> = bleRepository.listUpdate

    companion object {
        private const val TAG = "BleManagerViewModel" // TAG 정의
    }

    fun onWatchAlertReceived(message: ByteArray) {
        Log.d(TAG, "Watch alert received. Starting BLE scan.")
        Log.d("ViewModel", "스캔 시작할게유")
        startEmergencyScan(message)
    }

    // ViewModel에서 startEmergencyScan 메서드 수정
    private fun startEmergencyScan(message: ByteArray) {
        viewModelScope.launch {
            Log.d(TAG, "Starting BLE scan.")
            bleRepository.startScan { // 콜백 전달
                onScanComplete(message) // 스캔 완료 시 onScanComplete 호출
            }
        }
    }


//    fun startAdvertising() {
//        viewModelScope.launch {
//            bleRepository.startAdvertising()
//        }
//    }

    fun onScanComplete(message: ByteArray) {
        Log.d(TAG, "BLE scan completed. Connecting to all devices to send message.")
        Log.d(TAG, message.toString())

        // ByteArray에서 latitude와 longitude를 추출
        val byteBuffer = ByteBuffer.wrap(message)
        val latitude = byteBuffer.double // 앞의 8바이트는 latitude
        val longitude = byteBuffer.double // 뒤의 8바이트는 longitude

        viewModelScope.launch(Dispatchers.IO) {
            bleRepository.sendLocationToAllDevices(latitude, longitude)
        }
    }

}
