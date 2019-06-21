package com.module.home.ranked.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.module.home.R;
import com.module.home.ranked.model.RankDataModel;
import com.module.home.ranked.view.RankedDetailViewHolder;
import com.module.home.ranked.view.RankedDuanViewHolder;

import java.util.ArrayList;
import java.util.List;

import static com.module.home.ranked.model.RankDataModel.USER_RANKING;

public class RankedDetailAdapter extends RecyclerView.Adapter {


    public static final int MEILI_REWARD_VIEWTYPE = 1; // 魅力和打赏榜
    public static final int DUAN_VIEWTYPE = 2;         // 段位排行榜

    private List<RankDataModel> mDataList;

    public RankedDetailAdapter() {
        mDataList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == DUAN_VIEWTYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ranked_duan_item_layout, parent, false);
            RankedDuanViewHolder viewHolder = new RankedDuanViewHolder(view);
            return viewHolder;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ranked_detail_item_layout, parent, false);
            RankedDetailViewHolder viewHolder = new RankedDetailViewHolder(view);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RankDataModel model = mDataList.get(position);
        if (holder instanceof RankedDuanViewHolder) {
            ((RankedDuanViewHolder) holder).bindData(position, model);
        } else if (holder instanceof RankedDetailViewHolder) {
            ((RankedDetailViewHolder) holder).bindData(position, model);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataList != null && mDataList.size() > 0) {
            RankDataModel model = mDataList.get(position);
            if (model.getVType() == USER_RANKING) {
                return DUAN_VIEWTYPE;
            } else {
                return MEILI_REWARD_VIEWTYPE;
            }
        }
        return super.getItemViewType(position);
    }

    public List<RankDataModel> getDataList() {
        return mDataList;
    }

    public void setDataList(List<RankDataModel> dataList) {
        mDataList = dataList;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }
}
