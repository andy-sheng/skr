package com.module.playways.mic.home

import android.support.v7.widget.RecyclerView
import android.view.View
import com.common.core.view.setAnimateDebounceViewClickListener

class RecommendMicHeadHolder(item: View, listener: RecommendMicListener) : RecyclerView.ViewHolder(item) {

    init {
        item.setAnimateDebounceViewClickListener { listener.onClickQuickEnterRoom() }
    }
}