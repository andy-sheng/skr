package com.module.posts.watch.view

import android.support.v4.app.FragmentActivity
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.component.busilib.event.DynamicPostsEvent
import com.module.common.IBooleanCallback
import com.module.post.IDynamicPostsView
import com.module.posts.watch.model.PostsWatchModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

// 动态
class DynamicPostsWatchView(activity: FragmentActivity, type: Int) : FollowPostsWatchView(activity, type), IDynamicPostsView {
    var clubID: Int = 0


    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    //TODO 动态的上报
    override fun selected() {
        super.selected()
        StatisticsAdapter.recordCountEvent("posts", "follow_tab_expose", null)
    }

    override fun loadData(clubId: Int, callback: IBooleanCallback) {
        this.clubID = clubId
        adapter?.loadData(clubId, callback)
        getFollowPosts(0, true, callback)
    }


    override fun loadMoreData(callback: IBooleanCallback) {
        adapter?.loadMoreData(callback)
        if (hasMore) {
            getFollowPosts(mOffset, false, callback)
        } else {
            U.getToastUtil().showShort("没有更多了")
        }
    }

    override fun isHasMore(): Boolean {
        return hasMore
    }


    private fun getFollowPosts(off: Int, isClear: Boolean, callback: IBooleanCallback) {
        launch {
            val result = subscribe(RequestControl("getFollowPosts", ControlType.CancelThis)) {
                postsWatchServerApi.getPostsFollowList(off, mCNT, MyUserInfoManager.uid.toInt())
            }
            if (result.errno == 0) {
                mHasInitData = true
                val list = JSON.parseArray(result.data.getString("details"), PostsWatchModel::class.java)
                mOffset = result.data.getIntValue("offset")
                hasMore = result.data.getBoolean("hasMore")


                /*    val list: MutableList<PostsWatchModel> = ArrayList()
                    for (i in 0..39) {
                        val model = PostsWatchModel()
                        model.isLiked = true
                        model.posts = PostsModel()
                        list.add(model)
                    }*/

                MyLog.d("lijianqun DynamicRoomView loadRoomListData() list = " + list?.size)




                addWatchPosts(list, isClear)
                callback.result(hasMore)
            } else {
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
                /*val list: MutableList<PostsWatchModel> = ArrayList()
                for (i in 0..29) {
                    val model = PostsWatchModel()
                    model.isLiked = true
                    model.posts = PostsModel()
                    list.add(model)
                }

                MyLog.d("lijianqun DynamicRoomView loadRoomListData() list = " + list?.size)

                addWatchPosts(list, isClear)*/
                callback.result(hasMore)
            }
            finishRefreshOrLoadMore()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DynamicPostsEvent) {
        if (event.type == DynamicPostsEvent.EVENT_POST)
            loadData(clubID, {})

    }


    override fun cancel() {
        adapter?.cancel()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}