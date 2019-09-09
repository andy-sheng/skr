package com.module.playways.battle.songlist.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.module.playways.R
import com.module.playways.battle.songlist.adapter.BattleRankAdapter
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener

class BattleRankView(context: Context) : ConstraintLayout(context) {

    private val refreshLayout: SmartRefreshLayout
    private val recyclerView: RecyclerView
    private val adapter = BattleRankAdapter()

    init {
        View.inflate(context, R.layout.battle_rank_view_layout, this)

        refreshLayout = this.findViewById(R.id.refreshLayout)
        recyclerView = this.findViewById(R.id.recycler_view)

        refreshLayout.apply {
            setEnableRefresh(false)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(false)

            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                }
            })
        }

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        adapter.onClickListener = { model, _ ->
            model?.user?.let {
                // todo 跳到个人中心
            }
        }
    }
}