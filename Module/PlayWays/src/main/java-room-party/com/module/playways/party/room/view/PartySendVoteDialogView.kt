package com.module.playways.party.room.view

import android.content.Context
import android.view.Gravity
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.core.avatar.AvatarUtils
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.model.PartyPlayerInfoModel
import com.module.playways.room.data.H
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

/**
 *  下麦，关麦，查看信息，取消
 *  关闭座位，邀请上麦，取消
 *  打开座位，取消
 */
class PartySendVoteDialogView(context: Context) : ExConstraintLayout(context) {
    private val PRE_SCOPE_KEY = "scope_vote"

    private var mDialogPlus: DialogPlus? = null
    var inMicTv: ExTextView
    var inMicSelectedIv: ExImageView
    var allManTv: ExTextView
    var allManSelectedIv: ExImageView
    var tipsTv: ExTextView
    var seat1Text: ExTextView
    var seat1Avatar: BaseImageView
    var seat1Name: ExTextView
    var seat1SelecedIv: ExImageView
    var seat2Text: ExTextView
    var seat2Avatar: BaseImageView
    var seat2Name: ExTextView
    var seat2SelecedIv: ExImageView
    var seat3Text: ExTextView
    var seat3Avatar: BaseImageView
    var seat3Name: ExTextView
    var seat3SelecedIv: ExImageView
    var seat4Text: ExTextView
    var seat4Avatar: BaseImageView
    var seat4Name: ExTextView
    var seat4SelecedIv: ExImageView
    var seat5Text: ExTextView
    var seat5Avatar: BaseImageView
    var seat5Name: ExTextView
    var seat5SelecedIv: ExImageView
    var seat6Text: ExTextView
    var seat6Avatar: BaseImageView
    var seat6Name: ExTextView
    var seat6SelecedIv: ExImageView
    var sendIv: ExTextView
    var exitTv: ExImageView

    val guestNameList: ArrayList<ExTextView> = ArrayList(6)
    val guestAvatarList: ArrayList<BaseImageView> = ArrayList(6)
    val guestSelectedImgList: ArrayList<ExImageView> = ArrayList(6)

    val selectedSeatIndex: ArrayList<Int> = ArrayList(2)
    var selectedSendMode: Int = 2

    private val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    init {
        View.inflate(context, R.layout.party_send_vote_view_layout, this)
        inMicTv = rootView.findViewById(R.id.in_mic_tv)
        inMicSelectedIv = rootView.findViewById(R.id.in_mic_selected_iv)
        allManTv = rootView.findViewById(R.id.all_man_tv)
        allManSelectedIv = rootView.findViewById(R.id.all_man_selected_iv)
        tipsTv = rootView.findViewById(R.id.tips_tv)
        seat1Text = rootView.findViewById(R.id.seat_1_text)
        seat1Avatar = rootView.findViewById(R.id.seat_1_avatar)
        seat1Name = rootView.findViewById(R.id.seat_1_name)
        seat1SelecedIv = rootView.findViewById(R.id.seat_1_seleced_iv)
        seat2Text = rootView.findViewById(R.id.seat_2_text)
        seat2Avatar = rootView.findViewById(R.id.seat_2_avatar)
        seat2Name = rootView.findViewById(R.id.seat_2_name)
        seat2SelecedIv = rootView.findViewById(R.id.seat_2_seleced_iv)
        seat3Text = rootView.findViewById(R.id.seat_3_text)
        seat3Avatar = rootView.findViewById(R.id.seat_3_avatar)
        seat3Name = rootView.findViewById(R.id.seat_3_name)
        seat3SelecedIv = rootView.findViewById(R.id.seat_3_seleced_iv)
        seat4Text = rootView.findViewById(R.id.seat_4_text)
        seat4Avatar = rootView.findViewById(R.id.seat_4_avatar)
        seat4Name = rootView.findViewById(R.id.seat_4_name)
        seat4SelecedIv = rootView.findViewById(R.id.seat_4_seleced_iv)
        seat5Text = rootView.findViewById(R.id.seat_5_text)
        seat5Avatar = rootView.findViewById(R.id.seat_5_avatar)
        seat5Name = rootView.findViewById(R.id.seat_5_name)
        seat5SelecedIv = rootView.findViewById(R.id.seat_5_seleced_iv)
        seat6Text = rootView.findViewById(R.id.seat_6_text)
        seat6Avatar = rootView.findViewById(R.id.seat_6_avatar)
        seat6Name = rootView.findViewById(R.id.seat_6_name)
        seat6SelecedIv = rootView.findViewById(R.id.seat_6_seleced_iv)
        sendIv = rootView.findViewById(R.id.send_iv)
        exitTv = rootView.findViewById(R.id.exit_tv)

        guestNameList.add(seat1Name)
        guestNameList.add(seat2Name)
        guestNameList.add(seat3Name)
        guestNameList.add(seat4Name)
        guestNameList.add(seat5Name)
        guestNameList.add(seat6Name)

        guestAvatarList.add(seat1Avatar)
        guestAvatarList.add(seat2Avatar)
        guestAvatarList.add(seat3Avatar)
        guestAvatarList.add(seat4Avatar)
        guestAvatarList.add(seat5Avatar)
        guestAvatarList.add(seat6Avatar)

        guestSelectedImgList.add(seat1SelecedIv)
        guestSelectedImgList.add(seat2SelecedIv)
        guestSelectedImgList.add(seat3SelecedIv)
        guestSelectedImgList.add(seat4SelecedIv)
        guestSelectedImgList.add(seat5SelecedIv)
        guestSelectedImgList.add(seat6SelecedIv)

        inMicTv.setDebounceViewClickListener {
            inMicSelectedIv.visibility = View.VISIBLE
            allManSelectedIv.visibility = View.GONE
            selectedSendMode = 1
        }

        allManTv.setDebounceViewClickListener {
            inMicSelectedIv.visibility = View.GONE
            allManSelectedIv.visibility = View.VISIBLE
            selectedSendMode = 2
        }

        selectedSendMode = U.getPreferenceUtils().getSettingInt(PRE_SCOPE_KEY, 2)
        if (selectedSendMode == 2) {
            inMicSelectedIv.visibility = View.GONE
            allManSelectedIv.visibility = View.VISIBLE
        } else {
            inMicSelectedIv.visibility = View.VISIBLE
            allManSelectedIv.visibility = View.GONE
        }

        sendIv.setDebounceViewClickListener {
            if (selectedSeatIndex.size >= 2) {
                sendVote()
            } else {
                U.getToastUtil().showShort("请至少选择2位被投票嘉宾吧")
            }
        }

        exitTv.setDebounceViewClickListener {
            mDialogPlus?.dismiss(false)
        }

        setData()
    }

