package com.module.club.member

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.common.base.BaseActivity
import com.common.core.view.setDebounceViewClickListener
import com.common.view.titlebar.CommonTitleBar
import com.module.club.R
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener


class ClubMemberListActivity : BaseActivity() {

    lateinit var titlebar: CommonTitleBar
    lateinit var refreshLayout: SmartRefreshLayout
    lateinit var contentRv: RecyclerView

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.club_member_list_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

        titlebar = findViewById(R.id.titlebar)
        refreshLayout = findViewById(R.id.refreshLayout)
        contentRv = findViewById(R.id.content_rv)

        titlebar.leftTextView.setDebounceViewClickListener {
            finish()
        }

        refreshLayout.apply {
            setEnableLoadMore(true)
            setEnableRefresh(false)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(false)

            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {

                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }

            })
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

}