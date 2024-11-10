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

/*
----------
1. SecondActivity에서 넘어온 경우 (환자 워치에서 모든 가이드를 담당)
    -> 가이드 tts + 메트로놈 tts
    -> 신고 로직 끝 (확인)
4. BLE통신을 통해 넘어오는 경우 (fromNotification에서 옴)
    -> 피드백 tts
    -> 신고 로직 중복 신고 해야 하는 건지 통신 팀에게 질문 (안 함)
---------

2. CprWaitingActivity(가제)에서 넘어온 경우 (POL유저가 아닌 환자 발견 시에 헬퍼가 들어오는 곳)
    -> 가이드 tts + 메트로놈 tts + 피드백 tts
    -> 신고 로직 없음 (넣어야 함)
3. MainActivity에서 {cpr 연습 버튼}을 눌러 넘어오는 경우
    -> 가이드 tts + 메트로놈 tts + 피드백 tts
    -> 신고하면 안 됨
 */

class CprGuideActivity : ComponentActivity(), TextToSpeech.OnInitListener,
    GravitySensor.GravitySensorListener, LinearAccelerationSensor.LinearAccelerationListener {
    private lateinit var sensorServiceIntent: Intent
    private lateinit var cprSensor: CPRSensor
    private var feedbackCPR by mutableStateOf(intArrayOf(0, 0, 0))  // 세 개의 값으로 초기화
    private var inputFeedback by mutableStateOf(intArrayOf(0, 0, 0))
    private var feedbackTurn = 1 // 피드백 우선순위 턴을 관리하는 변수
    private lateinit var tts: TextToSpeech
    private var metronomeJob: Job? = null
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
    private var feedbackJob: Job? = null
    private var previousFeedback: IntArray? = null
    private var totalAcceleration : Float = 0.0f

    // 최근에 수신된 중력값과 가속도값을 저장
    private var latestGravityValues: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f)
    private var latestAccelerationValues: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // 이전 페이지를 기록하는 내비게이션
        val previousActivity = intent.getStringExtra("previous_activity")
        Log.d("ActivityInfo", "Navigated from $previousActivity")
        tts = TextToSpeech(this, this)
        // Foreground Service 중지
        sensorServiceIntent = Intent(this, SensorForegroundService::class.java)
        stopService(sensorServiceIntent) // SecondActivity에 진입할 때 Foreground Service 종료

        GravitySensor.initialize(this, this)
        LinearAccelerationSensor.initialize(this, this)

        // CPRSensor 초기화
        cprSensor = CPRSensor()

        feedbackJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive){
                delay(5000) // 5초
                //processFeedback()
            }
        }

        // Compose UI 설정
        setContent {

            LaunchedEffect(inputFeedback,totalAcceleration) {
                // feedbackCPR이 변경된 경우만 CprGuideScreen에 값 전달
            }

            CprGuideScreen(
                totalAcceleration = totalAcceleration,
                onExitClick = {
                    stopAudio()
                    finish()
                },
                cprFeedback = inputFeedback
            )
        }
        // 분기 조건에 따라 TTS 및 메트로놈 실행
        when (previousActivity) {
            "SecondActivity" -> {
                // SecondActivity에서 넘어온 경우: 가이드 TTS와 메트로놈 시작
                startCprGuide() // 가이드 TTS: "시작. 무릎을 꿇은 채로..."
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        runOnUiThread { startMetronome() }
                    }
                    override fun onError(utteranceId: String?) {}
                })
            }
            "fromNotification" -> {
                // 워치가 있는 환자에게 CPR 시행하는 헬퍼
                processFeedback()
            }
            "CprWaitingActivity","MainActivity" -> {
                // 가이드 tts + 메트로놈 + 피드백 tts
                startCprGuide()
                tts.setOnUtteranceProgressListener(object: UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        // 가이드 tts 완료
                        // 메트로놈 + 피드백 tts 시작
                        runOnUiThread {
                            startMetronome()
                            processFeedback()
                        }
                    }
                    override fun onError(utteranceId: String?) {}
                })
            }
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
        // CPRData 객체 반환받기
        val tempData = cprSensor.feedbackCPR(latestAccelerationValues, latestGravityValues)
// feedbackCPR에 0번부터 2번 인덱스에 해당하는 depth, frequency, angle 값을 넣기
        feedbackCPR = intArrayOf(tempData.depth, tempData.frequency, tempData.angle)
        totalAcceleration = tempData.accZ
        if ((feedbackCPR[0] != 0)&&(feedbackCPR[1] != 0)&&(feedbackCPR[2] != 0)) {
            Log.d("CPR Feedback Calculation", "Calculated Feedback - Depth: ${feedbackCPR[0]}, Frequency: ${feedbackCPR[1]}, Angle: ${feedbackCPR[2]}")
            inputFeedback = feedbackCPR.copyOf()  // 이전 값 업데이트
            processFeedback()
        }
    }

    private fun processFeedback() {
        val priorityOrder = when (feedbackTurn) {
            1 -> listOf(0, 1, 2)  // 깊이, 빈도, 각도
            2 -> listOf(1, 0, 2)  // 빈도, 깊이, 각도
            3 -> listOf(2, 0, 1)  // 각도, 깊이, 빈도
            else -> listOf(0, 1, 2)  // 깊이, 빈도, 각도 (반복)
        }

        for (i in priorityOrder) {
            when (feedbackCPR[i]) {
                1 -> { // 나쁨 상태일 때
                    val message = when (i) {
                        0 -> "더 깊게 압박해주세요"  // 깊이 피드백
                        1 -> "압박 빈도가 느립니다"  // 빈도 피드백
                        2 -> "팔을 90도로 세워 주세요"  // 각도 피드백
                        else -> ""
                    }
                    giveFeedback(message)
                    break
                }
                3 -> { // 빈도가 빠름 상태일 때
                    if (i == 1) {  // 빈도 피드백에서만 "빠름" 메시지 제공
                        giveFeedback("압박 빈도가 빠릅니다")
                        break
                    }
                }
                2 -> { // 좋음 상태일 때
                    if (i == priorityOrder.last() && feedbackCPR.all { it == 2 }) {
                        Log.d("CPR Feedback", "All parameters are optimal")
                    }
                }
            }
        }

        feedbackTurn = (feedbackTurn % 3) + 1  // 턴 업데이트
    }

    private fun giveFeedback(message: String) {
        if (tts.isSpeaking) tts.stop()
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "CPR_FEEDBACK")
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
        // 이미 메트로놈이 실행 중일 경우 중복 실행 방지
        if (metronomeJob?.isActive == true) return

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
