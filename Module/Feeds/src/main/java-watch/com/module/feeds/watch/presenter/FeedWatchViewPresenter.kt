package com.module.feeds.watch.presenter

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.module.feeds.watch.FeedsWatchServerApi
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.view.FeedsWatchView
import com.module.feeds.watch.view.IFeedsWatchView
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.HashMap

class FeedWatchViewPresenter(val view: IFeedsWatchView, private val type: Int) : RxLifeCyclePresenter() {

    private val mFeedServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

    var mOffset = 0   //偏移量
    private val mCNT = 20  // 默认拉去的个数
    private var mLastUpdatListTime = 0L    //上次拉取请求时间戳(个人中心)
    private var mHasInitData = false  //关注和推荐是否初始化过数据
    private var mHasMore = true     // 是否还有更多
    var mUserInfo: UserInfoModel? = null

    init {
        addToLifeCycle()
    }

    fun initWatchList(flag: Boolean): Boolean {

        if (!flag && type == FeedsWatchView.TYPE_PERSON) {
            // 3分钟切页面才刷一下(只在个人中心生效)
            val now = System.currentTimeMillis()
            if (now - mLastUpdatListTime < 180 * 1000) {
//                view.requestTimeShort()
                return false
            }
        }
        if (!flag && type != FeedsWatchView.TYPE_PERSON) {
            if (mHasInitData) {
                return false
            }
        }
        getWatchList(0, true)
        return true
    }

    fun loadMoreWatchList() {
        getWatchList(mOffset, false)
    }

    private fun getWatchList(offset: Int, isClear: Boolean) {
        when (type) {
            FeedsWatchView.TYPE_FOLLOW -> getFollowFeedList(offset, isClear)
            FeedsWatchView.TYPE_RECOMMEND -> getRecommendFeedList(offset, isClear)
            else -> getPersonFeedList(offset, isClear)
        }
    }

    private fun getRecommendFeedList(offset: Int, isClear: Boolean) {
        ApiMethods.subscribe(mFeedServerApi.getFeedRecommendList(offset, mCNT, MyUserInfoManager.getInstance().uid.toInt()), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    mHasInitData = true
//                    mLastUpdatListTime = System.currentTimeMillis()
                    val list = JSON.parseArray(obj.data.getString("recommends"), FeedsWatchModel::class.java)
                    mOffset = obj.data.getIntValue("offset")
                    mHasMore = obj.data.getBoolean("hasMore")
                    view.addWatchList(list, isClear, mHasMore)
                }
            }

        }, this, ApiMethods.RequestControl("getRecommendFeedList", ApiMethods.ControlType.CancelThis))
    }

    private fun getFollowFeedList(offset: Int, isClear: Boolean) {
        ApiMethods.subscribe(mFeedServerApi.getFeedFollowList(offset, mCNT, MyUserInfoManager.getInstance().uid.toInt()), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    mHasInitData = true
//                    mLastUpdatListTime = System.currentTimeMillis()
                    val list = JSON.parseArray(obj.data.getString("follows"), FeedsWatchModel::class.java)
                    mOffset = obj.data.getIntValue("offset")
                    mHasMore = obj.data.getBoolean("hasMore")
                    view.addWatchList(list, isClear, mHasMore)
                }
            }

        }, this, ApiMethods.RequestControl("getFollowFeedList", ApiMethods.ControlType.CancelThis))
    }

    private fun getPersonFeedList(offset: Int, isClear: Boolean) {
        var feedSongType = 1
        if (MyUserInfoManager.getInstance().uid.toInt() != mUserInfo?.userId) {
            feedSongType = 2
        }
        ApiMethods.subscribe(mFeedServerApi.queryFeedsList(offset, mCNT, MyUserInfoManager.getInstance().uid.toInt(), mUserInfo?.userId
                ?: 0, feedSongType), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult?) {
                if (result?.errno == 0) {
                    mLastUpdatListTime = System.currentTimeMillis()
                    mOffset = result.data.getIntValue("offset")
                    mHasMore = result.data.getBoolean("hasMore")
                    val list = JSON.parseArray(result.data.getString("userSongs"), FeedsWatchModel::class.java)
                    view.addWatchList(list, isClear, mHasMore)
                } else {
                    view.requestError()
                }
            }

            override fun onNetworkError(errorType: ErrorType?) {
                super.onNetworkError(errorType)
                view.requestError()
            }

        }, this, ApiMethods.RequestControl("getFeeds", ApiMethods.ControlType.CancelThis))
    }

    fun feedLike(position: Int, model: FeedsWatchModel) {
        val map = HashMap<String, Any>()
        map["feedID"] = model.feedID
        map["like"] = !model.isLiked

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mFeedServerApi.feedLike(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    view.feedLikeResult(position, model, !model.isLiked)
                } else {
                    U.getToastUtil().showShort("${obj?.errmsg}")
                }
            }

        }, this, ApiMethods.RequestControl("feedLike", ApiMethods.ControlType.CancelThis))

    }

    fun deleteFeed(position: Int, model: FeedsWatchModel) {
        val map = HashMap<String, Any>()
        map["songID"] = model.song?.songID ?: 0

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mFeedServerApi.deleteFeed(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    view.feedDeleteResult(position, model)
                } else {
                    U.getToastUtil().showShort("${obj?.errmsg}")
                }
            }

        }, this, ApiMethods.RequestControl("deleteFeed", ApiMethods.ControlType.CancelThis))
    }

    override fun destroy() {
        super.destroy()
    }
}