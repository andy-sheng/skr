package com.module.feeds.detail.presenter

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.component.feeds.model.FeedUserInfo
import com.module.feeds.detail.FeedsDetailServerApi
import com.module.feeds.detail.inter.IFeedsDetailView
import com.module.feeds.detail.model.FirstLevelCommentModel
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.*

class FeedsDetailPresenter(val mIFeedsDetailView: IFeedsDetailView) : RxLifeCyclePresenter() {
    val mTag = "FeedsDetailPresenter"
    val mFeedsDetailServerApi = ApiManager.getInstance().createService(FeedsDetailServerApi::class.java)

    fun addComment(content: String, feedID: Int) {
        val map = HashMap<String, Any>()
        map["content"] = content
        map["feedID"] = feedID

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mFeedsDetailServerApi.addComment(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    val commentId = obj.data.getIntValue("commentID")
                    val firstLevelCommentModel = FirstLevelCommentModel()
                    val feedUserInfo = FeedUserInfo()
                    feedUserInfo.avatar = MyUserInfoManager.getInstance().avatar
                    feedUserInfo.nickname = MyUserInfoManager.getInstance().nickName
                    feedUserInfo.userID = MyUserInfoManager.getInstance().uid.toInt()
                    firstLevelCommentModel.commentUser = feedUserInfo
                    firstLevelCommentModel.comment = FirstLevelCommentModel.CommentBean(commentId, content, System.currentTimeMillis(), MyUserInfoManager.getInstance().uid.toInt())
                    mIFeedsDetailView.addCommentSuccess(firstLevelCommentModel)
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
                    mIFeedsDetailView.likeFeed(like)
                }
            }
        }, this, ApiMethods.RequestControl(mTag + "likeFeeds", ApiMethods.ControlType.CancelThis))
    }
}