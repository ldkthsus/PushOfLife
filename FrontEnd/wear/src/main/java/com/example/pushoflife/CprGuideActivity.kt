package com.example.pushoflife

import android.os.PowerManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.util.Log
import com.example.pushoflife.sensor.CPRSensor
import com.example.pushoflife.sensor.WristAngleSensor
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.content.Intent
import com.example.pushoflife.ui.CprGuideScreen
import kotlinx.coroutines.*
import java.util.*

class CprGuideActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private var metronomeJob: Job? = null
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    private lateinit var cprSensor: CPRSensor
    private lateinit var wristAngleSensor: WristAngleSensor



    private var isCprGuideStarted = false

    // 피드백 리스트 초기화 (깊이, 빈도, 손목 각도 순서)
    private val feedbackList = mutableListOf(0, 0, 0)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CPRSensor 초기화
        cprSensor = CPRSensor(this)
        cprSensor.initialize(this) // 반드시 initialize 호출

        // CPRSensor 초기화
        wristAngleSensor = WristAngleSensor(this)
        wristAngleSensor.initialize(this) // 반드시 initialize 호출

        // TTS 초기화
        tts = TextToSpeech(this, this)

        // Compose UI 설정
        setContent {
            CprGuideScreen(
                feedbackList = feedbackList,  // 전체 리스트를 전달
                onExitClick = {
                    stopAudio()
                    finish() // 종료 버튼 클릭 시 액티비티 종료
                }
            )
        }

    }

    // 피드백 제공 함수 (우선순위로 확인하여 첫 번째만 제공)
    private fun provideFeedback() {
        when {
            feedbackList[0] == 1 -> Log.d("Feedback", "더 깊게.")
            feedbackList[1] == 1 -> Log.d("Feedback", "느립니다.")
            feedbackList[2] == 1 -> Log.d("Feedback", "잘못된 각도.")
            feedbackList[0] == 2 -> Log.d("Feedback", "깊이 굿.")
            feedbackList[1] == 2 -> Log.d("Feedback", "빈도 굿.")
            feedbackList[2] == 2 -> Log.d("Feedback", "각도 굿.")
        }
    }

    // 깊이 피드백 업데이트
    fun updateDepthFeedback(depthFeedback: Int) {
        feedbackList[0] = depthFeedback
        Log.d("Feedback", "깊이: $depthFeedback")
        provideFeedback()  // 피드백 제공
    }

    // 빈도 피드백 업데이트
    fun updateFrequencyFeedback(frequencyFeedback: Int) {
        feedbackList[1] = frequencyFeedback
        Log.d("Feedback", "빈도: $frequencyFeedback")
        provideFeedback()  // 피드백 제공
    }

    // 각도 피드백 업데이트
    fun updateWristAngleFeedback(angleFeedback: Int) {
        feedbackList[2] = angleFeedback
        Log.d("Feedback", "각도: $angleFeedback")
        provideFeedback()  // 피드백 제공
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREAN
            // CPR 안내가 시작되지 않은 경우에만 TTS 시작
            if (!isCprGuideStarted) {
                startCprGuide()
                isCprGuideStarted = true // 플래그 설정
            }
        } else {
            Log.e("CprGuideActivity", "TTS 초기화 실패")
        }
    }

    private fun startCprGuide() {
        // 포그라운드 서비스 중지
        val stopServiceIntent = Intent(this, SensorForegroundService::class.java)
        stopService(stopServiceIntent)
        val guideText = "시작. " +
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
                toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 100) // 100ms 길이의 톤 생성
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
        toneGenerator.release() // 리소스 해제
    }


    override fun onDestroy() {
        super.onDestroy()
        stopAudio() // 액티비티 종료 시 오디오 정지
    }
}

