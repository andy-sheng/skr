package com.module.posts.dialog

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.View
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.posts.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

//todo 评论的举报
class PostsCommentMoreDialogView(var activity: Activity) : ConstraintLayout(activity) {

    var mDialogPlus: DialogPlus? = null

    val cancleTv: ExTextView
    val reportTv: ExTextView
    val deleteTv: ExTextView
    val dividerDelete: View
    val replyTv: ExTextView
    val dividerReply: View

    init {
        View.inflate(context, R.layout.posts_comment_more_dialog_view_layout, this)

        cancleTv = this.findViewById(R.id.cancle_tv)
        reportTv = this.findViewById(R.id.report_tv)
        deleteTv = this.findViewById(R.id.delete_tv)
        dividerDelete = this.findViewById(R.id.divider_delete)
        replyTv = this.findViewById(R.id.reply_tv)
        dividerReply = this.findViewById(R.id.divider_reply)

        cancleTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {

            }
        })

        reportTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {

            }
        })
    }

    fun showByDialog() {
        showByDialog(true)
    }

    fun showByDialog(canCancel: Boolean) {
        mDialogPlus?.dismiss(false)
        mDialogPlus = DialogPlus.newDialog(activity)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setMargin(10.dp(), -1, 10.dp(), 10.dp())
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
