package com.module.playways.doubleplay.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.common.view.ex.ExImageView

class DoubleChatSenceView(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    val mMuteMicIv: ExImageView

    init {
        View.inflate(context, com.module.playways.R.layout.double_chat_sence_layout, this)
        mMuteMicIv = findViewById(com.module.playways.R.id.mute_mic_iv)
    }

    fun setData() {

    }
}