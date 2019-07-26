package com.module.feeds.rank.presenter

import com.alibaba.fastjson.JSON
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.component.feeds.model.FeedRankModel
import com.module.feeds.rank.FeedsRankServerApi
import com.module.feeds.rank.view.IFeedsRank

class FeedsRankPresenter(val view: IFeedsRank) : RxLifeCyclePresenter() {

    private val mFeedRankServerApi: FeedsRankServerApi = ApiManager.getInstance().createService(FeedsRankServerApi::class.java)

    fun getFeedsRankTags() {
        ApiMethods.subscribe(mFeedRankServerApi.getFeedsRankTags(), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    val list = JSON.parseArray(obj.data.getString("tags"), FeedRankModel::class.java)
                    view.showFeedRankTag(list)
                } else {
                    view.showFailed()
                }
            }

            override fun onNetworkError(errorType: ErrorType?) {
                super.onNetworkError(errorType)
                view.showFailed()
            }
        }, this)
    }
}