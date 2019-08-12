package com.module.feeds.detail.presenter

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.*
import com.module.feeds.detail.FeedsDetailServerApi
import com.module.feeds.detail.inter.IFirstLevelCommentView
import com.module.feeds.detail.model.FirstLevelCommentModel
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set

class FeedsCommentPresenter(var mFeedId: Int, val mIFirstLevelCommentView: IFirstLevelCommentView) : RxLifeCyclePresenter() {
    val mFeedsDetailServerApi = ApiManager.getInstance().createService(FeedsDetailServerApi::class.java)
    val mCount = 30
    var mOffset = 0
    val mModelList: ArrayList<FirstLevelCommentModel> = ArrayList()

    init {
        addToLifeCycle()
    }

    fun getFirstLevelCommentList() {
        ApiMethods.subscribe(mFeedsDetailServerApi.getFirstLevelCommentList(mOffset, mCount, mFeedId, MyUserInfoManager.getInstance().uid.toInt()), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    val list: List<FirstLevelCommentModel>? = JSON.parseArray(obj.data.getString("comments"), FirstLevelCommentModel::class.java)
                    if (list == null || list.isEmpty()) {
                        mIFirstLevelCommentView.noMore(mModelList.isEmpty())
                    } else {
                        mModelList.addAll(list)
                        mIFirstLevelCommentView.updateList(mModelList)
                    }

                    if (mOffset == 0) {
                        mIFirstLevelCommentView.showNum(obj.data.getIntValue("commentCnt"))
                    }

                    mOffset = obj.data.getIntValue("offset")
                } else {
                    mIFirstLevelCommentView.finishLoadMore()
                }
            }

            override fun onError(e: Throwable) {
                mIFirstLevelCommentView.finishLoadMore()
            }

            override fun onNetworkError(errorType: ErrorType?) {
                mIFirstLevelCommentView.finishLoadMore()
            }
        }, this, RequestControl(TAG + "getFirstLevelCommentList", ControlType.CancelThis))
    }

    fun updateCommentList() {
        mIFirstLevelCommentView.updateList(mModelList)
    }

    fun resetFeedId(feedID: Int) {
        mFeedId = feedID
        mModelList.clear()
    }

    fun likeComment(firstLevelCommentModel: FirstLevelCommentModel, feedID: Int, like: Boolean, position: Int) {
        val map = HashMap<String, Any>()
        map["commentID"] = firstLevelCommentModel.comment.commentID
        map["feedID"] = feedID
        map["like"] = like

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mFeedsDetailServerApi.likeComment(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    firstLevelCommentModel.isLiked = like
                    if (like) {
                        firstLevelCommentModel.comment.likedCnt++
                    } else {
                        firstLevelCommentModel.comment.likedCnt--
                    }

                    mIFirstLevelCommentView.likeFinish(firstLevelCommentModel, position, like)
                }
            }
        }, this, RequestControl(TAG + "likeComment", ControlType.CancelThis))
    }

    fun likeFeeds(like: Boolean, feedID: Int) {
        val map = mapOf("feedID" to feedID, "like" to like)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mFeedsDetailServerApi.likeFeed(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {

                }
            }
        }, this, RequestControl(TAG + "likeFeeds", ControlType.CancelThis))
    }
}