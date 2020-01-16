package com.module.playways.relay.match.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.module.playways.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

class RelayRedPacketConfirmView(context: Context, val isOpen: Boolean, listener: ClickRedPacketListener) : ConstraintLayout(context) {

    private var mDialogPlus: DialogPlus? = null

    private val imageOpTv: ImageView

    init {
        View.inflate(context, R.layout.relay_red_packet_view_layout, this)

        imageOpTv = this.findViewById(R.id.image_op_tv)
        if (isOpen) {
            imageOpTv.background = U.getDrawable(R.drawable.relay_red_packet_close_icon)
        } else {
            imageOpTv.background = U.getDrawable(R.drawable.relay_red_packet_open_icon)
        }

        imageOpTv.setDebounceViewClickListener {
            listener.onClickRedPacket()
        }
    }

    fun showByDialog() {
        showByDialog(true)
    }

    fun showByDialog(canCancel: Boolean) {

        mDialogPlus?.dismiss(false)

        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.CENTER)
                .setContentBackgroundResource(com.component.busilib.R.color.transparent)
                .setOverlayBackgroundResource(com.component.busilib.R.color.black_trans_80)
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

interface ClickRedPacketListener {
    fun onClickRedPacket()
}