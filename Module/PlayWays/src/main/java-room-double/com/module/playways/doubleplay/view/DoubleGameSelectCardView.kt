package com.module.playways.doubleplay.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView

class DoubleGameSelectCardView(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    val mGameNameTv: ExTextView
    val mIconIv1: ExImageView
    val mIconIv2: ExImageView

    init {
        View.inflate(context, com.module.playways.R.layout.double_gameselect_card_layout, this)
        mGameNameTv = findViewById(com.module.playways.R.id.game_name_tv)
        mIconIv1 = findViewById(com.module.playways.R.id.icon_iv_1)
        mIconIv2 = findViewById(com.module.playways.R.id.icon_iv_2)
    }

    fun setData() {

    }
}