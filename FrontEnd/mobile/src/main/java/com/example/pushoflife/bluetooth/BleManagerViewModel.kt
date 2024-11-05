package com.example.pushoflife.bluetooth

import android.Manifest
import android.bluetooth.*
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.*
import com.example.pushoflife.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class BleManagerViewModel(private val bleRepository: BleRepository) : ViewModel() {

    // BLE 상태 및 텍스트 데이터 관리 (UI에는 표시하지 않지만 데이터 흐름을 유지하기 위해 LiveData 사용)
    val statusTxt: LiveData<String>
        get() = bleRepository.fetchStatusText().asLiveData(viewModelScope.coroutineContext)
    val readTxt: LiveData<String>
        get() = bleRepository.fetchReadText().asLiveData(viewModelScope.coroutineContext)

    // BLE 스캔 상태와 연결 상태 관리
    val _isScanning: LiveData<Event<Boolean>>
        get() = bleRepository.isScanning
    var isScanning = ObservableBoolean(false)
    val _isConnect: LiveData<Event<Boolean>>
        get() = bleRepository.isConnect
    var isConnect = ObservableBoolean(false)

    // BLE 스캔된 장치 리스트 (로그로만 처리)
    val listUpdate: LiveData<Event<ArrayList<BluetoothDevice>?>>
        get() = bleRepository.listUpdate

    companion object {
        private const val TAG = "BleManagerViewModel" // TAG 정의
    }

    /**
     * 알림 수신 시 BLE 스캔을 시작
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun onWatchAlertReceived(message:ByteArray) {
        Log.d("BleManagerViewModel", "Watch alert received. Starting BLE scan.")
        startEmergencyScan(message)
    }

    /**
     * BLE 스캔 시작 메서드 (주변 장치 검색)
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun startEmergencyScan(message: ByteArray) {
        viewModelScope.launch {
            Log.d(TAG, "Starting BLE scan.")
            bleRepository.startScan { // 콜백 전달
                onScanComplete(message) // 스캔 완료 시 onScanComplete 호출
            }
        }
    }

    /**
     * BLE 광고 시작 메서드
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP) // 최소 Android L (5.0) 이상에서 실행 가능
    fun startAdvertising() {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bleRepository.startAdvertising()
            } else {
                Log.e(TAG,"Advertising is not supported below Lollipop (API 21).")
            }
        }
    }

    /**
     * 스캔 완료 시 모든 장치에 메시지를 전송하고 연결을 해제
     */
    fun onScanComplete(message: ByteArray) {
        Log.d(TAG, "BLE scan completed. Connecting to all devices to send message.")

        // ByteArray에서 latitude와 longitude를 추출
        val byteBuffer = ByteBuffer.wrap(message)
        val latitude = byteBuffer.double // 앞의 8바이트는 latitude
        val longitude = byteBuffer.double // 뒤의 8바이트는 longitude

        viewModelScope.launch(Dispatchers.IO) {
            bleRepository.sendLocationToAllDevices(latitude, longitude)
        }
    }

    /**
     * 모든 장치에 메시지를 전송하고 연결을 해제
     */
    private fun connectAndSendMessageToAllDevices(message: ByteArray) {
        listUpdate.value?.getContentIfNotHandled()?.let { deviceList ->
            deviceList?.forEach { device ->
                viewModelScope.launch(Dispatchers.IO) {
                    // Android 12 이상에서 권한 체크
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ContextCompat.checkSelfPermission(
                            bleRepository.getContext(), // getContext() 메서드 사용
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.e("BleManagerViewModel", "BLUETOOTH_CONNECT permission is required")
                        return@launch // 권한이 없으면 메서드 종료
                    }

                    Log.d("BleManagerViewModel", "Connecting to device: ${device.name} (${device.address})")

                    // connectDevice를 호출할 때 콜백 처리를 직접 구현하지 않는다면 별도의 람다식 대신에 단순 호출 사용
//                    bleRepository.connectDevice(device)
                }
            }
        }
    }
    /**
     * 메시지 전송 후 연결 해제
     */
    private fun sendMessageAndDisconnect(gatt: BluetoothGatt, message: ByteArray) {
        // Android 12 이상에서 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(
                bleRepository.getContext(), // bleRepository에서 context를 가져오는 부분, context가 public이거나 주입된 경우
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("BleManagerViewModel", "BLUETOOTH_CONNECT permission is required to send message and disconnect.")
            return // 권한이 없으면 함수 종료
        }

        // 메시지 전송 및 연결 해제 로직
        val characteristic = bleRepository.getWriteCharacteristic(gatt)
        characteristic?.let {
            it.value = message
            val writeResult = gatt.writeCharacteristic(it)
            Log.d("BleManagerViewModel", "Message sent to device (${gatt.device.address}): $writeResult")
            gatt.disconnect() // 메시지 전송 후 연결 해제
            Log.d("BleManagerViewModel", "Disconnected from device: ${gatt.device.address}")
        } ?: Log.e("BleManagerViewModel", "Failed to get characteristic for device: ${gatt.device.address}")
    }
}

