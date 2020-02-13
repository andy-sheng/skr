package com.module.playways.party.room.top

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

class PartyTopOpView2 : ConstraintLayout {

    var partyInviteIv:ExTextView
    var changeRoomIv:ExTextView
    var reportRoomIv:ExTextView
    var feedBackIv:ImageView
    var voiceSettingIv:ImageView
    var exitIv:ImageView

    private var mListener: Listener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    fun setListener(listener: Listener) {
        mListener = listener
    }

    init {
        View.inflate(context, R.layout.party_top_op_view2, this)
        partyInviteIv = rootView.findViewById(R.id.party_invite_iv)
        changeRoomIv = rootView.findViewById(R.id.change_room_iv)
        reportRoomIv = rootView.findViewById(R.id.report_room_iv)
        feedBackIv = rootView.findViewById(R.id.feed_back_iv)
        voiceSettingIv = rootView.findViewById(R.id.voice_setting_iv)
        exitIv = rootView.findViewById(R.id.exit_iv)


        partyInviteIv.setDebounceViewClickListener {
            StatisticsAdapter.recordCountEvent("party", "top_invite", null)
            mListener?.onClickInviteRoom()
        }

        changeRoomIv.setDebounceViewClickListener {
            StatisticsAdapter.recordCountEvent("party", "change_room", null)
            mListener?.onClickChangeRoom()
        }
        reportRoomIv.setDebounceViewClickListener {
            mListener?.onClickRoomReport()
        }

//        mIvSetting.setDebounceViewClickListener {
//            mListener?.onClickSetting()
//        }

        voiceSettingIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mListener?.onClickVoiceAudition()
            }
        })

//        mGameRuleIv.setOnClickListener(object : DebounceViewClickListener() {
//            override fun clickValid(v: View) {
//                mListener?.onClickGameRule()
//            }
//        })

        feedBackIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mListener?.onClickFeedBack()
            }
        })

        exitIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mListener?.closeBtnClick()
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event:PartyMyUserInfoChangeEvent){
        if(H.partyRoomData?.myUserInfo?.isHost()==true ||
                H.partyRoomData?.myUserInfo?.isGuest()==true){
            changeRoomIv.visibility = View.GONE
        }else{
            changeRoomIv.visibility = View.VISIBLE
        }
    }

    interface Listener {
        fun closeBtnClick()

        fun onClickGameRule()

        fun onClickFeedBack()

        fun onClickVoiceAudition()

        fun onClickSetting()

        fun onClickRoomReport()

        fun onClickChangeRoom()

        fun onClickInviteRoom()
    }
}
