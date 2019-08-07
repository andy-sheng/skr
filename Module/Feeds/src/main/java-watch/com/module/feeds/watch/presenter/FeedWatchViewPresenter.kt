package com.module.feeds.watch.presenter

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.*
import com.common.utils.U
import com.module.feeds.event.FeedsCollectChangeEvent
import com.module.feeds.watch.FeedsWatchServerApi
import com.module.feeds.watch.model.FeedsCollectModel
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.view.FeedsWatchView
import com.module.feeds.watch.view.IFeedsWatchView
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.util.HashMap

class FeedWatchViewPresenter(val view: IFeedsWatchView, private val type: Int) : RxLifeCyclePresenter() {

    private val mFeedServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

    var mOffset = 0   //偏移量
    private val mCNT = 20  // 默认拉去的个数
    private var mLastUpdatListTime = 0L    //上次拉取请求时间戳(个人中心)
    var mHasInitData = false  //关注和推荐是否初始化过数据
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
        launch {
            val obj = subscribe(RequestControl("getRecommendFeedList", ControlType.CancelThis)) {
                mFeedServerApi.getFeedRecommendList(offset, mCNT, MyUserInfoManager.getInstance().uid.toInt())
            }
            if (obj.errno == 0) {
                mHasInitData = true
//              mLastUpdatListTime = System.currentTimeMillis()
                val list = JSON.parseArray(obj.data.getString("recommends"), FeedsWatchModel::class.java)
                mOffset = obj.data.getIntValue("offset")
                mHasMore = obj.data.getBoolean("hasMore")
                view.addWatchList(list, isClear, mHasMore)
            } else {
                view.requestError()
                if (obj.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    private fun getFollowFeedList(offset: Int, isClear: Boolean) {
        launch {
            val result = subscribe(RequestControl("getFollowFeedList", ControlType.CancelThis)) {
                mFeedServerApi.getFeedFollowList(offset, mCNT, MyUserInfoManager.getInstance().uid.toInt())
            }
            if (result.errno == 0) {
                mHasInitData = true
//              mLastUpdatListTime = System.currentTimeMillis()
                val list = JSON.parseArray(result.data.getString("follows"), FeedsWatchModel::class.java)
                mOffset = result.data.getIntValue("offset")
                mHasMore = result.data.getBoolean("hasMore")
                view.addWatchList(list, isClear, mHasMore)
            } else {
                view.requestError()
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    private fun getPersonFeedList(offset: Int, isClear: Boolean) {
        var feedSongType = 1
        if (MyUserInfoManager.getInstance().uid.toInt() != mUserInfo?.userId) {
            feedSongType = 2
        }
        launch {
            var result = subscribe(RequestControl("getPersonFeedList", ControlType.CancelThis)) {
                mFeedServerApi.queryFeedsList(offset, mCNT, MyUserInfoManager.getInstance().uid.toInt(), mUserInfo?.userId
                        ?: 0, feedSongType)
            }
            if (result.errno == 0) {
                mLastUpdatListTime = System.currentTimeMillis()
                mOffset = result.data.getIntValue("offset")
                mHasMore = result.data.getBoolean("hasMore")
                val list = JSON.parseArray(result.data.getString("userSongs"), FeedsWatchModel::class.java)
                view.addWatchList(list, isClear, mHasMore)
            } else {
                view.requestError()
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    fun feedLike(position: Int, model: FeedsWatchModel) {
        launch {
            val map = HashMap<String, Any>()
            map["feedID"] = model.feedID
            map["like"] = !model.isLiked

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val obj = subscribe(RequestControl("feedLike", ControlType.CancelThis)) {
                mFeedServerApi.feedLike(body)
            }
            if (obj.errno == 0) {
                view.feedLikeResult(position, model, !model.isLiked)
            } else {
                if (obj.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }

    }

    fun deleteFeed(position: Int, model: FeedsWatchModel) {
        launch {
            val map = HashMap<String, Any>()
            map["songID"] = model.song?.songID ?: 0

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val obj = subscribe(RequestControl("deleteFeed", ControlType.CancelThis)) {
                mFeedServerApi.deleteFeed(body)
            }
            if (obj.errno == 0) {
                view.feedDeleteResult(position, model)
            } else {
                if (obj.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    suspend fun getCollectedStatus(model: FeedsWatchModel): Boolean {
        val job = async {
            val result = subscribe { mFeedServerApi.checkCollects(MyUserInfoManager.getInstance().uid.toInt(), model.feedID) }
            result
        }
        val result = job.await()
        var isCollected = false
        if (result.errno == 0) {
            isCollected = result.data.getBooleanValue("isCollected")
        }
        return isCollected
    }

    fun collectOrUnCollectFeed(position: Int, model: FeedsWatchModel) {
        launch {
            val map = HashMap<String, Any>()
            map["feedID"] = model.feedID
            map["like"] = !model.isCollected

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { mFeedServerApi.collectFeed(body) }
            if (result.errno == 0) {
                model.isCollected = !model.isCollected
                view.showCollect(position, model)
                if (model.isCollected) {
                    U.getToastUtil().showShort("收藏成功")
                } else {
                    U.getToastUtil().showShort("取消收藏成功")
                }
                EventBus.getDefault().post(FeedsCollectChangeEvent(model, model.isCollected))
            } else {
                view.requestError()
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

    fun addShareCount(model: FeedsWatchModel){
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

    override fun destroy() {
        super.destroy()
    }
}