package com.module.feeds.songmanage.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiResult
import com.common.rxretrofit.ERROR_NETWORK_BROKEN
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.module.feeds.R
import com.module.feeds.make.FROM_CHANGE_SING
import com.module.feeds.make.FROM_QUICK_SING
import com.module.feeds.make.make.openFeedsMakeActivityFromChangeSong
import com.module.feeds.make.make.openFeedsMakeActivityFromQuickSong
import com.module.feeds.songmanage.FeedSongManageServerApi
import com.module.feeds.songmanage.adapter.FeedSongManageAdapter
import com.module.feeds.songmanage.adapter.FeedSongManageListener
import com.module.feeds.songmanage.model.FeedSongInfoModel
import com.module.feeds.songmanage.model.FeedSongTagModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class FeedSongManageView(context: Context, val model: FeedSongTagModel, val from: Int) : ConstraintLayout(context), CoroutineScope by MainScope() {

    val refreshLayout: SmartRefreshLayout
    val recyclerView: RecyclerView

    val feedSongManageServerApi = ApiManager.getInstance().createService(FeedSongManageServerApi::class.java)
    var mOffset = 0
    val mCNT = 30
    var mHasMore = true

    val adapter: FeedSongManageAdapter

    init {
        View.inflate(context, R.layout.feed_song_manager_view_layout, this)

        refreshLayout = this.findViewById(R.id.refreshLayout)
        recyclerView = this.findViewById(R.id.recycler_view)

        refreshLayout.setEnableRefresh(false)
        refreshLayout.setEnableLoadMore(true)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        refreshLayout.setEnableOverScrollDrag(false)

        refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                loadMoreData()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
            }
        })

        adapter = FeedSongManageAdapter(object : FeedSongManageListener {
            override fun onClickSing(position: Int, model: FeedSongInfoModel?) {
                model?.let {
                    if (from == FROM_QUICK_SING) {
                        openFeedsMakeActivityFromQuickSong(it.song)
                    } else if (from == FROM_CHANGE_SING) {
                        openFeedsMakeActivityFromChangeSong(it.song)
                    }
                }
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    fun tryloadData() {
        loadData(0, true)
    }

    private fun loadMoreData() {
        loadData(mOffset, false)
    }

    private fun loadData(offset: Int, isClear: Boolean) {
        launch {
            var result: ApiResult? = null
            if (from == FROM_QUICK_SING) {
                result = subscribe { feedSongManageServerApi.getFeedQuickSongList(offset, mCNT, model.tagType) }
            } else if (from == FROM_CHANGE_SING) {
                result = subscribe { feedSongManageServerApi.getFeedChangeSongList(offset, mCNT, model.tagType) }
            }
            if (result?.errno == 0) {
                val list = JSON.parseArray(result.data.getString("songs"), FeedSongInfoModel::class.java)
                mOffset = result.data.getIntValue("offset")
                mHasMore = result.data.getBooleanValue("hasMore")
                addShowList(list, isClear)
            } else {
                if (result?.errno == ERROR_NETWORK_BROKEN) {
                    U.getToastUtil().showShort("网络异常，请检查网络后重试")
                }
            }
        }
    }

    private fun addShowList(list: List<FeedSongInfoModel>?, clear: Boolean) {
        refreshLayout.finishLoadMore()
        refreshLayout.finishRefresh()
        refreshLayout.setEnableLoadMore(mHasMore)

        if (clear) {
            adapter.mDataList.clear()
        }

        if (!list.isNullOrEmpty()) {
            adapter.mDataList.addAll(list)
            adapter.notifyDataSetChanged()
        }

        if (adapter.mDataList.isNullOrEmpty()) {
            // 没有数据，空的页面
        } else {
            //
        }
    }

    fun destory() {
        cancel()
    }
}