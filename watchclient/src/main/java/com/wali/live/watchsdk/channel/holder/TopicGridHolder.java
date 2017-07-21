package com.wali.live.watchsdk.channel.holder;

import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.channel.adapter.item.TopicGridRecyclerAdapter;
import com.wali.live.watchsdk.channel.decoration.TopicItemDecoration;
import com.wali.live.watchsdk.channel.viewmodel.ChannelNavigateViewModel;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 可滚动方形图片的item
 */
public class TopicGridHolder extends RecyclerHolder {
    public static final int SPAN_COUNT = 3;

    protected TopicGridRecyclerAdapter mAdapter;

    public TopicGridHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentView() {
        super.initContentView();
        mAdapter = new TopicGridRecyclerAdapter();

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mRecyclerView.getLayoutParams();
        lp.topMargin = DisplayUtils.dip2px(5f);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new TopicItemDecoration(TopicItemDecoration.GRID_LIST));
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        mSplitLine.setVisibility(View.GONE);
    }

    @Override
    protected void bindView() {
        super.bindView();
        ChannelNavigateViewModel model = mViewModel.get();
        mAdapter.setData(model.getItemDatas());
    }

    @Override
    protected void setLayoutManager() {
        mLayoutManager = new GridLayoutManager(itemView.getContext(), SPAN_COUNT);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }
}
