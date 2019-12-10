package com.module.playways.party.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.event.PartyApplyUserCntChangeEvent
import com.module.playways.party.room.event.PartyMySeatInfoChangeEvent
import com.module.playways.party.room.event.PartyMyUserInfoChangeEvent
import com.module.playways.room.data.H
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PartyRightOpView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr), CoroutineScope by MainScope() {

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    private val applyList: ExTextView
    private val opMicTv: ExTextView

    var listener: Listener? = null

    val mic_status_unapply = 1 // 未申请
    val mic_status_wating = 2  // 申请中
    val mic_status_online = 3  // 在麦上

    var micStatus = mic_status_online  // 默认

    private val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    init {
        View.inflate(context, R.layout.party_right_op_view_layout, this)

        applyList = this.findViewById(R.id.apply_list)
        opMicTv = this.findViewById(R.id.op_mic_tv)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        applyList.setDebounceViewClickListener {
            listener?.onClickApplyList()
        }

        opMicTv.setDebounceViewClickListener {
            // 申请 取消 下麦
            when (micStatus) {
                mic_status_unapply -> {
                    applyForGuest(false)
                }
                mic_status_wating -> {
                    applyForGuest(true)
                }
                else -> {
                    backSeat()
                }
            }
        }
    }

    // 申请或取消申请上麦 cancel为true，取消申请 cancel为false，为申请嘉宾
    private fun applyForGuest(cancel: Boolean) {
        launch {
            val map = mutableMapOf(
                    "cancel" to cancel,
                    "roomID" to H.partyRoomData?.gameId
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("applyForGuest", ControlType.CancelThis)) {
                roomServerApi.applyForGuest(body)
            }
            if (result.errno == 0) {
                micStatus = if (cancel) {
                    mic_status_unapply
                } else {
                    mic_status_wating
                }
                refreshMicStatus()
            } else {

            }
        }
    }

    private fun backSeat() {
        launch {
            val map = mutableMapOf(
                    "seatSeq" to H.partyRoomData?.getSeatInfoByUserId(MyUserInfoManager.uid.toInt())?.seatSeq,
                    "seatUserID" to MyUserInfoManager.uid,
                    "roomID" to H.partyRoomData?.gameId
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("backSeat", ControlType.CancelThis)) {
                roomServerApi.backSeat(body)
            }
            if (result.errno == 0) {
                micStatus = mic_status_unapply
                refreshMicStatus()
            } else {

            }
        }
    }

    // 身份改变需要重置
    fun bindData() {
        val myInfo = H.partyRoomData?.getMyUserInfoInParty()
        when {
            myInfo?.isHost() == true -> {
                // 主持人
                micStatus = mic_status_unapply
                applyList.visibility = View.VISIBLE
                opMicTv.visibility = View.GONE
            }
            myInfo?.isGuest() == true -> {
                // 嘉宾
                micStatus = mic_status_online
                opMicTv.visibility = View.VISIBLE
                if (myInfo?.isAdmin()) {
                    applyList.visibility = View.VISIBLE
                } else {
                    applyList.visibility = View.GONE
                }
            }
            else -> {
                // 观众
                micStatus = mic_status_unapply
                opMicTv.visibility = View.VISIBLE
                if (myInfo?.isAdmin() == true) {
                    applyList.visibility = View.VISIBLE
                } else {
                    applyList.visibility = View.GONE
                }
            }
        }
        applyList.text = "申请${H.partyRoomData?.applyUserCnt}人"
        refreshMicStatus()
    }

    private fun refreshMicStatus() {
        when (micStatus) {
            mic_status_unapply -> {
                opMicTv.text = "申请上麦"
            }
            mic_status_wating -> {
                opMicTv.text = "取消申请"
            }
            mic_status_online -> {
                opMicTv.text = "下麦"
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyMyUserInfoChangeEvent) {
        bindData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyApplyUserCntChangeEvent) {
        applyList.text = "申请${H.partyRoomData?.applyUserCnt}人"
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        cancel()
    }

    interface Listener {
        fun onClickApplyList()
    }
}