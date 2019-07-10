package com.module.playways.doubleplay.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.drawable.DrawableCreator
import com.module.playways.R
import com.zq.mediaengine.kit.ZqEngineKit

class DoubleChatSenceView : ConstraintLayout {
    val mMicIv: ExImageView

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflate(context, com.module.playways.R.layout.double_chat_sence_layout, this)
        mMicIv = findViewById(com.module.playways.R.id.mute_mic_iv)
    }

    fun joinAgora() {
        MyLog.d("DoubleChatSenceView", "joinAgora")
        val drawable = DrawableCreator.Builder()
                .setSelectedDrawable(U.getDrawable(R.drawable.skr_jingyin_able))
                .setUnSelectedDrawable(U.getDrawable(R.drawable.srf_bimai))
                .build()
        mMicIv?.background = drawable

        mMicIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 开关麦克
                val isSelected = mMicIv?.isSelected
                ZqEngineKit.getInstance().muteLocalAudioStream(!isSelected)
                mMicIv?.setSelected(!isSelected)
            }
        })
    }

    fun selected() {
        mMicIv?.setSelected(ZqEngineKit.getInstance().params.isLocalAudioStreamMute)
    }

    fun setData() {

    }

    fun destroy() {

    }
}