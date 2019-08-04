package com.module.feeds.rank.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.make.openFeedsMakeActivity
import com.module.feeds.rank.FeedsRankServerApi
import com.module.feeds.rank.adapter.FeedsRankAdapter
import com.module.feeds.rank.model.FeedRankInfoModel
import com.module.feeds.rank.model.FeedRankTagModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.android.synthetic.main.feeds_detail_activity_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class FeedsRankView(context: Context, val tag: FeedRankTagModel) : ConstraintLayout(context), CoroutineScope by MainScope() {

    private val mRefreshLayout: SmartRefreshLayout
    private val mRecyclerView: RecyclerView
    private val mAdapter: FeedsRankAdapter

    private val mFeedsRankServerApi: FeedsRankServerApi = ApiManager.getInstance().createService(FeedsRankServerApi::class.java)

    var offset = 0
    var cnt = 30
    var hasMore = true

    init {
        View.inflate(context, R.layout.feed_rank_view_layout, this)

        mRefreshLayout = findViewById(R.id.refreshLayout)
        mRecyclerView = findViewById(R.id.recycler_view)

        mRefreshLayout.setEnableRefresh(false)
        mRefreshLayout.setEnableLoadMore(true)
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        mRefreshLayout.setEnableOverScrollDrag(false)

        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                loadMoreData()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
            }
        })



        mAdapter = FeedsRankAdapter(object : FeedsRankAdapter.Listener {
            override fun onClickItem(position: Int, model: FeedRankInfoModel?) {
                // 进入详细排行榜
                model?.let {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_RANK_DETAIL)
                            .withString("rankTitle", it.rankTitle)
                            .withLong("challengeID", it.challengeID ?: 0L)
                            .navigation()
                }

            }

            override fun onClickHit(position: Int, model: FeedRankInfoModel?) {
                // 直接去打榜
                openFeedsMakeActivity(model?.challengeID)
            }

        })
        mRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mRecyclerView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }

    fun tryLoadData() {
        getData(0, true)
    }

    fun loadMoreData() {
        getData(offset, false)
    }

    private fun getData(off: Int, isClean: Boolean) {
        launch {
            val result = subscribe { mFeedsRankServerApi.getFeedRankInfoList(off, cnt, tag.tagType ?: 0) }
            if (result?.errno == 0) {
                val list = JSON.parseArray(result.data.getString("challengeInfos"), FeedRankInfoModel::class.java)
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBoolean("hasMore")
                showRankInfo(list, isClean)
            } else {
                mRefreshLayout.finishLoadMore()
                mRefreshLayout.finishRefresh()
            }
        }
    }

    private fun showRankInfo(list: List<FeedRankInfoModel>?, isClean: Boolean) {
        mRefreshLayout.finishRefresh()
        mRefreshLayout.finishLoadMore()
        mRefreshLayout.setEnableLoadMore(hasMore)
        if (isClean) {
            mAdapter.mDataList.clear()
        }

        if (list != null) {
            mAdapter.mDataList.addAll(list)
        }
        mAdapter.notifyDataSetChanged()
    }


}