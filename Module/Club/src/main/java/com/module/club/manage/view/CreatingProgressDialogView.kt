package com.module.club.manage.view

import android.content.Context
import android.view.Gravity
import android.view.View
import com.common.view.ex.ExConstraintLayout
import com.module.club.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder


/**
 *  下麦，关麦，查看信息，取消
 *  关闭座位，邀请上麦，取消
 *  打开座位，取消
 */
class CreatingProgressDialogView(context: Context) : ExConstraintLayout(context) {

    private var mDialogPlus: DialogPlus? = null

    init {
        View.inflate(context, R.layout.creating_club_dialog_view_layout, this)
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
                .setGravity(Gravity.CENTER)
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