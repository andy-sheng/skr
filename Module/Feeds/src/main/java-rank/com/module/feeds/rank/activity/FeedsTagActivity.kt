package com.module.feeds.rank.activity

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
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
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_FEEDS_TAG)
class FeedsTagActivity : BaseActivity() {

    private lateinit var titlebar: CommonTitleBar
    private lateinit var refreshLayout: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeedTagAdapter

    val mFeedServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = findViewById(R.id.titlebar)
        refreshLayout = findViewById(R.id.refreshLayout)
        recyclerView = findViewById(R.id.recycler_view)

        refreshLayout.apply {
            setEnableLoadMore(false)
            setEnableRefresh(false)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(true)
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
        launch {
            val obj = subscribe(RequestControl("getRecomendTagList", ControlType.CancelThis)) {
                mFeedServerApi.getRecomendTagList()
            }
            if (obj.errno == 0) {
                val list = JSON.parseArray(obj.data.getString("tags"), FeedRecommendTagModel::class.java)
                adapter.mDataList.clear()
                if (!list.isNullOrEmpty()) {
                    adapter.mDataList.addAll(list)
                }
                adapter.notifyDataSetChanged()
            } else {
                if (obj.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
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