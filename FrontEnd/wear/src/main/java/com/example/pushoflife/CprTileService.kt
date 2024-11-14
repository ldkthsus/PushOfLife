package com.example.pushoflife

import android.content.Intent
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Layout
import androidx.wear.protolayout.expression.ProtoLayoutExperimental
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalHorologistApi::class)
class CprTileService : SuspendingTileService() {

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val tileData = loadTileData()

        // 타일 타임라인 설정: 이미지의 위치를 변화시키는 타임라인 엔트리 생성
        val timelineBuilder = TimelineBuilders.Timeline.Builder()
        val paddingOffsets = listOf(0, 10, 20, 10, 0, -10, -20, -10) // 움직임 시뮬레이션 단계

        // 각 단계마다 타임라인 엔트리를 추가하여 이미지 위치 조정
        paddingOffsets.forEach { offset ->
            val layout = tileLayout(tileData, offset)
            timelineBuilder.addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(layout)
                    .build()
            )
        }

        return TileBuilders.Tile.Builder()
            .setResourcesVersion("1")
            .setTileTimeline(timelineBuilder.build())
            .build()
    }

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion("1")
            .addIdToImageMapping(
                "cpr_icon",
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(R.drawable.fadingheart)
                            .build()
                    )
                    .build()
            )
            .addIdToImageMapping(
                "hand_icon",
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(R.drawable.touch_app)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private suspend fun loadTileData(): String = withContext(Dispatchers.IO) {
        "PushOfLife"
    }

    // 타임라인 엔트리에 offset 적용
    @androidx.annotation.OptIn(ProtoLayoutExperimental::class)
    private fun tileLayout(text: String, verticalOffset: Int): Layout {
        val launchAction = ActionBuilders.LaunchAction.Builder()
            .setAndroidActivity(
                ActionBuilders.AndroidActivity.Builder()
                    .setPackageName(packageName)
                    .setClassName(MainActivity::class.java.name)
                    .build()
            )
            .build()

        val modifiers = ModifiersBuilders.Modifiers.Builder()
            .setClickable(
                ModifiersBuilders.Clickable.Builder()
                    .setId("open_app")
                    .setOnClick(launchAction)
                    .build()
            )
            .build()

        return Layout.Builder()
            .setRoot(
                LayoutElementBuilders.Box.Builder()
                    .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
                    .setWidth(DimensionBuilders.expand())
                    .setHeight(DimensionBuilders.expand())
                    .addContent(
                        LayoutElementBuilders.Image.Builder()
                            .setResourceId("cpr_icon")
                            .setWidth(DimensionBuilders.dp(128f))
                            .setHeight(DimensionBuilders.dp(128f))
                            .build()
                    )
                    .addContent(
                        // 두 번째 이미지: offset에 따라 이동하는 슬라이드 효과 시뮬레이션
                        LayoutElementBuilders.Image.Builder()
                            .setResourceId("hand_icon")
                            .setWidth(DimensionBuilders.dp(122f))
                            .setHeight(DimensionBuilders.dp(122f))
                            .setModifiers(
                                ModifiersBuilders.Modifiers.Builder()
                                    .setPadding(
                                        ModifiersBuilders.Padding.Builder()
                                            .setTop(DimensionBuilders.dp(88f + verticalOffset))
                                            .setStart(DimensionBuilders.dp(56f))
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .addContent(
                        LayoutElementBuilders.Text.Builder()
                            .setText(text)
                            .setModifiers(modifiers)
                            .build()
                    )
                    .build()
            )
            .build()
    }
}