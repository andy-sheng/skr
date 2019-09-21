package com.module.posts.watch.view

import android.support.v4.app.FragmentActivity
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.component.person.view.RequestCallBack
import com.module.posts.more.PostsMoreDialogView
import com.module.posts.watch.model.PostsTopicModel
import com.module.posts.watch.model.PostsTopicTabModel
import com.module.posts.watch.model.PostsWatchModel
import kotlinx.coroutines.launch

class TopicPostsWatchView(activity: FragmentActivity, val topicInfo: PostsTopicModel?, val model: PostsTopicTabModel, val callback: RequestCallBack) : BasePostsWatchView(activity, TYPE_POST_TOPIC) {

    override fun selected() {
        super.selected()
        initPostsList(false)
    }

    override fun unselected(reason: Int) {
        super.unselected(reason)
    }

    override fun onClickMore(position: Int, model: PostsWatchModel?) {
        model?.let {
            postsMoreDialogView?.dismiss(false)
            postsMoreDialogView = PostsMoreDialogView(activity, PostsMoreDialogView.FROM_POSTS_TOPIC, it)
            postsMoreDialogView?.showByDialog(true)
        }
    }

    override fun initPostsList(flag: Boolean): Boolean {
        if (!flag && mHasInitData) {
            // 不一定要刷新
            return false
        }

        getTopicPosts(0, true)
        return true
    }

    override fun getMorePosts() {
        if (hasMore) {
            getTopicPosts(mOffset, false)
        } else {
            U.getToastUtil().showShort("没有更多了")
        }
    }

    private fun getTopicPosts(off: Int, isClear: Boolean) {
        launch {
            val result = subscribe(RequestControl("getTopicPosts", ControlType.CancelThis)) {
                postsWatchServerApi.getTopicPostsList(off, mCNT, MyUserInfoManager.getInstance().uid, topicInfo?.topicID
                        ?: 0, model.tabType)
            }
            if (result.errno == 0) {
                mHasInitData = true
                val list = JSON.parseArray(result.data.getString("details"), PostsWatchModel::class.java)
                mOffset = result.data.getIntValue("offset")
                hasMore = result.data.getBoolean("hasMore")
                addWatchPosts(list, isClear)
            } else {
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }

            callback.onRequestSucess(hasMore)
        }
    }
}