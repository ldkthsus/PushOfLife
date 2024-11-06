package com.example.pushoflife

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Bundle
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.pushoflife.ui.CprGuideScreen
import kotlinx.coroutines.*
import java.util.*

class CprGuideActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var sensorService: SensorForegroundService? = null
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? SensorForegroundService.SensorBinder
            sensorService = binder?.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
            sensorService = null
        }
    }
    private lateinit var tts: TextToSpeech
    private var metronomeJob: Job? = null
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    // 상태 변수 선언 (Composable에 전달할 값)
    private var isOptimalAngle by mutableStateOf(true) // 기본값을 설정하거나 업데이트 가능

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TTS 초기화
        tts = TextToSpeech(this, this)
        // Service 바인딩
        Intent(this, SensorForegroundService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        // Compose UI 설정
        setContent {
            var totalAcceleration by remember { mutableStateOf(0.0f) }
            LaunchedEffect(Unit) {
                while (isServiceBound) {
                    totalAcceleration = sensorService?.getTotalAcceleration() ?: 0.0f
                    delay(1000L) // 주기적으로 업데이트
                }
            }
            CprGuideScreen(
                angleFeedback = isOptimalAngle,
                totalAcceleration = totalAcceleration,
                onExitClick = {
                stopAudio()
                finish() // 종료 버튼 클릭 시 액티비티 종료
            })
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREAN
            startCprGuide()  // 초기화 완료 후 TTS 시작
        } else {
            Log.e("CprGuideActivity", "TTS 초기화 실패")
        }
    }

    private fun startCprGuide() {
        val guideText = "심폐소생술 가이드를 시작합니다. " +
                "무릎을 꿇은 채로 세워 주세요." +
                "검지, 중지, 약지를 펴고 환자의 명치에 약지를 대 주세요." +
                "검지가 놓인 지점에 손꿈치를 대 주세요." +
                "손목과 팔의 각도를 90도로 유지해 주세요." +
                "박자에 맞춰 5cm 깊이로 흉부 압박 시작합니다." +
                "3,2,1."

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                runOnUiThread {
                    startMetronome() // TTS가 완료되면 메트로놈 시작
                }
            }
            override fun onError(utteranceId: String?) {}
        })

        // TTS로 안내 방송 시작
        tts.speak(guideText, TextToSpeech.QUEUE_FLUSH, null, "CPR_GUIDE")
    }

    private fun startMetronome() {
        val intervalMillis = (60000 / 110).toLong() // 110Bpm에 맞춘 간격: 약 545ms

        metronomeJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100) // 100ms 길이의 톤 생성
                delay(intervalMillis)
            }
        }
    }

    private fun stopAudio() {
        if (tts.isSpeaking) {
            tts.stop()
        }
        tts.shutdown()

        metronomeJob?.cancel()
        toneGenerator.release() //리소스 해제
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudio() // 액티비티 종료 시 오디오 정지
    }

    // T/F 값에 따라 피드백 처리
    fun provideAngleFeedback(isOptimal: Boolean) {
        isOptimalAngle = isOptimal
        if (isOptimalAngle) {
//            Log.d("CprGuideActivity", "각도 굿.")
            // 각도가 올바른 경우의 동작
        } else {
//            Log.d("CprGuideActivity", "손목 각도를 90도로 맞춰 주세요.")
            // 각도가 잘못된 경우의 동작
        }
    }
}
