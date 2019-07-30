package com.module.feeds.detail.presenter

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.module.feeds.detail.FeedsDetailServerApi
import com.module.feeds.detail.inter.IFeedsDetailView
import com.module.feeds.detail.model.FirstLevelCommentModel
import com.module.feeds.watch.model.FeedUserInfo
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
                    val model: FirstLevelCommentModel.CommentBean = JSON.parseObject(obj.data.getString("comment"), FirstLevelCommentModel.CommentBean::class.java)
                    val firstLevelCommentModel = FirstLevelCommentModel()
                    firstLevelCommentModel.comment = model
                    firstLevelCommentModel.comment.content = content
                    firstLevelCommentModel.commentUser = FeedUserInfo()
                    firstLevelCommentModel.commentUser.nickname = MyUserInfoManager.getInstance().nickName
                    firstLevelCommentModel.commentUser.avatar = MyUserInfoManager.getInstance().avatar
                    firstLevelCommentModel.commentUser.userID = MyUserInfoManager.getInstance().uid.toInt()
                    mIFeedsDetailView.addCommentSuccess(firstLevelCommentModel)
                }
            }
        }, this, ApiMethods.RequestControl(mTag + "addComment", ApiMethods.ControlType.CancelThis))
    }

    //直接回复评论
    fun refuseComment(content: String, feedID: Int, firstLevelCommentID: Int, refuseModel: FirstLevelCommentModel, callBack: ((FirstLevelCommentModel) -> Unit)) {
        val map = HashMap<String, Any>()
        map["content"] = content
        map["feedID"] = feedID
        map["firstLevelCommentID"] = firstLevelCommentID
        map["replyedCommentID"] = refuseModel.comment.commentID

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mFeedsDetailServerApi.addComment(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    val model: FirstLevelCommentModel.CommentBean = JSON.parseObject(obj.data.getString("comment"), FirstLevelCommentModel.CommentBean::class.java)
                    val firstLevelCommentModel = FirstLevelCommentModel()
                    firstLevelCommentModel.comment = model
                    firstLevelCommentModel.comment.content = content
                    firstLevelCommentModel.commentUser = FeedUserInfo()
                    firstLevelCommentModel.commentUser.nickname = MyUserInfoManager.getInstance().nickName
                    firstLevelCommentModel.commentUser.avatar = MyUserInfoManager.getInstance().avatar
                    firstLevelCommentModel.commentUser.userID = MyUserInfoManager.getInstance().uid.toInt()
                    firstLevelCommentModel.replyUser = refuseModel.commentUser
                    callBack.invoke(firstLevelCommentModel)
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

    fun getRelation(userID: Int) {
        ApiMethods.subscribe(mFeedsDetailServerApi.getRelation(userID), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    mIFeedsDetailView.showRelation(obj.data.getBooleanValue("isBlacked"), obj.data.getBooleanValue("isFollow"), obj.data.getBooleanValue("isFriend"))
                }
            }
        }, this, ApiMethods.RequestControl(mTag + "likeFeeds", ApiMethods.ControlType.CancelThis))
    }

    fun addShareCount(userID: Int, feedID: Int) {
        val map = mapOf("feedID" to feedID, "userID" to userID)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mFeedsDetailServerApi.shareAdd(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {

                }
            }
        }, this)
    }
}