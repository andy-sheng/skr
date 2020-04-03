package com.module.playways.party.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.event.*
import com.module.playways.party.room.model.PartyPlayerInfoModel
import com.module.playways.room.data.H
import com.zq.live.proto.PartyRoom.EGetSeatMode
import com.zq.live.proto.PartyRoom.PApplyForGuest
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

    val MIC_STATUS_UNAPPLY = 1 // 未申请
    val MIC_STATUS_WATING = 2  // 申请中
    val MIC_STATUS_ONLINE = 3  // 在麦上

    var micStatus = MIC_STATUS_ONLINE  // 默认

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
                MIC_STATUS_UNAPPLY -> {
                    if (H.partyRoomData?.getSeatMode == EGetSeatMode.EGSM_NO_APPLY.value) {
                        selfGetSeat()
                    } else {
                        applyForGuest(false)
                    }

                }
                MIC_STATUS_WATING -> {
                    // 取消申请
                    applyForGuest(true)
                }
                else -> {
                    backSeat()
                }
            }
        }
    }

    fun selfGetSeat() {
        launch {
            val map = mutableMapOf(
                    "roomID" to H.partyRoomData?.gameId
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("selfGetSeat", ControlType.CancelThis)) {
                roomServerApi.selfGetSeat(body)
            }
            if (result.errno == 0) {
                // todo 看本地是否要做即时的更新
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    // 申请或取消申请上麦 cancel为true，取消申请 cancel为false，为申请嘉宾
    fun applyForGuest(cancel: Boolean) {
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
                    MIC_STATUS_UNAPPLY
                } else {
                    MIC_STATUS_WATING
                }
                refreshMicStatus("applyForGuest")
            } else {
                U.getToastUtil().showShort(result.errmsg)
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
                micStatus = MIC_STATUS_UNAPPLY
                refreshMicStatus("backSeat")
            } else {

            }
        }
    }

    // 身份改变需要重置
    fun bindData(isInit: Boolean, oldUser: PartyPlayerInfoModel?) {
        val myInfo = H.partyRoomData?.getMyUserInfoInParty()
        when {
            myInfo?.isHost() == true -> {
                // 主持人
                micStatus = MIC_STATUS_UNAPPLY
                if (H.partyRoomData?.getSeatMode == EGetSeatMode.EGSM_NO_APPLY.value) {
                    // 自由上麦
                    applyList.visibility = View.GONE
                } else {
                    applyList.visibility = View.VISIBLE
                }
                opMicTv.visibility = View.GONE
            }
            myInfo?.isGuest() == true -> {
                // 嘉宾
                micStatus = MIC_STATUS_ONLINE
                opMicTv.visibility = View.VISIBLE
                if (H.partyRoomData?.getSeatMode == EGetSeatMode.EGSM_NO_APPLY.value) {
                    // 自由上麦
                    applyList.visibility = View.GONE
                } else {
                    if (myInfo?.isAdmin()) {
                        applyList.visibility = View.VISIBLE
                    } else {
                        applyList.visibility = View.GONE
                    }
                }
            }
            else -> {
                if (!isInit) {
                    if (oldUser?.isGuest() == true || oldUser?.isHost() == true) {
                        // 不是初始化 之前是嘉宾或者主持
                        micStatus = MIC_STATUS_UNAPPLY
                    }
                } else {
                    micStatus = MIC_STATUS_UNAPPLY
                }
                opMicTv.visibility = View.VISIBLE
                if (H.partyRoomData?.getSeatMode == EGetSeatMode.EGSM_NO_APPLY.value) {
                    // 自由上麦
                    applyList.visibility = View.GONE
                } else {
                    if (myInfo?.isAdmin() == true) {
                        applyList.visibility = View.VISIBLE
                    } else {
                        applyList.visibility = View.GONE
                    }
                }
            }
        }
        applyList.text = SpanUtils().append("麦序管理").setFontSize(10, true).append("\n${H.partyRoomData?.applyUserCnt}人申请").setFontSize(8, true).create()
        refreshMicStatus("bindData isInit=$isInit")
    }

    private fun refreshMicStatus(from: String) {
        MyLog.d("PartyRightOpView", "refreshMicStatus from = $from")
        when (micStatus) {
            MIC_STATUS_UNAPPLY -> {
                if (H.partyRoomData?.getSeatMode == EGetSeatMode.EGSM_NO_APPLY.value) {
                    // 自由上麦
                    opMicTv.text = "直接上麦"
                } else {
                    opMicTv.text = "申请上麦"
                }
            }
            MIC_STATUS_WATING -> {
                opMicTv.text = "取消申请"
            }
            MIC_STATUS_ONLINE -> {
                opMicTv.text = "下麦"
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyRoomSeatModeChangeEvent) {
        // 上麦的方式改变了，重新绑定数据
        bindData(true, null)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyMyUserInfoChangeEvent) {
        bindData(false, event.oldUser)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyHostChangeEvent) {
        if (H.partyRoomData?.hostId == 0) {
            micStatus = MIC_STATUS_UNAPPLY
            refreshMicStatus("PartyHostChangeEvent")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyApplyUserCntChangeEvent) {
        applyList.text = "申请${H.partyRoomData?.applyUserCnt}人"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PApplyForGuest) {
        if (event.cancel && event.user.userInfo.userID == MyUserInfoManager.uid.toInt()) {
            // 自己申请被取消,不管是被谁取消的，都要重置一下
            micStatus = MIC_STATUS_UNAPPLY
            refreshMicStatus("PApplyForGuest")
        }
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