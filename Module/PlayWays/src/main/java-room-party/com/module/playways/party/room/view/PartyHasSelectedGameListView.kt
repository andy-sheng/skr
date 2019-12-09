package com.module.playways.party.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.module.playways.R
import com.module.playways.party.room.adapter.PartyHasSelectedGameListRecyclerAdapter
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener

class PartyHasSelectedGameListView : ConstraintLayout {
    var recyclerView: RecyclerView

    var partyHasSelectedGameListRecyclerAdapter: PartyHasSelectedGameListRecyclerAdapter? = null
    var smartRefresh: SmartRefreshLayout

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    init {
        View.inflate(context, R.layout.party_game_list_view_layout, this)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        smartRefresh = rootView.findViewById(R.id.smart_refresh)
        partyHasSelectedGameListRecyclerAdapter = PartyHasSelectedGameListRecyclerAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = partyHasSelectedGameListRecyclerAdapter

        smartRefresh.apply {
            setEnableRefresh(false)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(true)
            setEnableOverScrollDrag(true)
            setHeaderMaxDragRate(1.5f)
            setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    super.onLoadMore(refreshLayout)
                    getSelectedGameList()
                }
            })
        }
    }

    fun selected() {

    }

    fun getSelectedGameList() {

    }

    interface Listener {
        fun onClickApplyList()
    }
}