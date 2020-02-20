package com.component.person.utils

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.R
import com.component.person.model.RelationInfo

object RelationResUtils {

    fun getDesc(relationType: Int?): String? {
        return when (relationType) {
            RelationInfo.DJK_CP -> "CP"
            RelationInfo.DJK_Bai_Shi -> "徒弟"
            RelationInfo.DJK_Shou_Tu -> "师傅"
            RelationInfo.DJK_Gui_Mi -> "闺蜜"
            RelationInfo.DJK_Ji_You -> "基友"
            else -> null
        }
    }

    fun getDrawable(relationType: Int?): Drawable? {
        return when (relationType) {
            RelationInfo.DJK_CP -> U.getDrawable(R.drawable.relation_cp_icon)
            RelationInfo.DJK_Bai_Shi -> U.getDrawable(R.drawable.relation_tudi_icon)
            RelationInfo.DJK_Shou_Tu -> U.getDrawable(R.drawable.relation_shifu_icon)
            RelationInfo.DJK_Gui_Mi -> U.getDrawable(R.drawable.relation_guimi_icon)
            RelationInfo.DJK_Ji_You -> U.getDrawable(R.drawable.relation_jiyou_icon)
            else -> null
        }
    }
}