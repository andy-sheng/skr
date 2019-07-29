package com.module.feeds.rank.fragment

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.common.base.BaseFragment
import com.common.view.ex.NoLeakEditText
import com.module.feeds.R
import com.scwang.smartrefresh.layout.SmartRefreshLayout

class FeedsRankSearchFragment : BaseFragment() {
    lateinit var mCancleTv: TextView
    lateinit var mSearchContent: NoLeakEditText
    lateinit var mRefreshLayout: SmartRefreshLayout
    lateinit var mRecyclerView: RecyclerView


    override fun initView(): Int {
        return R.layout.feeds_rank_search_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mCancleTv = rootView.findViewById(R.id.cancle_tv)
        mSearchContent = rootView.findViewById(R.id.search_content)
        mRefreshLayout = rootView.findViewById(R.id.refreshLayout)
        mRecyclerView = rootView.findViewById(R.id.recycler_view)

    }

    override fun useEventBus(): Boolean {
        return false
    }
}