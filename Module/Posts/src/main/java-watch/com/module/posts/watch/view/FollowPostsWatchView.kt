package com.module.posts.watch.view

import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.module.posts.watch.model.PostsWatchModel
import kotlinx.coroutines.launch

// 关注
class FollowPostsWatchView(fragment: BaseFragment) : BasePostsWatchView(fragment, TYPE_POST_FOLLOW) {

    override fun selected() {
        super.selected()
        initPostsList(false)
    }

    override fun unselected(reason: Int) {
        super.unselected(reason)
    }

    override fun initPostsList(flag: Boolean): Boolean {
        if (!flag && mHasInitData) {
            // 不一定要刷新
            return false
        }

        getFollowPosts(0, true)
        return true
    }

    override fun getMorePosts() {
        if (hasMore) {
            getFollowPosts(mOffset, false)
        } else {
            U.getToastUtil().showShort("没有更多了")
        }
    }

    private fun getFollowPosts(off: Int, isClear: Boolean) {
        launch {
            val result = subscribe(RequestControl("getFollowPosts", ControlType.CancelThis)) {
                postsWatchServerApi.getPostsFollowList(off, mCNT, MyUserInfoManager.getInstance().uid.toInt())
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

            finishRefreshOrLoadMore()
        }
    }
}