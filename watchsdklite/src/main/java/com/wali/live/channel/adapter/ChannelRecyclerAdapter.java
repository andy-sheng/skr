package com.wali.live.channel.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.log.MyLog;
import com.wali.live.channel.holder.BaseHolder;
import com.wali.live.channel.holder.ThreeCardHolder;
import com.wali.live.channel.holder.TwoCardHolder;
import com.wali.live.channel.viewmodel.BaseViewModel;
import com.wali.live.channel.viewmodel.ChannelUiType;
import com.wali.live.channel.viewmodel.ChannelViewModel;
import com.mi.liveassistant.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 15-12-3.
 *
 * @module 频道
 * @description 频道view的基本适配器
 */
public class ChannelRecyclerAdapter extends RecyclerView.Adapter<BaseHolder> {
    public static final String TAG = ChannelRecyclerAdapter.class.getSimpleName();

    private List<? extends BaseViewModel> mChannelModels = new ArrayList<>();

    private WeakReference<Activity> mActRef;
    private long mChannelId;

    public ChannelRecyclerAdapter(Activity activity, long channelId) {
    }

    public void setChannelId(long channelId) {
        mChannelId = channelId;
    }

    public void setData(List<? extends BaseViewModel> channelModels) {
        mChannelModels = channelModels;
        notifyDataSetChanged();
    }


    public BaseViewModel getData(int position) {
        if (position < 0 || position >= mChannelModels.size()) {
            return null;
        }
        return mChannelModels.get(position);
    }


    @Override
    public int getItemCount() {
        return mChannelModels == null ? 0 : mChannelModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChannelViewModel channelViewModel = mChannelModels.get(position).get();
        return channelViewModel.getUiType();
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseHolder holder = null;
        View view;
        switch (viewType) {
            case ChannelUiType.TYPE_TWO_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.michannel_two_card_item, parent, false);
                holder = new TwoCardHolder(view);
                break;
            case ChannelUiType.TYPE_THREE_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.michannel_three_card_item, parent, false);
                holder = new ThreeCardHolder(view);
                break;
            default:
                MyLog.d(TAG, "viewType is : " + viewType);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(final BaseHolder holder, final int position) {
        if (holder == null) {
            // 此分支不应该进来
            MyLog.e(TAG, "onBindViewHolder error : " + position);
            return;
        }
        holder.bindModel(getDataItem(position), position);
    }

    private BaseViewModel getDataItem(int position) {
        int channelModelPos = position;
        if (channelModelPos < 0 || channelModelPos >= mChannelModels.size()) {
            return null;
        }
        return mChannelModels.get(channelModelPos);
    }
}
