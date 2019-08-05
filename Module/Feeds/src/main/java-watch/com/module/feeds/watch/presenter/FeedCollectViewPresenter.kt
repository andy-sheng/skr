package com.module.feeds.watch.presenter

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.*
import com.common.utils.U
import com.module.feeds.watch.FeedsWatchServerApi
import com.module.feeds.watch.model.FeedsCollectModel
import com.module.feeds.watch.view.IFeedCollectView
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.HashMap

class FeedCollectViewPresenter(var view: IFeedCollectView) : RxLifeCyclePresenter() {

    private val mFeedServerApi: FeedsWatchServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

    var mOffset = 0   //偏移量
    private val mCNT = 20  // 默认拉去的个数
    var mLastUpdatListTime = 0L    // 上次拉取请求数据时间戳

    init {
        addToLifeCycle()
    }

    fun initFeedLikeList(isFlag: Boolean) {
        if (!isFlag) {
            // 正常给一个5分钟的间隔
            val now = System.currentTimeMillis()
            if (now - mLastUpdatListTime < 5 * 60 * 1000) {
                return
            }
        }

        getFeedsLikeList(0, true)
    }

    fun loadMoreFeedLikeList() {
        getFeedsLikeList(mOffset, false)
    }

    private fun getFeedsLikeList(offset: Int, isClear: Boolean) {
        launch {
            val result = subscribe { mFeedServerApi.getFeedCollectList(offset, mCNT, MyUserInfoManager.getInstance().uid.toInt()) }
            if (result.errno == 0) {
                mLastUpdatListTime = System.currentTimeMillis()
                val list = JSON.parseArray(result.data.getString("likes"), FeedsCollectModel::class.java)
                mOffset = result.data.getIntValue("offset")
                view.addLikeList(list, isClear)
            } else {
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络异常，请检查网络之后重试")
                }
                view.requestError()
            }
        }
    }

    fun likeOrUnLikeFeed(model: FeedsCollectModel) {
        launch {
            val map = HashMap<String, Any>()
            map["feedID"] = model.feedID
            map["like"] = !model.isLiked

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { mFeedServerApi.collectFeed(body) }
            if (result?.errno == 0) {
                model.isLiked = !model.isLiked
                view.showCollect(model)
            } else {
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络异常，请检查网络之后重试")
                }
                if (MyLog.isDebugLogOpen()) {
                    U.getToastUtil().showShort("${result?.errmsg}")
                } else {
                    MyLog.e(TAG, "${result?.errmsg}")
                }
            }
        }
    }

    override fun destroy() {
        super.destroy()
    }
}
