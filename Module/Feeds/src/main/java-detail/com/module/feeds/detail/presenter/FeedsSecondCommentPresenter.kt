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

class FeedsSecondCommentPresenter(val mFeedId: Int, val mIFirstLevelCommentView: IFirstLevelCommentView) : AbsCoroutinePresenter() {
    val mTag = "FeedsSecondCommentPresenter"
    val mFeedsDetailServerApi = ApiManager.getInstance().createService(FeedsDetailServerApi::class.java)
    val mCount = 30
    var mOffset = 0
    val mModelList: ArrayList<FirstLevelCommentModel> = ArrayList()

    init {
        addToLifeCycle()
    }

    fun getSecondLevelCommentList() {
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
                    firstLevelCommentModel.isLiked = like
                    if (like) {
                        firstLevelCommentModel.comment.likedCnt++
                    } else {
                        firstLevelCommentModel.comment.likedCnt--
                    }

                    mIFirstLevelCommentView.likeFinish(firstLevelCommentModel, position, like)
                }
            }
        }, this, ApiMethods.RequestControl(mTag + "likeComment", ApiMethods.ControlType.CancelThis))
    }

    fun addComment(content: String, feedID: Int, refuseModel: FirstLevelCommentModel, callBack: ((FirstLevelCommentModel) -> Unit)) {
        val map = HashMap<String, Any>()
        map["content"] = content
        map["feedID"] = feedID
        map["firstLevelCommentID"] = refuseModel.comment.commentID
        map["replyedUserID"] = refuseModel.comment.userID

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mFeedsDetailServerApi.addComment(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    callBack.invoke(refuseModel)
                }
            }
        }, this, ApiMethods.RequestControl(mTag + "addComment", ApiMethods.ControlType.CancelThis))
    }
}