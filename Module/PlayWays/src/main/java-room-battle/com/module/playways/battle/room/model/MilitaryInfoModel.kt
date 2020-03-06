package com.module.playways.battle.room.model

import android.graphics.drawable.Drawable
import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField
import com.common.utils.U
import com.module.playways.R


// 这一期只做展示，评价体系
class MilitaryInfoModel : Serializable {

    @JSONField(name = "currBar")
    var currBar: Int? = null
    @JSONField(name = "maxBar")
    var maxBar: Int? = null
    @JSONField(name = "nextTitle")
    var nextTitle: String? = null
    @JSONField(name = "title")
    var title: String? = null
    @JSONField(name = "titleIndex")
    var titleIndex: Int? = null
    @JSONField(name = "totalScore")
    var totalScore: Int? = null

    fun getSmallDrawable(index: Int): Drawable? {
        return when (index) {
            1 -> U.getDrawable(R.drawable.military_index_1)
            2 -> U.getDrawable(R.drawable.military_index_2)
            3 -> U.getDrawable(R.drawable.military_index_3)
            4 -> U.getDrawable(R.drawable.military_index_4)
            5 -> U.getDrawable(R.drawable.military_index_5)
            6 -> U.getDrawable(R.drawable.military_index_6)
            7 -> U.getDrawable(R.drawable.military_index_7)
            8 -> U.getDrawable(R.drawable.military_index_8)
            9 -> U.getDrawable(R.drawable.military_index_9)
            10 -> U.getDrawable(R.drawable.military_index_10)
            11 -> U.getDrawable(R.drawable.military_index_11)
            12 -> U.getDrawable(R.drawable.military_index_12)
            else -> null
        }
    }
}