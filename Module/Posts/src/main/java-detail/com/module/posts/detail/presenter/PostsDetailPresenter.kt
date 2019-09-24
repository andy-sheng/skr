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
import com.module.posts.detail.model.PostsSecondLevelCommentModel
import com.module.posts.watch.model.PostsModel
import com.module.posts.watch.model.PostsWatchModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.*

class PostsDetailPresenter : RxLifeCyclePresenter {
    val mTag = "PostsDetailPresenter"

    val mPostsDetailServerApi = ApiManager.getInstance().createService(PostsDetailServerApi::class.java)
    val mModelList: MutableList<PostFirstLevelCommentModel> = mutableListOf()

    var mOffset = 0
    var mLimit = 30

    var mHasMore = true

    var model: PostsModel? = null
    var view: IPostsDetailView? = null

    constructor(view: IPostsDetailView) {
        this.view = view
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

    fun likePosts(like: Boolean, postsWatchModel: PostsWatchModel) {
        launch(Dispatchers.Main) {
            val result = subscribe {
                val map = mapOf("postsID" to postsWatchModel.posts!!.postsID, "like" to like)
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                mPostsDetailServerApi.likePosts(body)
            }

            if (result.errno == 0) {
                postsWatchModel.isLiked = like
                if (like) {
                    postsWatchModel?.numeric?.starCnt = postsWatchModel?.numeric?.starCnt!! + 1
                } else {
                    postsWatchModel?.numeric?.starCnt = postsWatchModel?.numeric?.starCnt!! - 1
                }
                view?.showLikePostsResulet()
            } else {
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络异常，请检查网络之后重试")
                } else {
                    U.getToastUtil().showShort("${result?.errmsg}")
                    MyLog.e(TAG, "${result?.errmsg}")
                }
            }
        }
    }

    fun likeFirstLevelComment(like: Boolean, postFirstLevelCommentModel: PostFirstLevelCommentModel) {
        launch(Dispatchers.Main) {
            val result = subscribe {
                val map = mapOf("postsID" to postFirstLevelCommentModel.comment?.postsID, "like" to like, "commentID" to postFirstLevelCommentModel.comment?.commentID)
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                mPostsDetailServerApi.likeComment(body)
            }

            if (result.errno == 0) {
                postFirstLevelCommentModel.isLiked = like
                if (like) {
                    postFirstLevelCommentModel.comment?.likedCnt = postFirstLevelCommentModel.comment?.likedCnt!! + 1
                } else {
                    postFirstLevelCommentModel.comment?.likedCnt = postFirstLevelCommentModel.comment?.likedCnt!! - 1
                }

                view?.showLikeFirstLevelCommentResult(postFirstLevelCommentModel)
            } else {
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络异常，请检查网络之后重试")
                } else {
                    U.getToastUtil().showShort("${result?.errmsg}")
                    MyLog.e(TAG, "${result?.errmsg}")
                }
            }
        }
    }

    fun getPostsDetail(postsID: Int) {
        launch(Dispatchers.Main) {
            val result = subscribe {
                mPostsDetailServerApi.getPostsDetail(MyUserInfoManager.getInstance().uid.toInt(), postsID)
            }

            if (result.errno == 0) {
                val postsWatchModel = JSON.parseObject(result.data.getString("detail"), PostsWatchModel::class.java)
                if (postsWatchModel != null) {
                    model = postsWatchModel.posts
                    view?.showPostsWatchModel(postsWatchModel)
                } else {
                    view?.loadDetailDelete()
                }
            } else {
                view?.loadDetailError()
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络异常，请检查网络之后重试")
                } else {
                    U.getToastUtil().showShort("${result?.errmsg}")
                    MyLog.e(TAG, "${result?.errmsg}")
                }
            }
        }
    }

    fun getPostsFirstLevelCommentList() {
        launch(Dispatchers.Main) {
            val result = subscribe {
                mPostsDetailServerApi.getFirstLevelCommentList(mOffset, mLimit, model?.postsID?.toInt()
                        ?: 0, MyUserInfoManager.getInstance().uid.toInt())
            }

            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("comments"), PostFirstLevelCommentModel::class.java)

                list?.let {
                    mModelList.addAll(it)
                }

                mHasMore = result.data.getBooleanValue("hasMore")
                mOffset = result.data.getIntValue("offset")
                view?.showFirstLevelCommentList(mModelList, mHasMore)
            } else {
                view?.loadMoreError()
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络异常，请检查网络之后重试")
                } else {
                    U.getToastUtil().showShort("${result?.errmsg}")
                    MyLog.e(TAG, "${result?.errmsg}")
                }
            }
        }
    }

    fun addComment(body: RequestBody, mObj: Any?) {
        launch(Dispatchers.Main) {
            val result = subscribe {
                if (mObj is PostsWatchModel) {
                    mPostsDetailServerApi.addFirstLevelComment(body)
                } else {
                    mPostsDetailServerApi.addSecondLevelComment(body)
                }
            }

            if (result.errno == 0) {
                U.getToastUtil().showShort("评论成功")
                if (mObj is PostsWatchModel) {
                    val model = JSON.parseObject(result.data.getString("firstLevelComment"), PostFirstLevelCommentModel::class.java)
                    mModelList.add(0, model)
                    view?.addFirstLevelCommentSuccess()
                    view?.showFirstLevelCommentList(mModelList, mHasMore)
                } else if (mObj is PostFirstLevelCommentModel) {
                    val model = JSON.parseObject(result.data.getString("secondLevelComment"), PostsSecondLevelCommentModel::class.java)
                    if (mObj.secondLevelComments == null) {
                        mObj.secondLevelComments = mutableListOf()
                    }
                    mObj.secondLevelComments?.add(0, model)
                    view?.addSecondLevelCommentSuccess()
                }
            } else {
                view?.addCommetFaild()
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络异常，请检查网络之后重试")
                } else {
                    U.getToastUtil().showShort("${result?.errmsg}")
                    MyLog.e(TAG, "${result?.errmsg}")
                }
            }
        }
    }
}