package com.wali.live.watchsdk.channel.holder;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.View;

import com.wali.live.watchsdk.R;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 可滚动的item基类
 */
public abstract class RecyclerHolder extends HeadHolder {
    protected RecyclerView mRecyclerView;
    protected LayoutManager mLayoutManager;

    public RecyclerHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentView() {
        mRecyclerView = $(R.id.recycler_view);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        setLayoutManager();
    }

    protected void setLayoutManager() {
        mLayoutManager = new LiveLinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }
}
