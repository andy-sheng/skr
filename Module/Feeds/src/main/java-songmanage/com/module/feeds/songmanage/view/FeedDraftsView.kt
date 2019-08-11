package com.module.feeds.songmanage.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.utils.U
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.make.FeedsMakeLocalApi
import com.module.feeds.make.FeedsMakeModel
import com.module.feeds.make.make.openFeedsMakeActivity
import com.module.feeds.make.sFeedsMakeModelHolder
import com.module.feeds.songmanage.adapter.FeedSongDraftsAdapter
import com.module.feeds.songmanage.adapter.FeedSongDraftsListener
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * 草稿箱view
 */
class FeedDraftsView(context: Context) : ConstraintLayout(context) {

    val refreshLayout: SmartRefreshLayout
    val recyclerView: RecyclerView

    val adapter: FeedSongDraftsAdapter
    init {
        View.inflate(context, R.layout.feed_song_drafts_view_layout, this)

        refreshLayout = this.findViewById(R.id.refreshLayout)
        recyclerView = this.findViewById(R.id.recycler_view)

        refreshLayout.setEnableRefresh(false)
        refreshLayout.setEnableLoadMore(false)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        refreshLayout.setEnableOverScrollDrag(false)

        refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                // 加载更多
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
            }
        })

        adapter = FeedSongDraftsAdapter(object : FeedSongDraftsListener {
            override fun onClickSing(position: Int, model: FeedsMakeModel?) {
                model?.let {
                    //todo 点击发布或演唱
                    if(TextUtils.isEmpty(model?.audioUploadUrl)){
                        // 演唱
                        openFeedsMakeActivity(model)
                    }else{
                        sFeedsMakeModelHolder = model
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_PUBLISH)
                                .navigation()
                    }
                }
            }

            override fun onLongClick(position: Int, model: FeedsMakeModel?) {
                model?.let {
                    //todo 长按删除
                }
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        (getContext() as BaseActivity).launch {
            val list = async {
                FeedsMakeLocalApi.loadAll()
            }
            adapter.setData(list.await())
        }
    }


}