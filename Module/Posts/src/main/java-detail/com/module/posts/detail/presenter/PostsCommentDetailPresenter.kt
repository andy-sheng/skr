package com.module.posts.detail.presenter

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.module.posts.detail.PostsDetailServerApi
import com.module.posts.detail.event.AddSecondCommentEvent
import com.module.posts.detail.inter.IPostsCommentDetailView
import com.module.posts.detail.model.PostsSecondLevelCommentModel
import com.module.posts.watch.model.PostsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.util.*

class PostsCommentDetailPresenter(val model: PostsModel, val view: IPostsCommentDetailView) : RxLifeCyclePresenter() {
    val mTag = "PostsDetailPresenter"

    val mPostsServerApi = ApiManager.getInstance().createService(PostsDetailServerApi::class.java)
//    val mModelList: MutableList<PostsSecondLevelCommentModel> = mutableListOf()

    var mOffset = 0
    var mLimit = 30

    var mHasMore = true

    fun getPostsSecondLevelCommentList(firstLevelCommentID: Int) {
        launch {
            val result = subscribe {
                mPostsServerApi.getSecondLevelCommentList(mOffset, mLimit, model.postsID.toInt(), firstLevelCommentID, MyUserInfoManager.getInstance().uid.toInt())
            }

            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("secondLevelComments"), PostsSecondLevelCommentModel::class.java)

                list?.let {
                    if (it.size > 0) {
                        view.showSecondLevelCommentList(it)
                    }
                }

                mHasMore = result.data.getBooleanValue("hasMore")
                mOffset = result.data.getIntValue("offset")
                view.hasMore(mHasMore)
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

    fun addComment(body: RequestBody, mObj: Any?) {
        launch(Dispatchers.Main) {
            val result = subscribe {
                mPostsServerApi.addSecondLevelComment(body)
            }

            if (result.errno == 0) {
                StatisticsAdapter.recordCountEvent("posts", "comment_success", null)
                val model = JSON.parseObject(result.data.getString("secondLevelComment"), PostsSecondLevelCommentModel::class.java)
//                mModelList.add(0, model)
                mOffset++
                view.addSecondLevelCommentSuccess(model)
//                view.showSecondLevelCommentList(mModelList, mHasMore)
                EventBus.getDefault().post(AddSecondCommentEvent(model, view.getFirstLevelCommentID()))
            } else {
                view.addCommetFaild()
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络异常，请检查网络之后重试")
                } else {
                    U.getToastUtil().showShort("${result?.errmsg}")
                    MyLog.e(TAG, "${result?.errmsg}")
                }
            }
        }
    }

    fun deleteComment(commentID: Int, postsID: Int, pos: Int, model: PostsSecondLevelCommentModel?) {
        launch {
            val map = HashMap<String, Any>()
            map["commentID"] = commentID
            map["postsID"] = postsID
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

            val result = subscribe {
                mPostsServerApi.deleteComment(body)
            }

            if (result.errno == 0) {
                view?.deleteCommentSuccess(true, pos, model)
                mOffset--
            } else {
                view?.deleteCommentSuccess(false, pos, model)
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