package com.module.feeds.make.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.feeds.R


class VocalAlignControlPannelView(context: Context?, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    val mVaResetTv: ExTextView
    val mInfoTipsTv: ExTextView
    val mLeftIv: ExImageView
    val mRightIv: ExImageView
    val mVaSeekBar: SeekBar
    init {
        View.inflate(context, R.layout.feeds_editor_vocal_align_control_pannel_layout,this)
        mVaResetTv = this.findViewById(R.id.va_reset_tv)
        mInfoTipsTv = this.findViewById(R.id.info_tips_tv)
        mLeftIv = this.findViewById(R.id.left_iv)
        mRightIv = this.findViewById(R.id.right_iv)
        mVaSeekBar = this.findViewById(R.id.va_seek_bar)
    }


}