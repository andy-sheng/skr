package com.wali.live.watchsdk.contest.rank.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.contest.rank.holder.BaseArrayHolder;
import com.wali.live.watchsdk.contest.rank.holder.ContestRankHolder;
import com.wali.live.watchsdk.contest.rank.holder.ContestRankTopThreeHolder;
import com.wali.live.watchsdk.contest.rank.model.ContestRankItemModel;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 2018/1/11.
 */
public class ContestRankAdapter extends RecyclerView.Adapter<BaseHolder> {
    private static final int TYPE_RANK_TOP = 100;
    private static final int TYPE_RANK = 101;

    private List<ContestRankItemModel> mDataList;

    private boolean mShowTopThree = true;

    public ContestRankAdapter() {
        mDataList = new ArrayList<>();
    }

    public void setDataList(List<ContestRankItemModel> dataList) {
        if (dataList == null || dataList.size() == 0) {
            return;
        }
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mShowTopThree) {
            if (mDataList == null || mDataList.size() == 0) {
                return 0;
            }
            if (mDataList.size() <= 3) {
                return 1;
            }
            return mDataList.size() - 2;
        }
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseHolder holder;
        View view;
        switch (viewType) {
            case TYPE_RANK_TOP:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contest_rank_top_item, parent, false);
                holder = new ContestRankTopThreeHolder(view);
                break;
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contest_rank_item, parent, false);
                holder = new ContestRankHolder(view);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(BaseHolder holder, int position) {
        if (mShowTopThree) {
            if (position == 0) {
                ((BaseArrayHolder) holder).bindModels(mDataList);
            } else {
                holder.bindModel(mDataList.get(position + 2));
            }
            return;
        }
        holder.bindModel(mDataList.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        if (mShowTopThree) {
            if (position == 0) {
                return TYPE_RANK_TOP;
            }
        }
        return TYPE_RANK;
    }
}
