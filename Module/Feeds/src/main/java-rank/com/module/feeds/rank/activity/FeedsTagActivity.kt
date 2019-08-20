package com.module.feeds.rank.activity

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.rank.adapter.FeedTagAdapter
import com.module.feeds.watch.FeedsWatchServerApi
import com.module.feeds.watch.model.FeedRecommendTagModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_FEEDS_TAG)
class FeedsTagActivity : BaseActivity() {

    private lateinit var titlebar: CommonTitleBar
    private lateinit var refreshLayout: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeedTagAdapter

    var mOffset = 0
    var mCNT = 30
    var hasMore = true

    private val mFeedServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = findViewById(R.id.titlebar)
        refreshLayout = findViewById(R.id.refreshLayout)
        recyclerView = findViewById(R.id.recycler_view)

        refreshLayout.apply {
            setEnableLoadMore(true)
            setEnableRefresh(false)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(true)

            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    loadMoreData()
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {
                    loadData()
                }
            })
        }

        adapter = FeedTagAdapter()
        adapter.onClickTagListener = { _, model ->
            model?.let {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_TAG_DETAIL)
                        .withSerializable("model", it)
                        .navigation()
            }
        }
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter

        titlebar.leftTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })

        loadData()
    }

    private fun loadData() {
        getRecommendTagList(0, true)
    }

    private fun loadMoreData() {
        getRecommendTagList(mOffset, false)
    }

    private fun getRecommendTagList(offset: Int, isClear: Boolean) {
        launch {
            val obj = subscribe(RequestControl("getRecomendTagList", ControlType.CancelThis)) {
                mFeedServerApi.getRecomendTagList(offset, mCNT, MyUserInfoManager.getInstance().uid)
            }
            if (obj.errno == 0) {
                val list = JSON.parseArray(obj.data.getString("tags"), FeedRecommendTagModel::class.java)
                mOffset = obj.data.getIntValue("offset")
                hasMore = obj.data.getBooleanValue("hasMore")
                addRecommendTagList(list, isClear)
            } else {
                refreshLayout.finishLoadMore()
                refreshLayout.finishRefresh()
                if (obj.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    private fun addRecommendTagList(list: List<FeedRecommendTagModel>, clear: Boolean) {
        refreshLayout.finishRefresh()
        refreshLayout.finishLoadMore()
        refreshLayout.setEnableLoadMore(hasMore)

        if (clear) {
            adapter.mDataList.clear()
        }

        if (!list.isNullOrEmpty()) {
            adapter.mDataList.addAll(list)
        }
        adapter.notifyDataSetChanged()
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_tag_activity_layout
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }
}