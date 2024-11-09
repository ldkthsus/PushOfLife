package com.example.pushoflife

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.util.Log
import androidx.compose.runtime.*
import com.example.pushoflife.ui.CprGuideScreen
import kotlinx.coroutines.*
import java.util.*
import com.example.pushoflife.sensor.utils.GravitySensor
import com.example.pushoflife.sensor.utils.LinearAccelerationSensor
import com.example.pushoflife.sensor.CPRSensor
import android.view.WindowManager

class CprGuideActivity : ComponentActivity(), TextToSpeech.OnInitListener,
    GravitySensor.GravitySensorListener, LinearAccelerationSensor.LinearAccelerationListener {
    private lateinit var sensorServiceIntent: Intent
    private lateinit var cprSensor: CPRSensor
    private var feedbackCPR by mutableStateOf(intArrayOf(0, 0, 0))  // 세 개의 값으로 초기화

    private lateinit var tts: TextToSpeech
    private var metronomeJob: Job? = null
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    // 최근에 수신된 중력값과 가속도값을 저장
    private var latestGravityValues: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f)
    private var latestAccelerationValues: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // TTS 초기화
        tts = TextToSpeech(this, this)
        // Foreground Service 중지
        sensorServiceIntent = Intent(this, SensorForegroundService::class.java)
        stopService(sensorServiceIntent) // SecondActivity에 진입할 때 Foreground Service 종료

        // 센서 초기화 및 리스너 설정
        GravitySensor.initialize(this, this)
        LinearAccelerationSensor.initialize(this, this)

        // CPRSensor 초기화
        cprSensor = CPRSensor()

        // Compose UI 설정
        setContent {
            var totalAcceleration by remember { mutableStateOf(0.0f) }
            var previousFeedback by remember { mutableStateOf(intArrayOf(0, 0, 0)) }

            LaunchedEffect(feedbackCPR) {
                // feedbackCPR이 변경된 경우만 CprGuideScreen에 값 전달
                if (!feedbackCPR.contentEquals(previousFeedback)) {
                    previousFeedback = feedbackCPR.copyOf()  // 이전 값 업데이트
                }
            }

            CprGuideScreen(
                totalAcceleration = totalAcceleration,
                onExitClick = {
                    stopAudio()
                    finish()
                },
                cprFeedback = feedbackCPR
            )
        }
    }

    // GravitySensor 데이터 변경 시 호출
    override fun onGravityDataChanged(gravityValues: FloatArray) {
        latestGravityValues = gravityValues
        checkAndUpdateCprFeedback() // 중력 데이터가 변경될 때마다 피드백 업데이트 시도
    }

    // LinearAccelerationSensor 데이터 변경 시 호출
    override fun onLinearAccelerationDataChanged(accelerationValues: FloatArray) {
        latestAccelerationValues = accelerationValues
        checkAndUpdateCprFeedback() // 가속도 데이터가 변경될 때마다 피드백 업데이트 시도
    }

    // 두 센서의 데이터를 모두 확인하여 CPR 피드백 업데이트
    private fun checkAndUpdateCprFeedback() {
        feedbackCPR = cprSensor.feedbackCPR(latestAccelerationValues, latestGravityValues)
        if ((feedbackCPR[0] != 0)||(feedbackCPR[1] != 0)) {
            Log.d("CPR Feedback Calculation", "Calculated Feedback - Depth: ${feedbackCPR[0]}, Frequency: ${feedbackCPR[1]}, Angle: ${feedbackCPR[2]}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREAN
            startCprGuide()
        } else {
            Log.e("CprGuideActivity", "TTS 초기화 실패")
        }
    }

    private fun startCprGuide() {
        val guideText = "시작. 무릎을 꿇은 채로 세워 주세요... 박자에 맞춰 5cm 깊이로 흉부 압박 시작합니다. 3,2,1."

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                runOnUiThread { startMetronome() }
            }
            override fun onError(utteranceId: String?) {}
        })

        tts.speak(guideText, TextToSpeech.QUEUE_FLUSH, null, "CPR_GUIDE")
    }

    private fun startMetronome() {
        val intervalMillis = (60000 / 110).toLong()

        metronomeJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 100)
                delay(intervalMillis)
            }
        }
    }

    private fun stopAudio() {
        if (tts.isSpeaking) tts.stop()
        tts.shutdown()

        metronomeJob?.cancel()
        toneGenerator.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudio()
    }
    override fun onStop() {
        super.onStop()
        stopAudio() // TTS와 메트로놈을 멈춤
        GravitySensor.stop() // 중력 센서 업데이트 중지
        LinearAccelerationSensor.stop() // 가속도 센서 업데이트 중지
    }
}
