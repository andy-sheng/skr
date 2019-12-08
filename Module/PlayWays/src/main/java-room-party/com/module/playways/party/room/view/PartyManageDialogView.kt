package com.module.playways.party.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.View
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.party.room.model.PartyActorInfoModel
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.live.proto.PartyRoom.ESeatStatus

/**
 *  下麦，关麦，查看信息，取消
 *  关闭座位，邀请上麦，取消
 *  打开座位，取消
 */
class PartyManageDialogView(context: Context, model: PartyActorInfoModel?) : ConstraintLayout(context) {

    private val function1: ExTextView
    private val function2: ExTextView
    private val function3: ExTextView
    private val cancel: ExTextView

    private var mDialogPlus: DialogPlus? = null

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
            function1.setDebounceViewClickListener { }
            function2.visibility = View.VISIBLE
            function2.text = "关麦"
            function2.setDebounceViewClickListener { }
            function3.visibility = View.VISIBLE
            function3.text = "查看信息"
            function3.setDebounceViewClickListener { }
        } else {
            // 麦上无人
            if (model?.seat?.seatStatus == ESeatStatus.SS_CLOSE.value) {
                // 已关闭的席位
                function1.visibility = View.VISIBLE
                function1.text = "打开座位"
                function1.setDebounceViewClickListener { }
                function2.visibility = View.GONE
                function3.visibility = View.GONE
            } else {
                //空席位
                function1.visibility = View.VISIBLE
                function1.text = "关闭座位"
                function1.setDebounceViewClickListener { }
                function2.visibility = View.VISIBLE
                function2.text = "邀请上麦"
                function2.setDebounceViewClickListener { }
                function3.visibility = View.GONE
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
}