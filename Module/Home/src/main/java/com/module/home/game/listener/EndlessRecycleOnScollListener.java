package com.module.home.game.listener;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessRecycleOnScollListener extends RecyclerView.OnScrollListener {

    // 用来标记是否正在向左滑动
    private boolean isSlidingToLeft = false;

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
        // 当不滑动时
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            // 获取最后一个完全显示的itemPosition
            int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();
            int itemCount = manager.getItemCount();

            // 判断是否滑动到了最后一个item，并且是向左滑动
            if (lastItemPosition == (itemCount - 1) && isSlidingToLeft) {
                // 加载更多
                onLoadMore();
            }
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        // dx值大于0表示正在向左滑动，小于或等于0表示向右滑动或停止
        isSlidingToLeft = dx > 0;
    }

    public abstract void onLoadMore();
}
