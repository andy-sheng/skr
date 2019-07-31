package com.module.feeds.watch.presenter

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.module.feeds.watch.FeedsWatchServerApi
import com.module.feeds.watch.model.FeedsLikeModel
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.view.IFeedLikeView
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.HashMap

class FeedLikeViewPresenter(var view: IFeedLikeView) : RxLifeCyclePresenter() {

    val mFeedServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

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

        getFeedsLikeList(0)
    }

    fun loadMoreFeedLikeList() {
        getFeedsLikeList(mOffset)
    }

    private fun getFeedsLikeList(offset: Int) {
        ApiMethods.subscribe(mFeedServerApi.getFeedLikeList(offset, mCNT, MyUserInfoManager.getInstance().uid.toInt()), object : ApiObserver<ApiResult>() {
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

    fun likeOrUnLikeFeed(model: FeedsLikeModel) {
        val map = HashMap<String, Any>()
        map["feedID"] = model.feedID
        map["like"] = !model.isLiked

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mFeedServerApi.feedLike(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    model.isLiked = !model.isLiked
                    view.showLike(model)
                } else {
                    U.getToastUtil().showShort("${obj?.errmsg}")
                }
            }

        }, this, ApiMethods.RequestControl("likeOrUnLikeFeed", ApiMethods.ControlType.CancelThis))

    }

    override fun destroy() {
        super.destroy()
    }
}
