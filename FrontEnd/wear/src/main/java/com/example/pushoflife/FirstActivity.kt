package com.example.pushoflife

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pushoflife.network.MessageSender
import com.example.pushoflife.ui.SecondScreen
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import androidx.compose.runtime.mutableStateOf
class FirstActivity : ComponentActivity() {
    private lateinit var messageSender: MessageSender
    private val nodeIdState = mutableStateOf<String?>(null)  // nodeId를 상태로 관리
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 화면 밝기를 최대로 설정
        window.attributes = window.attributes.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        }

        // 화면이 절대 꺼지지 않도록 설정
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // MessageSender 초기화
        messageSender = MessageSender(applicationContext)

        // nodeId 가져오기 비동기 작업
        getConnectedNodeId { id ->
            nodeIdState.value = id  // nodeId 상태 업데이트
            Log.d("NodeID", "Connected node ID: ${nodeIdState.value}")
        }
        // Compose UI 설정
        setContent {
            if (nodeIdState.value == null) {
//                // 로딩 화면을 보여줌
//                LoadingScreen()
            } else {
                // nodeId가 설정된 후 SecondScreen을 표시
                SecondScreen(
                    nodeId = nodeIdState.value,
                    messageSender = messageSender,
                    onNavigateToCprGuide = {
                        val intent = Intent(this, CprGuideActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
    // 연결된 노드 ID를 가져오는 메서드
    private fun getConnectedNodeId(onNodeIdReceived: (String?) -> Unit) {
        val nodeClient: NodeClient = Wearable.getNodeClient(this)
        val nodeListTask: Task<List<Node>> = nodeClient.connectedNodes

        nodeListTask.addOnSuccessListener { nodes ->
            val nodeId = nodes.firstOrNull()?.id
            onNodeIdReceived(nodeId)
        }.addOnFailureListener {
            Log.e("NodeID", "Failed to get connected node ID", it)
            onNodeIdReceived(null)
        }
    }
}
