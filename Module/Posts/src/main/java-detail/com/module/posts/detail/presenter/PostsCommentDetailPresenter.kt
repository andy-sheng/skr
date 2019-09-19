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
import kotlinx.coroutines.launch

class PostsCommentDetailPresenter(val model: PostsModel, val view: IPostsCommentDetailView) : RxLifeCyclePresenter() {
    val mTag = "PostsDetailPresenter"

    val mPostsServerApi = ApiManager.getInstance().createService(PostsDetailServerApi::class.java)
    val mModelList: MutableList<PostsSecondLevelCommentModel> = mutableListOf()

    var mOffset = 0
    var mLimit = 5

    var mHasMore = true

    fun getPostsSecondLevelCommentList() {
        launch {
            val result = subscribe {
                mPostsServerApi.getSecondLevelCommentList(mOffset, mLimit, model.postsID.toInt(), 0, MyUserInfoManager.getInstance().uid.toInt())
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
}