package com.module.feeds.watch.presenter

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.ResponseCallBack
import com.common.core.userinfo.UserInfoManager
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.*
import com.common.utils.U
import com.module.feeds.watch.FeedsWatchServerApi
import com.module.feeds.watch.manager.FeedCollectManager
import com.module.feeds.watch.model.FeedsCollectModel
import com.module.feeds.watch.view.IFeedCollectView
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.HashMap

class FeedCollectViewPresenter(var view: IFeedCollectView) : RxLifeCyclePresenter() {

    private val mFeedServerApi: FeedsWatchServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

    var hasInitData = false  // 标记是否刷新过数据

    init {
        addToLifeCycle()
    }

    fun initFeedLikeList(isFlag: Boolean) {
        if (!isFlag && hasInitData) {
            // 不是必要的刷新，并且已经拉过一次数据
            return
        }

        getFeedsLikeList(true)
    }

    private fun getFeedsLikeList(isClear: Boolean) {
        launch {
            val l = async (Dispatchers.IO){
                FeedCollectManager.getMyCollect()
            }
            view.addLikeList(l.await(), isClear)
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
