package com.module.feeds.watch.view

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.rxretrofit.ApiManager
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.common.view.ex.ExTextView
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.event.FeedsCollectChangeEvent
import com.module.feeds.watch.FeedsWatchServerApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.util.HashMap

/**
 * 关注Ta，收藏歌曲，举报和取消 //首页
 * 举报，收藏歌曲，取消  //详情页面
 * 回复，举报和取消 //长按评论
 * 分享，收藏歌曲，举报和取消 //他人页面
 * 分享，收藏歌曲，删除和取消 //自己页面
 */
class FeedsMoreDialogView(var activity: Activity, type: Int, val targetID: Int, val feedID: Int) : ConstraintLayout(activity), CoroutineScope by MainScope() {

    companion object {
        const val FROM_FEED_HOME = 1
        const val FROM_FEED_DETAIL = 2
        const val FROM_COMMENT = 3
        const val FROM_PERSON = 4
        const val FROM_OTHER_PERSON = 5
    }

    private val mFeedServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

    private val mCancleTv: ExTextView
    val mReportTv: ExTextView
    val mFuncationTv: ExTextView
    private val mDividerFuncation: View

    private val mCollectTv: ExTextView
    private val mDividerCollect: View

    var mDialogPlus: DialogPlus? = null

    var songID: Int = 0
    var commentID: Int = 0

    init {
        View.inflate(context, R.layout.feeds_more_dialog_view_layout, this)

        mCancleTv = findViewById(R.id.cancle_tv)
        mReportTv = findViewById(R.id.report_tv)
        mFuncationTv = findViewById(R.id.funcation_tv)
        mDividerFuncation = findViewById(R.id.divider_funcation)
        mCollectTv = findViewById(R.id.collect_tv)
        mDividerCollect = findViewById(R.id.divider_collect)


        mReportTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                dismiss(false)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_REPORT)
                        .withInt("from", type)
                        .withInt("targetID", targetID)
                        .withInt("songID", songID)
                        .withInt("commentID", commentID)
                        .withInt("feedID", feedID)
                        .navigation()
            }
        })

        mCancleTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                dismiss()
            }
        })

        mCollectTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                dismiss()
                if ("收藏歌曲".equals(mCollectTv.text)) {
                    collect(false)
                } else {
                    collect(true)
                }
            }
        })

        when (type) {
            FROM_COMMENT -> {
                // 不用拉接口
                mCollectTv.visibility = View.GONE
                mDividerCollect.visibility = View.GONE
            }
            FROM_FEED_HOME -> {
                // 关系和收藏
                checkCollectAndRelation()
            }
            else -> {
                // 收藏
                checkColledt()
            }
        }
    }

    fun showFuncation(text: String) {
        mFuncationTv.text = text
        mFuncationTv.visibility = View.VISIBLE
        mDividerFuncation.visibility = View.VISIBLE
    }

    fun hideFuncation() {
        mFuncationTv.visibility = View.GONE
        mDividerFuncation.visibility = View.GONE
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

    // 检查收藏
    private fun checkColledt() {
        launch {
            val result = mFeedServerApi.getRelation(targetID)
            if (result.errno == 0) {
                val isCollocted = result.data.getBooleanValue("isCollected")
                showCollected(isCollocted)
            } else {

            }
        }
    }

    // 检查收藏和关注
    private fun checkCollectAndRelation() {
        launch {
            var relation = async {
                mFeedServerApi.getRelation(targetID)
            }.await()

            var collect = async {
                mFeedServerApi.checkCollects(MyUserInfoManager.getInstance().uid.toInt(), feedID)
            }.await()

            if (relation.errno == 0 && collect.errno == 0) {
                val isCollocted = collect.data.getBooleanValue("isCollected")
                val isFriend = relation.data.getBooleanValue("isFriend")
                val isFollow = relation.data.getBooleanValue("isFollow")
                showCollected(isCollocted)
                if (isFollow) {
                    hideFuncation()
                } else {
                    showFuncation("关注Ta")
                }
            } else {

            }
        }
    }

    // 收藏
    private fun collect(isCollocted: Boolean) {
        launch {
            val map = HashMap<String, Any>()
            map["feedID"] = feedID
            map["like"] = !isCollocted

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = mFeedServerApi.collectFeed(body)
            if (result.errno == 0) {
                EventBus.getDefault().post(FeedsCollectChangeEvent())
            }
        }
    }

    private fun showCollected(isCollocted: Boolean) {
        mCollectTv.visibility = View.VISIBLE
        if (isCollocted) {
            mCollectTv.text = "取消收藏"
        } else {
            mCollectTv.text = "收藏歌曲"
        }
    }
}