package com.component.person.utils

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.common.utils.dp
import com.common.view.ex.drawable.DrawableCreator
import com.component.person.model.RelationInfo

object RelationResUtils {

    fun getDesc(relationType: Int?): String? {
        return when (relationType) {
            RelationInfo.DJK_CP -> "CP"
            RelationInfo.DJK_Bai_Shi -> "师徒"
            RelationInfo.DJK_Shou_Tu -> "师徒"
            RelationInfo.DJK_Gui_Mi -> "闺蜜"
            RelationInfo.DJK_Ji_You -> "基友"
            else -> null
        }
    }

    fun getDrawable(relationType: Int?): Drawable? {
        return when (relationType) {
            RelationInfo.DJK_CP -> cpDrawable
            RelationInfo.DJK_Bai_Shi -> shituDrawable
            RelationInfo.DJK_Shou_Tu -> shituDrawable
            RelationInfo.DJK_Gui_Mi -> guimiDrawable
            RelationInfo.DJK_Ji_You -> jiyouDrawable
            else -> null
        }
    }

    // cp按钮背景
    val cpDrawable: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#FF8AB6"))
            .setCornersRadius(4.dp().toFloat())
            .build()

    // 师徒按钮背景
    val shituDrawable: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#B1AC99"))
            .setCornersRadius(4.dp().toFloat())
            .build()

    // 守护标签背景
    val guardDrawable: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#7088FF"))
            .setCornersRadius(4.dp().toFloat())
            .build()

    // 闺蜜标签背景
    val guimiDrawable: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#E55088"))
            .setCornersRadius(4.dp().toFloat())
            .build()

    // 基友标签背景
    val jiyouDrawable: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#61B14F"))
            .setCornersRadius(4.dp().toFloat())
            .build()
}