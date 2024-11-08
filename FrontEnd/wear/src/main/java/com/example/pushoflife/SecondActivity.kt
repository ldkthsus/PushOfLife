package com.example.pushoflife

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pushoflife.ui.SecondScreen
import com.example.pushoflife.network.MessageSender
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable

class SecondActivity : ComponentActivity() {

    private lateinit var messageSender: MessageSender
    private var nodeId: String? = null

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

        // nodeId 가져오기
        getConnectedNodeId { id ->
            nodeId = id
            Log.d("NodeID", "Second Connected node ID: $nodeId")
        }

        // Compose UI 설정
        setContent {
            SecondScreen(
                nodeId = nodeId,
                messageSender = messageSender,
                onNavigateToCprGuide = {
                    val intent = Intent(this, CprGuideActivity::class.java)
                    startActivity(intent)
                }
            )
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
