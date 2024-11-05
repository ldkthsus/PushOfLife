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

    val _isScanning: LiveData<Event<Boolean>> = bleRepository.isScanning
    val listUpdate: MutableLiveData<Event<List<BluetoothDevice>?>> = bleRepository.listUpdate

    companion object {
        private const val TAG = "BleManagerViewModel" // TAG 정의
    }

    fun onWatchAlertReceived(message: ByteArray) {
        Log.d(TAG, "스캔 시작할게유")
        startEmergencyScan(message)
    }

    // ViewModel에서 startEmergencyScan 메서드 수정
    private fun startEmergencyScan(message: ByteArray) {
        viewModelScope.launch {
            Log.d(TAG, "스 캔 시 작")
            bleRepository.startScan { // 콜백 전달
                onScanComplete(message) // 스캔 완료 시 onScanComplete 호출
            }
        }
    }
    fun onScanComplete(message: ByteArray) {
        Log.d(TAG, "BLE 스캔 끝 ! 기기들한테 메세지 보내러 간다.")
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
