package com.module.posts.watch.view

import android.support.v4.app.FragmentActivity
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.module.posts.more.PostsMoreDialogView
import com.module.posts.watch.model.PostsWatchModel
import kotlinx.coroutines.launch

// 推荐
class RecommendPostsWatchView(activity: FragmentActivity) : BasePostsWatchView(activity, TYPE_POST_RECOMMEND) {

    override fun selected() {
        super.selected()
        StatisticsAdapter.recordCountEvent("posts", "hot_tab_expose", null)
    }

    override fun initPostsList(flag: Boolean): Boolean {
        if (!flag && mHasInitData) {
            // 不一定要刷新
            return false
        }

        getRecommendPosts(0, true)
        return true
    }

    override fun getMorePosts() {
        if (hasMore) {
            getRecommendPosts(mOffset, false)
        } else {
            U.getToastUtil().showShort("没有更多了")
        }
    }

    private fun getRecommendPosts(off: Int, isClear: Boolean) {
        launch {
            val result = subscribe(RequestControl("getRecommendPosts", ControlType.CancelThis)) {
                postsWatchServerApi.getPostsRecommendList(off, mCNT, MyUserInfoManager.uid.toInt())
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