package com.module.feeds.make.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.feeds.R
import com.zq.mediaengine.kit.ZqAudioEditorKit


class VocalAlignControlPannelView(context: Context?, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    lateinit var audioEditorKit: ZqAudioEditorKit

    val mVaResetTv: ExTextView
    val mInfoTipsTv: ExTextView
    val mLeftIv: ExImageView
    val mRightIv: ExImageView
    val mVaSeekBar: SeekBar

    init {
        View.inflate(context, R.layout.feeds_editor_vocal_align_control_pannel_layout, this)
        mVaResetTv = this.findViewById(R.id.va_reset_tv)
        mInfoTipsTv = this.findViewById(R.id.info_tips_tv)
        mLeftIv = this.findViewById(R.id.left_iv)
        mRightIv = this.findViewById(R.id.right_iv)
        mVaSeekBar = this.findViewById(R.id.va_seek_bar)
        mLeftIv.setOnClickListener {
            mVaSeekBar?.progress = mVaSeekBar.progress - 5
            updateProgress()
        }
        mRightIv.setOnClickListener {
            mVaSeekBar?.progress = mVaSeekBar.progress + 5
            updateProgress()
        }
        mVaResetTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                mVaSeekBar?.progress = 50
                updateProgress()
            }
        })
        mVaSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                updateProgress()
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val ts = (progress - 50) * 20
                if (ts < 0) {
                    mInfoTipsTv?.text = "人声提前${-ts}毫秒"
                } else {
                    mInfoTipsTv?.text = "人声延后${ts}毫秒"
                }
            }
        })
    }

    fun updateProgress() {
        val ts = ((mVaSeekBar.progress ?: 0) - 50) * 20
        audioEditorKit.setDelay(1, ts.toLong())
        if (ts < 0) {
            mInfoTipsTv?.text = "人声提前${-ts}毫秒"
        } else {
            mInfoTipsTv?.text = "人声延后${ts}毫秒"
        }
    }

    fun bindData() {
        val ts = audioEditorKit.getDelay(1)
        mVaSeekBar.progress = (ts / 20 + 50).toInt()
        if (ts < 0) {
            mInfoTipsTv?.text = "人声提前${-ts}毫秒"
        } else {
            mInfoTipsTv?.text = "人声延后${ts}毫秒"
        }
    }

}