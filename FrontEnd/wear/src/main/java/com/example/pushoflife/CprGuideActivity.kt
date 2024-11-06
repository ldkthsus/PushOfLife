package com.example.pushoflife

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.util.Log
import com.example.pushoflife.ui.CprGuideScreen
import kotlinx.coroutines.*
import java.util.*

class CprGuideActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var metronomeJob: Job? = null
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TTS 초기화
        tts = TextToSpeech(this, this)

        // Compose UI 설정
        setContent {
            CprGuideScreen(onExitClick = {
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
        toneGenerator.release() //리소스 해제
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudio() // 액티비티 종료 시 오디오 정지
    }

    // T/F 값에 따라 피드백 처리
    fun provideAngleFeedback(isOptimalAngle: Boolean) {
        if (isOptimalAngle) {
            Log.d("CprGuideActivity", "각도 굿.")
            // 각도가 올바른 경우의 동작
        } else {
            Log.d("CprGuideActivity", "손목 각도를 90도로 맞춰 주세요.")
            // 각도가 잘못된 경우의 동작
        }
    }
}