    private fun setData() {
        guestSelectedImgList.forEach {
            it.visibility = View.GONE
        }

        guestAvatarList.forEachIndexed { index, baseImageView ->
            baseImageView.setOnClickListener(null)
            baseImageView.setTag(null)
            H.partyRoomData?.getSeatInfoMap()?.get(index + 1)?.player?.let {
                setClickListener(baseImageView, index + 1)
            }
        }

        H.partyRoomData?.getSeatInfoMap()?.forEach {
            showGuestInfo(it.key, it.value.player)
        }
    }

    private fun sendVote() {
        val map = HashMap<String, Any?>()
        map["roomID"] = H.partyRoomData?.gameId
        map["scope"] = selectedSendMode
        U.getPreferenceUtils().setSettingInt(PRE_SCOPE_KEY, selectedSendMode)
        val list = ArrayList<Int>()
        selectedSeatIndex.forEach {
            list.add(guestAvatarList.get(it - 1).getTag() as Int)
        }
        map["beVotedUserIDs"] = list

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe(RequestControl("sendVote", ControlType.CancelThis)) {
                roomServerApi.beginVote(body)
            }
            if (result.errno == 0) {
                dismiss(false)
//                U.getToastUtil().showShort("")
            } else {
                if (result.errno == 8348041) {
                    refreshUserInfo(list)
                }
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    //用户不在了，需要重新选择
    private fun refreshUserInfo(userIDs: ArrayList<Int>) {
        selectedSeatIndex.clear()
        setData()
        guestAvatarList.forEachIndexed { index, baseImageView ->
            val tag = baseImageView.getTag()
            tag?.let {
                if (userIDs.contains((tag as Int))) {
                    selectedSeatIndex.add(index + 1)
                    select(true, index + 1)
                }
            }
        }

    }

    private fun setClickListener(view: View, index: Int) {
        view.setDebounceViewClickListener {
            if (selectedSeatIndex.contains(index)) {
                selectedSeatIndex.remove(index)
                select(false, index)
            } else {
                if (selectedSeatIndex.size >= 2) {
                    U.getToastUtil().showShort("最多选择2位被投票嘉宾")
                } else {
                    selectedSeatIndex.add(index)
                    select(true, index)
                }
            }
        }
    }

    private fun showGuestInfo(seatIndex: Int, info: PartyPlayerInfoModel?) {
        MyLog.d("PartySendVoteDialogView", "showGuestInfo seatIndex = $seatIndex, info = $info")
        if (info != null) {
            guestAvatarList[seatIndex - 1].setTag(info.userID)
            guestNameList[seatIndex - 1].text = info.userInfo.nicknameRemark
            guestAvatarList[seatIndex - 1].visibility = View.VISIBLE
            AvatarUtils.loadAvatarByUrl(guestAvatarList[seatIndex - 1],
                    AvatarUtils.newParamsBuilder(info.userInfo.avatar)
                            .setCircle(true)
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(1f).toFloat())
                            .build())
        } else {
            guestNameList[seatIndex - 1].text = ""
            guestAvatarList[seatIndex - 1].visibility = View.GONE
            guestSelectedImgList[seatIndex - 1].visibility = View.GONE
        }
    }

    private fun select(select: Boolean, index: Int) {
        guestSelectedImgList[index - 1].visibility = if (select) View.VISIBLE else View.GONE
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
                .setContentHeight(U.getDisplayUtils().dip2px(400f))
                .setCancelable(canCancel)
                .setMargin(U.getDisplayUtils().dip2px(10f), 0, U.getDisplayUtils().dip2px(10f), U.getDisplayUtils().dip2px(10f))
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