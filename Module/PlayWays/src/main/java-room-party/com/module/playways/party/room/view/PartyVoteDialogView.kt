package com.module.playways.party.room.view

import android.content.Context
import android.view.Gravity
import android.view.View
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

/**
 *  下麦，关麦，查看信息，取消
 *  关闭座位，邀请上麦，取消
 *  打开座位，取消
 */
class PartyVoteDialogView(context: Context) : ExConstraintLayout(context) {
    private var mDialogPlus: DialogPlus? = null
    val leftBg: ExImageView
    val rightBg: ExImageView
    val leftAvatarIv: BaseImageView
    val leftNameTv: ExTextView
    val leftTicketTv: ExTextView
    val leftTicketName: ExTextView
    val leftButtom: ExTextView
    val rightAvatarIv: BaseImageView
    val rightNameTv: ExTextView
    val rightTicketTv: ExTextView
    val rightTicketName: ExTextView
    val rightButtom: ExTextView
    val countDownTv: ExTextView

    init {
        View.inflate(context, R.layout.party_vote_view_layout, this)
        leftBg = this.findViewById(R.id.left_bg)
        rightBg = this.findViewById(R.id.right_bg)
        leftAvatarIv = this.findViewById(R.id.left_avatar_iv)
        leftNameTv = this.findViewById(R.id.left_name_tv)
        leftTicketTv = this.findViewById(R.id.left_ticket_tv)
        leftTicketName = this.findViewById(R.id.left_ticket_name)
        leftButtom = this.findViewById(R.id.left_buttom)
        rightAvatarIv = this.findViewById(R.id.right_avatar_iv)
        rightNameTv = this.findViewById(R.id.right_name_tv)
        rightTicketTv = this.findViewById(R.id.right_ticket_tv)
        rightTicketName = this.findViewById(R.id.right_ticket_name)
        rightButtom = this.findViewById(R.id.right_buttom)
        countDownTv = this.findViewById(R.id.count_down_tv)

        leftButtom.setDebounceViewClickListener {

        }

        rightButtom.setDebounceViewClickListener {

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
                .setMargin(U.getDisplayUtils().dip2px(10f), 0, U.getDisplayUtils().dip2px(10f), U.getDisplayUtils().dip2px(10f))
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