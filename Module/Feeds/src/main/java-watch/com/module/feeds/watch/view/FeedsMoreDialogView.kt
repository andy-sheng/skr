package com.module.feeds.watch.view

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.share.SharePanel
import com.common.core.share.ShareType
import com.common.core.userinfo.ResponseCallBack
import com.common.core.userinfo.ResultCallback
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.common.view.ex.ExTextView
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.event.FeedsCollectChangeEvent
import com.module.feeds.watch.FeedsWatchServerApi
import com.module.feeds.watch.model.FeedsWatchModel
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.util.HashMap

/**
 * 关注Ta,分享，举报，版权举报和取消 //首页
 * 举报，版权举报，取消  //详情页面
 * 收藏歌曲，分享，举报，版权举报和取消 //他人页面
 * 收藏歌曲，分享，删除和取消 //自己页面
 *
 * 回复，举报和取消 //长按评论
 */
class FeedsMoreDialogView(var activity: Activity, type: Int, val model: FeedsWatchModel, isFollow: Boolean?) : ConstraintLayout(activity), CoroutineScope by MainScope() {

    companion object {
        const val FROM_FEED_HOME = 1
        const val FROM_FEED_DETAIL = 2
        const val FROM_PERSON = 3
        const val FROM_OTHER_PERSON = 4
    }

    private val mFeedServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

    val mCancleTv: ExTextView
    val mCopyReportTv: ExTextView
    val mReportTv: ExTextView
    val mDividerReport: View
    val mShareTv: ExTextView
    val mDividerShare: View
    val mCollectTv: ExTextView
    val mDividerCollect: View
    val mFollowTv: ExTextView
    val mDividerFollow: View

    var mSharePanel: SharePanel? = null
    var mDialogPlus: DialogPlus? = null

