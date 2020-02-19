package com.module.playways.party.room.view

import android.content.Context
import android.view.Gravity
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.R
import com.module.playways.grab.room.invite.IInviteCallBack
import com.module.playways.grab.room.invite.InviteFriendActivity
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.model.PartyActorInfoModel
import com.module.playways.room.data.H
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.live.proto.PartyRoom.EMicStatus
import com.zq.live.proto.PartyRoom.ESeatStatus
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus

/**
 *  下麦，关麦，查看信息，取消
 *  关闭座位，邀请上麦，取消
 *  打开座位，取消
 */
class PartyManageDialogView(context: Context, model: PartyActorInfoModel?, val inviteCallBack : IInviteCallBack) : ExConstraintLayout(context) {

    private val function1: ExTextView
    private val function2: ExTextView
    private val function3: ExTextView
    private val cancel: ExTextView

    private var mDialogPlus: DialogPlus? = null

    private var roomServer = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    init {
        View.inflate(context, R.layout.party_manage_dialog_view_layout, this)

        function1 = this.findViewById(R.id.function_1)
        function2 = this.findViewById(R.id.function_2)
        function3 = this.findViewById(R.id.function_3)
        cancel = this.findViewById(R.id.cancel)

        cancel.setDebounceViewClickListener { mDialogPlus?.dismiss() }

        if (model?.player != null) {
            // 麦上有人
            function1.visibility = View.VISIBLE
            function1.text = "下麦"
            function1.setDebounceViewClickListener {
                val map = HashMap<String, Any?>()
                map["roomID"] = H.partyRoomData?.gameId
                map["seatSeq"] = model.seat?.seatSeq
                map["seatUserID"] = model.seat?.userID
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                launch {
                    var result = subscribe(RequestControl("backSeat", ControlType.CancelThis)) {
                        roomServer.backSeat(body)
                    }
                    if (result.errno == 0) {
                        mDialogPlus?.dismiss()
                    } else {
                        U.getToastUtil().showShort(result.errmsg)
                    }
                }
            }

            function2.visibility = View.VISIBLE
            if (model?.seat?.micStatus == EMicStatus.MS_OPEN.value) {
                function2.text = "关麦"
            } else {
                function2.text = "开麦"
            }
            function2.setDebounceViewClickListener {
                val map = HashMap<String, Any?>()
                map["roomID"] = H.partyRoomData?.gameId
                map["seatSeq"] = model.seat?.seatSeq
                map["seatUserID"] = model.seat?.userID

                if (model?.seat?.micStatus == EMicStatus.MS_OPEN.value) {
                    map["micStatus"] = EMicStatus.MS_CLOSE.value
                } else {
                    map["micStatus"] = EMicStatus.MS_OPEN.value
                }
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                launch {
                    var result = subscribe(RequestControl("setUserMicStatus", ControlType.CancelThis)) {
                        roomServer.setUserMicStatus(body)
                    }
                    if (result.errno == 0) {
                        mDialogPlus?.dismiss()
                    } else {
                        U.getToastUtil().showShort(result.errmsg)
                    }
                }
            }

            function3.visibility = View.VISIBLE
            function3.text = "查看信息"
            function3.setDebounceViewClickListener {
                mDialogPlus?.dismiss(false) // 2个对话框
                EventBus.getDefault().post(ShowPersonCardEvent(model?.player?.userID ?: 0))
            }
        } else {
            // 麦上无人
            if (model?.seat?.seatStatus == ESeatStatus.SS_CLOSE.value) {
                // 已关闭的席位
                function1.visibility = View.VISIBLE
                function1.text = "打开座位"
                function2.visibility = View.GONE
                function3.visibility = View.GONE
            } else {
                //空席位
                function1.visibility = View.VISIBLE
                function1.text = "关闭座位"
                function2.visibility = View.VISIBLE
                function2.text = "邀请好友"
                function2.setDebounceViewClickListener {
                    mDialogPlus?.dismiss()
//                    ARouter.getInstance().build(RouterConstants.ACTIVITY_INVITE_FRIEND)
//                            .withInt("from", GameModeType.GAME_MODE_PARTY)
//                            .withInt("roomId", H.partyRoomData?.gameId ?: 0)
//                            .navigation()

                    InviteFriendActivity.open(inviteCallBack)
                }
                function3.visibility = View.GONE
            }

            function1.setDebounceViewClickListener {
                val map = HashMap<String, Any?>()
                map["roomID"] = H.partyRoomData?.gameId
                map["seatSeq"] = model?.seat?.seatSeq
                if (model?.seat?.seatStatus == ESeatStatus.SS_CLOSE.value) {
                    map["seatStatus"] = ESeatStatus.SS_OPEN.value
                } else {
                    map["seatStatus"] = ESeatStatus.SS_CLOSE.value
                }
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                launch {
                    var result = subscribe(RequestControl("setSeatStatus", ControlType.CancelThis)) {
                        roomServer.setSeatStatus(body)
                    }
                    if (result.errno == 0) {
                        mDialogPlus?.dismiss()
                    } else {
                        U.getToastUtil().showShort(result.errmsg)
                    }
                }
            }
        }
    }

    /**
     * 以后tips dialog 不要在外部单独写 dialog 了。
     * 可以不
     */
    fun showByDialog() {
        showByDialog(true)
    }

    fun showByDialog(canCancel: Boolean) {
        mDialogPlus?.dismiss(false)
        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(com.common.base.R.color.transparent)
                .setOverlayBackgroundResource(com.common.base.R.color.black_trans_80)
                .setExpanded(false)
                .setCancelable(canCancel)
                .create()
        mDialogPlus?.show()
    }

    fun dismiss() {
        mDialogPlus?.dismiss()
    }

    fun dismiss(isAnimation: Boolean) {
        mDialogPlus?.dismiss(isAnimation)
    }

    interface Listener {
    }
}