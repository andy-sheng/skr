package com.wali.live.modulechannel.adapter.item;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.adapter.item.holder.TopicGridItemHolder;
import com.wali.live.modulechannel.model.viewmodel.ChannelNavigateViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 15-12-3.
 *
 * @module 频道
 * @description TopicItem
 */
public class TopicGridRecyclerAdapter extends RecyclerView.Adapter<TopicGridItemHolder> {
    public static final String TAG = TopicGridRecyclerAdapter.class.getSimpleName();

    private List<ChannelNavigateViewModel.NavigateItem> mDatas = new ArrayList();

    public TopicGridRecyclerAdapter() {
    }

    public void setData(List<ChannelNavigateViewModel.NavigateItem> datas) {
        mDatas = datas;
        notifyDataSetChanged();
    }

    public ChannelNavigateViewModel.NavigateItem getData(int position) {
        if (position < 0 || position >= mDatas.size()) {
            return null;
        }
        return mDatas.get(position);
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public TopicGridItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_topic_grid_item, parent, false);
        return new TopicGridItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final TopicGridItemHolder holder, final int position) {
        holder.bindModel(mDatas.get(position));
    }
}
