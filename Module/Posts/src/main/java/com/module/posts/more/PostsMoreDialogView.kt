package com.module.posts.more

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.support.constraint.Group
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.watch.model.PostsWatchModel
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

/**
 * // 更多的弹窗
 * 举报和取消 //首页
 * 举报和取消 //详情页面
 * 举报和取消 或者 删除和取消//个人中心
 */
class PostsMoreDialogView : ConstraintLayout {

    var mDialogPlus: DialogPlus? = null
    var model: PostsWatchModel? = null

    var from: Int = 0

    constructor(context: Activity?, from: Int, model: PostsWatchModel) : super(context) {
        this.from = from
        this.model = model
    }

    companion object {
        const val FROM_POSTS_HOME = 1    //帖子首页
        const val FROM_POSTS_TOPIC = 2   //话题下的帖子
        const val FROM_POSTS_DETAIL = 3  //帖子详情
        const val FROM_POSTS_PERSON = 4  //个人中心(自己或者他人)
    }

    val cancleTv: ExTextView
    val reportTv: ExTextView

    var replayTv: ExTextView
    var replayTvDivider: View
    var replayArea: Group

    init {
        View.inflate(context, R.layout.posts_more_dialog_view_layout, this)

        cancleTv = rootView.findViewById(R.id.cancle_tv)
        reportTv = rootView.findViewById(R.id.report_tv)
        replayTv = rootView.findViewById(R.id.replay_tv)
        replayTvDivider = rootView.findViewById(R.id.replay_tv_divider)
        replayArea = rootView.findViewById(R.id.replay_area)

        cancleTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                dismiss()
            }
        })

        reportTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 举报
                dismiss(false)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_REPORT)
                        .withInt("from", from)
                        .withInt("targetID", model?.user?.userId ?: 0)
                        .withLong("postsID", model?.posts?.postsID ?: 0)
                        .navigation()
            }
        })
    }

    fun showByDialog() {
        showByDialog(true)
    }

    fun showByDialog(canCancel: Boolean) {
        mDialogPlus?.dismiss(false)
        mDialogPlus = DialogPlus.newDialog(context as Activity)
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