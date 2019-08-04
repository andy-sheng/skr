package com.module.feeds.detail.view

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.detail.model.FirstLevelCommentModel
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

// 评论的更多
class FeedCommentMoreDialog(var activity: Activity, val model: FirstLevelCommentModel) : ConstraintLayout(activity) {

    val mCancleTv: ExTextView
    val mReportTv: ExTextView
    val mReplyTv: ExTextView
    val mDividerReport: View

    var mDialogPlus: DialogPlus? = null

    companion object {
        const val FROM_COMMENT = 5
    }

    init {
        View.inflate(context, R.layout.comment_more_dialog_view_layout, this)

        mCancleTv = findViewById(R.id.cancle_tv)
        mReportTv = findViewById(R.id.report_tv)
        mReplyTv = findViewById(R.id.reply_tv)
        mDividerReport = findViewById(R.id.divider_report)

        mCancleTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                dismiss()
            }

        })

        mReportTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 普通举报
                dismiss(false)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_REPORT)
                        .withInt("from", FROM_COMMENT)
                        .withInt("targetID", model.commentUser.userID)
                        .withInt("commentID", model.comment.commentID)
                        .withInt("feedID", model.comment.feedID)
                        .navigation()
            }
        })
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