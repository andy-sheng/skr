package com.module.posts.detail.presenter

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.module.posts.detail.PostsDetailServerApi
import com.module.posts.detail.inter.IPostsCommentDetailView
import com.module.posts.detail.model.PostsSecondLevelCommentModel
import com.module.posts.watch.model.PostsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class PostsCommentDetailPresenter(val model: PostsModel, val view: IPostsCommentDetailView) : RxLifeCyclePresenter() {
    val mTag = "PostsDetailPresenter"

    val mPostsServerApi = ApiManager.getInstance().createService(PostsDetailServerApi::class.java)
    val mModelList: MutableList<PostsSecondLevelCommentModel> = mutableListOf()

    var mOffset = 0
    var mLimit = 5

    var mHasMore = true

    fun getPostsSecondLevelCommentList(firstLevelCommentID: Int) {
        launch {
            val result = subscribe {
                mPostsServerApi.getSecondLevelCommentList(mOffset, mLimit, model.postsID.toInt(), firstLevelCommentID, MyUserInfoManager.getInstance().uid.toInt())
            }

            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("secondLevelComments"), PostsSecondLevelCommentModel::class.java)

                list?.let {
                    mModelList.addAll(it)
                }

                mHasMore = result.data.getBooleanValue("hasMore")
                mOffset = result.data.getIntValue("offset")
                view.showSecondLevelCommentList(mModelList, mHasMore)
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

    fun addComment(body: RequestBody, mObj: Any?) {
        launch(Dispatchers.Main) {
            val result = subscribe {
                mPostsServerApi.addSecondLevelComment(body)
            }

            if (result.errno == 0) {
                U.getToastUtil().showShort("评论成功")
                val model = JSON.parseObject(result.data.getString("secondLevelComment"), PostsSecondLevelCommentModel::class.java)
                mModelList.add(0, model)
                view.addSecondLevelCommentSuccess()
                view.showSecondLevelCommentList(mModelList, mHasMore)
            } else {
                view.addCommetFaild()
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