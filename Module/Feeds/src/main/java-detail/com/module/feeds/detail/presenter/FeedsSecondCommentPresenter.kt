package com.module.feeds.detail.presenter

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.mvp.AbsCoroutinePresenter
import com.common.rxretrofit.*
import com.common.utils.U
import com.module.feeds.detail.FeedsDetailServerApi
import com.module.feeds.detail.inter.IFirstLevelCommentView
import com.module.feeds.detail.model.FirstLevelCommentModel
import com.module.feeds.watch.model.FeedUserInfo
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

    fun getSecondLevelCommentList(commentID: Int) {
        ApiMethods.subscribe(mFeedsDetailServerApi.getSecondLevelCommentList(mOffset, mCount, mFeedId, commentID, MyUserInfoManager.getInstance().uid.toInt()), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    val list: List<FirstLevelCommentModel>? = JSON.parseArray(obj.data.getString("comments"), FirstLevelCommentModel::class.java)
                    if (list == null || list.isEmpty()) {
                        mIFirstLevelCommentView.noMore(mModelList.isEmpty())
                    } else {
                        mModelList.addAll(list)
                        mIFirstLevelCommentView.updateList(mModelList)
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
        }, this, RequestControl(mTag + "getFirstLevelCommentList", ControlType.CancelThis))
    }

    fun updateCommentList() {
        mIFirstLevelCommentView.updateList(mModelList)
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
        }, this, RequestControl(mTag + "likeComment", ControlType.CancelThis))
    }

    fun addComment(content: String, feedID: Int, firstLevelCommentID: Int, refuseModel: FirstLevelCommentModel, callBack: ((FirstLevelCommentModel) -> Unit)) {
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
                } else {
                    U.getToastUtil().showShort(obj?.errmsg)
                }
            }
        }, this, RequestControl(mTag + "addComment", ControlType.CancelThis))
    }
}