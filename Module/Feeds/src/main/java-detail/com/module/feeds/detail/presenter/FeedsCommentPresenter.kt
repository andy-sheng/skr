package com.module.feeds.detail.presenter

import com.alibaba.fastjson.JSON
import com.common.mvp.AbsCoroutinePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.module.feeds.detail.FeedsDetailServerApi
import com.module.feeds.detail.inter.IFirstLevelCommentView
import com.module.feeds.detail.model.FirstLevelCommentModel
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set

class FeedsCommentPresenter(val mFeedId: Int, val mIFirstLevelCommentView: IFirstLevelCommentView) : AbsCoroutinePresenter() {
    val mTag = "FeedsCommentPresenter"
    val mFeedsDetailServerApi = ApiManager.getInstance().createService(FeedsDetailServerApi::class.java)
    val mCount = 30
    var mOffset = 0
    val mModelList: ArrayList<FirstLevelCommentModel> = ArrayList()

    init {
        addToLifeCycle()
    }

    fun getFirstLevelCommentList() {
        ApiMethods.subscribe(mFeedsDetailServerApi.getFirstLevelCommentList(mOffset, mCount, mFeedId), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    val list: List<FirstLevelCommentModel>? = JSON.parseArray(obj.data.getString("comments"), FirstLevelCommentModel::class.java)
                    if (list == null) {
                        mIFirstLevelCommentView.noMore()
                    } else {
                        mModelList.addAll(list)
                        mIFirstLevelCommentView.updateList(mModelList)
                    }

                    mOffset = obj.data.getIntValue("offset")
                }
            }
        }, this, ApiMethods.RequestControl(mTag + "getFirstLevelCommentList", ApiMethods.ControlType.CancelThis))
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
                    firstLevelCommentModel.comment.isLiked = like
                    if (like) {
                        firstLevelCommentModel.comment.starCnt++
                    } else {
                        firstLevelCommentModel.comment.starCnt--
                    }

                    mIFirstLevelCommentView.likeFinish(firstLevelCommentModel, position)
                }
            }
        }, this, ApiMethods.RequestControl(mTag + "likeComment", ApiMethods.ControlType.CancelThis))
    }

    fun addComment(content: String, feedID: Int) {
        val map = HashMap<String, Any>()
        map["content"] = content
        map["feedID"] = feedID

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mFeedsDetailServerApi.addComment(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {

                }
            }
        }, this, ApiMethods.RequestControl(mTag + "addComment", ApiMethods.ControlType.CancelThis))
    }

    fun likeFeeds(like: Boolean, feedID: Int) {
        val map = mapOf("feedID" to feedID, "like" to like)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mFeedsDetailServerApi.likeFeed(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {

                }
            }
        }, this, ApiMethods.RequestControl(mTag + "likeFeeds", ApiMethods.ControlType.CancelThis))
    }
}