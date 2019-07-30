package com.component.dialog

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.component.busilib.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.common.view.ex.ExTextView
import com.module.RouterConstants


/**
 * 关注，举报和取消 //首页
 * 举报和取消  //详情页面
 * 回复，举报和取消 //长按评论
 * 分享，举报和取消 //他人页面
 * 分享，删除和取消 //自己页面
 */
class FeedsMoreDialogView(var activity: Activity, type: Int, targetID: Int, songID: Int, commentID: Int) : ConstraintLayout(activity) {

    companion object {
        const val FROM_FEED = 1
        const val FROM_FEED_DETAIL = 2
        const val FROM_COMMENT = 3
        const val FROM_PERSON = 4
        const val FROM_OTHER_PERSON = 5
    }

    private val mCancleTv: ExTextView
    val mReportTv: ExTextView
    val mFuncationTv: ExTextView
    private val mDivider: View

    var mDialogPlus: DialogPlus? = null

    init {
        View.inflate(context, R.layout.feeds_more_dialog_view_layout, this)

        mCancleTv = findViewById(R.id.cancle_tv)
        mReportTv = findViewById(R.id.report_tv)
        mFuncationTv = findViewById(R.id.funcation_tv)
        mDivider = findViewById(R.id.divider)

        mReportTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                dismiss(false)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_REPORT)
                        .withInt("from", type)
                        .withInt("targetID", targetID)
                        .withInt("songID", songID)
                        .withInt("commentID", commentID)
                        .navigation()
            }
        })

        mCancleTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                dismiss()
            }
        })
    }

    fun showFuncation(text: String) {
        mFuncationTv.text = "${text}"
        mFuncationTv.visibility = View.VISIBLE
        mDivider.visibility = View.VISIBLE
    }

    fun hideFuncation() {
        mFuncationTv.visibility = View.GONE
        mDivider.visibility = View.GONE
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