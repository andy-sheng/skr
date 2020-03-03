package com.module.playways.battle.room.top

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.common.core.view.setDebounceViewClickListener
import com.common.statistics.StatisticsAdapter
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.mic.room.MicRoomData
import com.module.playways.party.room.event.PartyMyUserInfoChangeEvent
import com.module.playways.room.data.H
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BattleTopOpView : ConstraintLayout {

    private val feedBackIv: ImageView
    private val ruleIv: ImageView
    private val voiceSettingIv: ImageView
    private val exitIv: ImageView

    private var mListener: Listener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    fun setListener(listener: Listener) {
        mListener = listener
    }

    init {
        View.inflate(context, R.layout.battle_top_op_view, this)


        feedBackIv = this.findViewById(R.id.feed_back_iv)
        ruleIv = this.findViewById(R.id.rule_iv)
        voiceSettingIv = this.findViewById(R.id.voice_setting_iv)
        exitIv = this.findViewById(R.id.exit_iv)


        feedBackIv.setDebounceViewClickListener {
            mListener?.onClickFeedBack()
        }

        ruleIv.setDebounceViewClickListener {
            mListener?.onClickGameRule()
        }

        voiceSettingIv.setDebounceViewClickListener {
            mListener?.onClickVoiceAudition()
        }

        exitIv.setDebounceViewClickListener {
            mListener?.closeBtnClick()
        }

    }

    fun bindData() {

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    interface Listener {

        fun onClickFeedBack()

        fun onClickGameRule()

        fun onClickVoiceAudition()

        fun closeBtnClick()
    }
}
