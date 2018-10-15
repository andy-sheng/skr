package com.wali.live.watchsdk.channel.adapter.item;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.holder.item.TopicGridItemHolder;
import com.wali.live.watchsdk.channel.viewmodel.ChannelNavigateViewModel.NavigateItem;

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

    private List<NavigateItem> mDatas = new ArrayList();

    public TopicGridRecyclerAdapter() {
    }

    public void setData(List<NavigateItem> datas) {
        mDatas = datas;
        notifyDataSetChanged();
    }

    public NavigateItem getData(int position) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.michannel_topic_grid_item, parent, false);
        return new TopicGridItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final TopicGridItemHolder holder, final int position) {
        holder.bindModel(mDatas.get(position));
    }
}
