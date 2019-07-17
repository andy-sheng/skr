package com.module.home.game.listener

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

abstract class EndlessRecycleOnScollListener : RecyclerView.OnScrollListener() {

    // 用来标记是否正在向左滑动
    private var isSlidingToLeft = false

    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        val manager = recyclerView!!.layoutManager as LinearLayoutManager
        // 当不滑动时
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            // 获取最后一个完全显示的itemPosition
            val lastItemPosition = manager.findLastCompletelyVisibleItemPosition()
            val itemCount = manager.itemCount

            // 判断是否滑动到了最后一个item，并且是向左滑动
            if (lastItemPosition == itemCount - 1 && isSlidingToLeft) {
                // 加载更多
                onLoadMore()
            }
        }
    }

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        // dx值大于0表示正在向左滑动，小于或等于0表示向右滑动或停止
        isSlidingToLeft = dx > 0
    }

    abstract fun onLoadMore()
}
