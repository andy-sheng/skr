package com.module.playways.mic.room.top

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.common.core.view.setDebounceViewClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.mic.room.MicRoomData

class MicTopOpView : RelativeLayout {
    private val mIvSetting: ImageView
    private val mIvVoiceSetting: ImageView
    private val mGameRuleIv: ImageView
    private val mFeedBackIv: ImageView
    private val mExitTv: ExTextView

    private var mListener: Listener? = null
    private var mRoomData: MicRoomData? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    fun setListener(listener: Listener) {
        mListener = listener
    }

    init {
        View.inflate(context, R.layout.mic_top_op_view, this)
        mGameRuleIv = findViewById(R.id.game_rule_iv)
        mIvSetting = findViewById(R.id.game_setting)
        mFeedBackIv = findViewById(R.id.feed_back_iv)
        mExitTv = findViewById(R.id.exit_tv)
        mIvVoiceSetting = findViewById<View>(R.id.iv_voice_setting) as ImageView

        mIvSetting.setDebounceViewClickListener {
            mListener?.onClickSetting()
        }

        mIvVoiceSetting.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mListener?.onClickVoiceAudition()
            }
        })

        mGameRuleIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mListener?.onClickGameRule()
            }
        })

        mFeedBackIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mListener?.onClickFeedBack()
            }
        })

        mExitTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mListener?.closeBtnClick()
            }
        })
    }

    fun setRoomData(roomData: MicRoomData) {
        mRoomData = roomData
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    interface Listener {
        fun closeBtnClick()

        fun onClickGameRule()

        fun onClickFeedBack()

        fun onClickVoiceAudition()

        fun onClickSetting();
    }
}
