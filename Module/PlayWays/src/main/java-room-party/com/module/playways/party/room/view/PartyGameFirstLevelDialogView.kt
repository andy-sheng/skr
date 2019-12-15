package com.module.playways.party.room.view

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

/**
 *  下麦，关麦，查看信息，取消
 *  关闭座位，邀请上麦，取消
 *  打开座位，取消
 */
class PartyGameFirstLevelDialogView(context: Context) : ExConstraintLayout(context) {

    private var mDialogPlus: DialogPlus? = null
    var ivTitle: TextView
    var gameDesTv: ExTextView

    init {
        View.inflate(context, R.layout.party_game_first_level_dialog_layout, this)

        ivTitle = rootView.findViewById(R.id.iv_title)
        gameDesTv = rootView.findViewById(R.id.game_des_tv)

    }

    fun setDes(title: String, str: String) {
        ivTitle.text = title
        gameDesTv.text = str
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