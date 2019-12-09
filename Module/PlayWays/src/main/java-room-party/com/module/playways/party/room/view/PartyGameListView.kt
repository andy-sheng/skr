package com.module.playways.party.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.module.playways.R
import com.module.playways.party.room.adapter.PartyGameListRecyclerAdapter
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener

class PartyGameListView : ConstraintLayout {
    lateinit var recyclerView: RecyclerView
    lateinit var smartRefresh: SmartRefreshLayout

    var partyGameListRecyclerAdapter: PartyGameListRecyclerAdapter? = null

    var offset = 0
    val cnt = 30
    var hasMore = true

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.party_game_list_view_layout, this)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        smartRefresh = rootView.findViewById(R.id.smart_refresh)
        partyGameListRecyclerAdapter = PartyGameListRecyclerAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = partyGameListRecyclerAdapter

        smartRefresh.apply {
            setEnableRefresh(false)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(true)
            setEnableOverScrollDrag(true)
            setHeaderMaxDragRate(1.5f)
            setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    super.onLoadMore(refreshLayout)
                    getGameList()
                }
            })
        }
    }

    fun selected() {

    }

    fun getGameList() {

    }

    interface Listener {
        fun onClickApplyList()
    }
}