    init {
        View.inflate(context, R.layout.feeds_more_dialog_view_layout, this)

        mCancleTv = findViewById(R.id.cancle_tv)
        mCopyReportTv = findViewById(R.id.copy_report_tv)
        mReportTv = findViewById(R.id.report_tv)
        mDividerReport = findViewById(R.id.divider_report)
        mShareTv = findViewById(R.id.share_tv)
        mDividerShare = findViewById(R.id.divider_share)
        mCollectTv = findViewById(R.id.collect_tv)
        mDividerCollect = findViewById(R.id.divider_collect)
        mFollowTv = findViewById(R.id.follow_tv)
        mDividerFollow = findViewById(R.id.divider_follow)

        mCancleTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 取消
                dismiss()
            }
        })

        mCopyReportTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 版权举报
                dismiss(false)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_COPY_REPORT)
                        .withInt("from", type)
                        .withSerializable("watchModel", model)
                        .navigation()
            }
        })
        mReportTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 普通举报
                dismiss(false)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_REPORT)
                        .withInt("from", type)
                        .withInt("targetID", model.user?.userId ?: 0)
                        .withInt("songID", model.song?.songID ?: 0)
                        .withInt("feedID", model.feedID)
                        .navigation()
            }
        })

        mShareTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 分享
                dismiss(false)
                share()
            }
        })

        mCollectTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 收藏或取消收藏
                dismiss()
                if ("收藏歌曲".equals(mCollectTv.text)) {
                    collect(false)
                } else {
                    collect(true)
                }
            }
        })

        mFollowTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 关注Ta
                dismiss(false)
                UserInfoManager.getInstance().mateRelation(model.user?.userId
                        ?: 0, UserInfoManager.RA_BUILD
                        , false, object : ResponseCallBack<Boolean>() {
                    override fun onServerFailed() {
                        U.getToastUtil().showShort("关注失败了")
                    }

                    override fun onServerSucess(isFriend: Boolean?) {
                        U.getToastUtil().showShort("关注成功")
                    }
                })
            }
        })

        when (type) {
            FROM_FEED_HOME -> {
                checkFollow(isFollow)
                checkCollect()
            }
            FROM_FEED_DETAIL -> {
                // 详情，只留两个举报和取消
                mFollowTv.visibility = View.GONE
                mDividerFollow.visibility = View.GONE
                mCollectTv.visibility = View.GONE
                mDividerCollect.visibility = View.GONE
                mShareTv.visibility = View.GONE
                mDividerShare.visibility = View.GONE
            }
            FROM_PERSON -> {
                // 只留分享和删除
                mFollowTv.visibility = View.GONE
                mDividerFollow.visibility = View.GONE
                mReportTv.visibility = View.GONE
                mDividerReport.visibility = View.GONE
                mCopyReportTv.text = "删除"
                if (model.status != 2) {
                    // 未审核通过
                    mCollectTv.visibility = View.GONE
                    mDividerCollect.visibility = View.GONE
                } else {
                    checkCollect()
                }
            }
            FROM_OTHER_PERSON -> {
                mFollowTv.visibility = View.GONE
                mDividerFollow.visibility = View.GONE

                mCollectTv.visibility = View.GONE
                mDividerCollect.visibility = View.GONE
                checkCollect()
            }
            else -> {

            }
        }
    }

    private fun share() {
        mSharePanel = SharePanel(activity)
                .apply {
                    mTitle = model.song?.workName
                    mDes = model.user?.nickname
                    mPlayMusicUrl = model.song?.playURL
                    mUrl = String.format("http://www.skrer.mobi/feed/song?songID=%d&userID=%d",
                            model.song?.songID, model.user?.userId)
                }
        mSharePanel?.show(ShareType.MUSIC)
        mSharePanel?.setUMShareListener(object : UMShareListener {
            override fun onResult(p0: SHARE_MEDIA?) {

            }

            override fun onCancel(p0: SHARE_MEDIA?) {

            }

            override fun onError(p0: SHARE_MEDIA?, p1: Throwable?) {

            }

            override fun onStart(p0: SHARE_MEDIA?) {
                launch {
                    val map = mapOf("feedID" to model.feedID, "userID" to MyUserInfoManager.getInstance().uid.toInt())
                    val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                    val result = subscribe { mFeedServerApi.shareAdd(body) }
                    if (result.errno == 0) {
                        model.shareCnt = model.shareCnt.plus(1)
                    } else {

                    }
                }
            }
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
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
        mSharePanel?.dismiss()
    }

    fun dismiss(isAnimation: Boolean) {
        mDialogPlus?.dismiss(isAnimation)
    }

    // 检查是否收藏
    private fun checkCollect() {
//        launch {
//            val result = subscribe { mFeedServerApi.checkCollects(MyUserInfoManager.getInstance().uid.toInt(), model.feedID) }
//            if (result.errno == 0) {
//                val isCollected = result.data.getBooleanValue("isCollected")
//                model.isCollected = isCollected
//                showCollected(isCollected)
//            } else {
//
//            }
//        }
        // model中的就准了，不用请求
        showCollected(model.isCollected)
    }

    // 收藏
    private fun collect(isCollocted: Boolean) {
        launch {
            val map = HashMap<String, Any>()
            map["feedID"] = model.feedID
            map["like"] = !isCollocted

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { mFeedServerApi.collectFeed(body) }
            if (result.errno == 0) {
                // TODO 等服务器接口更新，将收藏接进去
                if (isCollocted) {
                    U.getToastUtil().showShort("取消收藏成功")
                } else {
                    U.getToastUtil().showShort("收藏成功")
                }
                model.isCollected = !isCollocted
                EventBus.getDefault().post(FeedsCollectChangeEvent(model.feedID, model.isCollected))
            } else {
                if (isCollocted) {
                    U.getToastUtil().showShort("收藏失败")
                } else {
                    U.getToastUtil().showShort("取消收藏失败")
                }
            }
        }
    }

    private fun showCollected(isCollocted: Boolean) {
        mCollectTv.visibility = View.VISIBLE
        mDividerCollect.visibility = View.VISIBLE
        if (isCollocted) {
            mCollectTv.text = "取消收藏"
        } else {
            mCollectTv.text = "收藏歌曲"
        }
    }

    private fun checkFollow(isFollow: Boolean?) {
        // 首页推荐和关注
        if (isFollow == false) {
            mFollowTv.visibility = View.VISIBLE
            mDividerFollow.visibility = View.VISIBLE
        } else if (isFollow == true) {
            mFollowTv.visibility = View.GONE
            mDividerFollow.visibility = View.GONE
        } else if (isFollow == null) {
            // 没有取
            mFollowTv.visibility = View.GONE
            mDividerFollow.visibility = View.GONE
            UserInfoManager.getInstance().getUserRelationByUuid(model.user?.userId
                    ?: 0, object : ResultCallback<UserInfoModel>() {
                override fun onGetServer(t: UserInfoModel?): Boolean {
                    checkFollow(t?.isFollow ?: false)
                    return false
                }

                override fun onGetLocalDB(t: UserInfoModel?): Boolean {
                    return false
                }

            })
        }
    }
}