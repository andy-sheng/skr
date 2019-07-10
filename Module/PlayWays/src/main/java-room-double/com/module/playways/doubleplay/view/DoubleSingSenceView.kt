package com.module.playways.doubleplay.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.view.ex.ExImageView


class DoubleSingSenceView(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    val mShowCard1: DoubleSingCardView
    val mShowCard2: DoubleSingCardView
    val mMicIv: ExImageView
    val mPickIv: ImageView
    val mSelectIv: ImageView
    val mMicTv: TextView

    init {
        View.inflate(context, com.module.playways.R.layout.double_sing_sence_layout, this)
        mShowCard1 = findViewById(com.module.playways.R.id.show_card1)
        mShowCard2 = findViewById(com.module.playways.R.id.show_card2)
        mMicIv = findViewById(com.module.playways.R.id.mic_iv)
        mPickIv = findViewById(com.module.playways.R.id.pick_iv) as ImageView
        mSelectIv = findViewById(com.module.playways.R.id.select_iv) as ImageView
        mMicTv = findViewById(com.module.playways.R.id.mic_tv)
    }

    fun setData() {

    }
}