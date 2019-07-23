package com.module.feeds.watch.presenter

import com.alibaba.fastjson.JSON
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.module.feeds.watch.FeedsWatchServerApi
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.view.FeedsWatchView
import com.module.feeds.watch.view.IFeedsWatchView

class FeedWatchViewPresenter(val view: IFeedsWatchView, private val type: Int) : RxLifeCyclePresenter() {

    private val mFeedServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

    var mOffset = 0   //偏移量
    private val mCNT = 20  // 默认拉去的个数
    private var mLastUpdatListTime = 0L    //上次拉取请求时间戳

    init {
        addToLifeCycle()
    }

    fun initWatchList(flag: Boolean) {
        if (!flag) {
            // 10秒切页面才刷一下
            val now = System.currentTimeMillis()
            if (now - mLastUpdatListTime < 10 * 1000) {
                return
            }
        }
        getWatchList(0)
    }

    fun loadMoreWatchList() {
        getWatchList(mOffset)
    }

    private fun getWatchList(offset: Int) {
        if (type == FeedsWatchView.TYPE_FOLLOW) {
            getRecommendFeedList(offset)
        } else {
            getFollowFeedList(offset)
        }
    }

    private fun getRecommendFeedList(offset: Int) {
        ApiMethods.subscribe(mFeedServerApi.getFeedRecommendList(offset, mCNT), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    val list = JSON.parseArray(obj.data.getString("recommends"), FeedsWatchModel::class.java)
                    mOffset = obj.data.getIntValue("offset")
                    view.addWatchList(list, offset == 0)
                }
            }

        }, this)
    }

    private fun getFollowFeedList(offset: Int) {
        ApiMethods.subscribe(mFeedServerApi.getFeedFollowList(offset, mCNT), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    val list = JSON.parseArray(obj.data.getString("follows"), FeedsWatchModel::class.java)
                    mOffset == obj.data.getIntValue("offset")
                    view.addWatchList(list, offset == 0)
                }
            }

        }, this)
    }


    override fun destroy() {
        super.destroy()
    }
}