package com.module.posts.detail.presenter

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.module.posts.detail.PostsDetailServerApi
import com.module.posts.detail.inter.IPostsDetailView
import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.watch.model.PostsModel
import kotlinx.coroutines.launch
import java.util.*

class PostsDetailPresenter(val model: PostsModel, val view: IPostsDetailView) : RxLifeCyclePresenter() {
    val mTag = "PostsDetailPresenter"

    val mPostsDetailServerApi = ApiManager.getInstance().createService(PostsDetailServerApi::class.java)
    val mModelList: MutableList<PostFirstLevelCommentModel> = mutableListOf()

    var mOffset = 0
    var mLimit = 5

    var mHasMore = true

    fun addComment(content: String, feedID: Int) {
        val map = HashMap<String, Any>()
        map["content"] = content
        map["feedID"] = feedID

//        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
//        ApiMethods.subscribe(mFeedsDetailServerApi.addComment(body), object : ApiObserver<ApiResult>() {
//            override fun process(obj: ApiResult?) {
//                if (obj?.errno == 0) {
//                    val model = JSON.parseObject(obj.data.getString("comment"), FirstLevelCommentModel.CommentBean::class.java)
//                    model?.let {
//                        val firstLevelCommentModel = FirstLevelCommentModel()
//                        firstLevelCommentModel.comment = model
//                        firstLevelCommentModel.comment.content = content
//                        firstLevelCommentModel.commentUser = UserInfoModel()
//                        firstLevelCommentModel.commentUser.nickname = MyUserInfoManager.getInstance().nickName
//                        firstLevelCommentModel.commentUser.avatar = MyUserInfoManager.getInstance().avatar
//                        firstLevelCommentModel.commentUser.userId = MyUserInfoManager.getInstance().uid.toInt()
//                        mIFeedsDetailView.addCommentSuccess(firstLevelCommentModel)
//                    }
//                } else {
//                    U.getToastUtil().showShort(obj?.errmsg)
//                }
//            }
//        }, this, RequestControl(mTag + "addComment", ControlType.CancelThis))
    }

    //直接回复评论
    fun refuseComment(content: String, feedID: Int, firstLevelCommentID: Int) {
        val map = HashMap<String, Any>()
        map["content"] = content
        map["feedID"] = feedID
        map["firstLevelCommentID"] = firstLevelCommentID
//        map["replyedCommentID"] = refuseModel.comment.commentID
//
//        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
//        ApiMethods.subscribe(mFeedsDetailServerApi.addComment(body), object : ApiObserver<ApiResult>() {
//            override fun process(obj: ApiResult?) {
//                if (obj?.errno == 0) {
//                    val model = JSON.parseObject(obj.data.getString("comment"), FirstLevelCommentModel.CommentBean::class.java)
//                    model?.let {
//                        val firstLevelCommentModel = FirstLevelCommentModel()
//                        firstLevelCommentModel.comment = model
//                        firstLevelCommentModel.comment.content = content
//                        firstLevelCommentModel.commentUser = UserInfoModel()
//                        firstLevelCommentModel.commentUser.nickname = MyUserInfoManager.getInstance().nickName
//                        firstLevelCommentModel.commentUser.avatar = MyUserInfoManager.getInstance().avatar
//                        firstLevelCommentModel.commentUser.userId = MyUserInfoManager.getInstance().uid.toInt()
//                        firstLevelCommentModel.replyUser = refuseModel.commentUser
//                        callBack.invoke(firstLevelCommentModel)
//                    }
//                }
//            }
//        }, this, RequestControl(mTag + "addComment", ControlType.CancelThis))
    }

    fun likeFeeds(like: Boolean, feedID: Int) {
        val map = mapOf("feedID" to feedID, "like" to like)
//        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
//        ApiMethods.subscribe(mFeedsDetailServerApi.likeFeed(body), object : ApiObserver<ApiResult>() {
//            override fun process(obj: ApiResult?) {
//                if (obj?.errno == 0) {
//                    mIFeedsDetailView.likeFeed(like)
//                }
//            }
//        }, this, RequestControl(mTag + "likeFeeds", ControlType.CancelThis))
    }

    fun getRelation(userID: Int) {
        launch {
            val result = subscribe {
                mPostsDetailServerApi.getRelation(userID)
            }

            if (result.errno == 0) {
                view.showRelation(result.data.getBooleanValue("isBlacked"), result.data.getBooleanValue("isFollow"), result.data.getBooleanValue("isFriend"))
            }
        }
    }

    fun getPostsFirstLevelCommentList() {
        launch {
            val result = subscribe {
                mPostsDetailServerApi.getFirstLevelCommentList(mOffset, mLimit, model.postsID.toInt(), MyUserInfoManager.getInstance().uid.toInt())
            }

            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("comments"), PostFirstLevelCommentModel::class.java)

                list?.let {
                    mModelList.addAll(it)
                }

                mHasMore = result.data.getBooleanValue("hasMore")
                mOffset = result.data.getIntValue("offset")
                view.showFirstLevelCommentList(mModelList, mHasMore)
            } else {
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
}