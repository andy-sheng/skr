package com.component.dialog

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.HandlerTaskTimer
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.busilib.view.NickNameView
import com.facebook.drawee.view.SimpleDraweeView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.live.proto.Notification.RelayRoomInviteMsg

class RedPacketRelayDialogView constructor(context: Context, val event: RelayRoomInviteMsg) : RelativeLayout(context) {
    val TAG = "RedPacketRelayDialogView"
    private var mDialogPlus: DialogPlus? = null

    lateinit var mAvatarIv: SimpleDraweeView
    lateinit var mContentTv: TextView
    lateinit var mCancleTv: ExTextView
    lateinit var mConfirmTv: ExTextView
//    lateinit var mNameTv: NickNameView
    var mUserInfoModel: UserInfoModel

    var clickMethod: ((Boolean) -> Unit)? = null
    var timeOutMethod: (() -> Unit)? = null

    internal var mCounDownTimer: HandlerTaskTimer? = null

    internal var normalColor = Color.parseColor("#cc8B572A")
    internal var lightColor = Color.parseColor("#F5A623")

    init {
        mUserInfoModel = UserInfoModel.parseFromPB(event.user)
        initView()
        initData()
    }

    fun initView() {
        View.inflate(context, R.layout.red_packet_relay_confirm_dialog_view, this)

        mAvatarIv = findViewById(R.id.avatar_iv)
        mContentTv = findViewById(R.id.content_tv)
        mCancleTv = findViewById(R.id.cancle_tv)
        mConfirmTv = findViewById(R.id.confirm_tv)
//        mNameTv = findViewById(R.id.name_view)
    }

    private fun initData() {
        if (mUserInfoModel == null) {
            MyLog.d(TAG, "未找到该用户相关信息")
            return
        }

//        mNameTv.setAllStateText(mUserInfoModel)
        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(mUserInfoModel!!.avatar)
                        .setCircle(true)
                        .build())

        val stringBuilder = SpanUtils()
                .append(" " + mUserInfoModel!!.nicknameRemark + " ").setForegroundColor(lightColor)
                .append("\n")
                .append(event.inviteMsg).setForegroundColor(normalColor)
                .create()
        mContentTv.text = stringBuilder

        mCancleTv.text = "取消"
        mConfirmTv.text = "确定"

        mCancleTv.setDebounceViewClickListener {
            dismiss(false)
            clickMethod?.invoke(false)
        }

        mConfirmTv.setDebounceViewClickListener {
            dismiss(false)
            clickMethod?.invoke(true)
        }
    }

    fun starCounDown(counDown: Int) {
        disposeCounDownTask()
        mCounDownTimer = HandlerTaskTimer.newBuilder()
                .take(counDown)
                .interval(1000)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(integer: Int) {
                        mContentTv.text = "${event.inviteMsg} ${counDown - integer!!}s"
                    }

                    override fun onComplete() {
                        super.onComplete()
                        dismiss(false)
                        timeOutMethod?.invoke()
                    }
                })
    }

    private fun disposeCounDownTask() {
        if (mCounDownTimer != null) {
            mCounDownTimer!!.dispose()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposeCounDownTask()
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
                .setContentHeight(U.getDisplayUtils().dip2px(170f))
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
}
