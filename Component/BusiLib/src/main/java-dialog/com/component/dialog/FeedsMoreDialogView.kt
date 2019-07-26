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
 * 回复，举报和取消
 * 关注，回复和取消
 */
class FeedsMoreDialogView(var activity: Activity, type: Int) : ConstraintLayout(activity) {

    companion object {
        const val FROM_FEED = 1
        const val FROM_COMMENT = 2
    }

    private val mCancleTv: ExTextView
    private val mReportTv: ExTextView
    val mFuncationTv: ExTextView

    var mDialogPlus: DialogPlus? = null

    init {
        View.inflate(context, R.layout.feeds_more_dialog_view_layout, this)

        mCancleTv = findViewById(R.id.cancle_tv)
        mReportTv = findViewById(R.id.report_tv)
        mFuncationTv = findViewById(R.id.funcation_tv)

        mReportTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_REPORT)
                        .navigation()
            }
        })

        mCancleTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                dismiss()
            }
        })
    }

    fun setFollow(isFollow: Boolean) {
        if (isFollow) {
            mFuncationTv.visibility = View.GONE
        } else {
            mFuncationTv.visibility = View.VISIBLE
            mFuncationTv.text = "关注"
        }
    }

    fun setReply() {
        mFuncationTv.visibility = View.VISIBLE
        mFuncationTv.text = "回复"
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