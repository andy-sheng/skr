package com.module.playways.doubleplay.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.view.ex.ExImageView


class DoubleGameSenceView(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    val mShowCard: DoubleSingCardView
    val mMicIv: ExImageView
    val mPickIv: ImageView
    val mSelectIv: ImageView
    val mMicTv: TextView

    init {
        View.inflate(context, com.module.playways.R.layout.double_game_sence_layout, this)

        mShowCard = findViewById(com.module.playways.R.id.show_card)
        mMicIv = findViewById(com.module.playways.R.id.mic_iv)
        mPickIv = findViewById(com.module.playways.R.id.pick_iv) as ImageView
        mSelectIv = findViewById(com.module.playways.R.id.select_iv) as ImageView
        mMicTv = findViewById(com.module.playways.R.id.mic_tv)
    }

    fun setData() {

    }
}