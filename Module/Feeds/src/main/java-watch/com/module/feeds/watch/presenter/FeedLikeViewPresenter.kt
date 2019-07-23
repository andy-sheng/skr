package com.module.feeds.watch.presenter

import com.alibaba.fastjson.JSON
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.module.feeds.watch.FeedsWatchServerApi
import com.module.feeds.watch.model.FeedsLikeModel
import com.module.feeds.watch.view.IFeedLikeView

class FeedLikeViewPresenter(var view: IFeedLikeView) : RxLifeCyclePresenter() {

    val mFeedServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

    var mOffset = 0   //偏移量
    private val mCNT = 20  // 默认拉去的个数
    private var mLastUpdatListTime = 0L    //上次拉去推荐房间剩余次数的时间

    init {
        addToLifeCycle()
    }

    fun initFeedLikeList(isFlag: Boolean) {
        if (!isFlag) {
            // 正常给一个10秒的间隔
            val now = System.currentTimeMillis()
            if (now - mLastUpdatListTime < 10 * 1000) {
                return
            }
        }

        getFeedsLikeList(0)
    }

    fun loadMoreFeedLikeList() {
        getFeedsLikeList(mOffset)
    }

    fun getFeedsLikeList(offset: Int) {
        ApiMethods.subscribe(mFeedServerApi.getFeedLikeList(offset, mCNT), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    mLastUpdatListTime = System.currentTimeMillis()
                    val list = JSON.parseArray(obj.data.getString("likes"), FeedsLikeModel::class.java)
                    mOffset = obj.data.getIntValue("offset")
                    view.addLikeList(list, offset == 0)
                }
            }

        }, this)
    }

    override fun destroy() {
        super.destroy()
    }
}
