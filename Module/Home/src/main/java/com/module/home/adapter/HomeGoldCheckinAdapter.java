package com.module.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;
import com.module.home.R;
import com.module.home.model.HomeGoldModel;

public class HomeGoldCheckinAdapter extends DiffAdapter<HomeGoldModel, RecyclerView.ViewHolder> {
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.check_in_item_layout, parent, false);
        ItemHolder viewHolder = new ItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HomeGoldModel model = mDataList.get(position);

        ItemHolder reportItemHolder = (ItemHolder) holder;
        reportItemHolder.bind(model);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        HomeGoldModel mHomeGoldModel;
        ExTextView mTvNum;
        TextView mTvGold;
        ExTextView mTvCover;
        ImageView mOk;
        ImageView mIvGoldBj;

        public ItemHolder(View itemView) {
            super(itemView);
            mTvNum = itemView.findViewById(R.id.tv_num);
            mTvGold = itemView.findViewById(R.id.tv_gold);
            mTvCover = itemView.findViewById(R.id.tv_cover);
            mOk = itemView.findViewById(R.id.ok);
            mIvGoldBj = itemView.findViewById(R.id.iv_gold_bj);
        }

        public void bind(HomeGoldModel model) {
            if(model == null){
                MyLog.e(TAG, "model is null");
                return;
            }

            this.mHomeGoldModel = model;
            mTvNum.setText("0" + model.getSeq());
            HomeGoldModel.BonusesBean bonusesBean = model.getCoinBonuses();
            if (bonusesBean == null) {
                MyLog.e(TAG, "bonusesBean is null");
                return;
            }

            mTvGold.setText(bonusesBean.getAmount() + "金币");

            switch (model.getSeq()) {
                case 1:
                    if (model.getState() == 1) {
                        mIvGoldBj.setBackground(U.getDrawable(R.drawable.wujinbi_dangtian));
                    } else {
                        mIvGoldBj.setBackground(U.getDrawable(R.drawable.wujinbi_moren));
                    }
                    break;
                case 2:
                    if (model.getState() == 1) {
                        mIvGoldBj.setBackground(U.getDrawable(R.drawable.shijinbi_dangtian));
                    } else {
                        mIvGoldBj.setBackground(U.getDrawable(R.drawable.shijinbi_moren));
                    }
                    break;
                case 3:
                    if (model.getState() == 1) {
                        mIvGoldBj.setBackground(U.getDrawable(R.drawable.shijinbi_dangtian));
                    } else {
                        mIvGoldBj.setBackground(U.getDrawable(R.drawable.shijinbi_moren));
                    }
                    break;
                case 4:
                    if (model.getState() == 1) {
                        mIvGoldBj.setBackground(U.getDrawable(R.drawable.shiwujinbi_dangtian));
                    } else {
                        mIvGoldBj.setBackground(U.getDrawable(R.drawable.shiwujinbi_moren));
                    }
                    break;
                case 5:
                    if (model.getState() == 1) {
                        mIvGoldBj.setBackground(U.getDrawable(R.drawable.shiwujinbi_dangtian));
                    } else {
                        mIvGoldBj.setBackground(U.getDrawable(R.drawable.shiwujinbi_moren));
                    }
                    break;
                case 6:
                    if (model.getState() == 1) {
                        mIvGoldBj.setBackground(U.getDrawable(R.drawable.ershijinbi_dangtian));
                    } else {
                        mIvGoldBj.setBackground(U.getDrawable(R.drawable.ershijinbi_moren));
                    }
                    break;
            }

            if (model.getState() == 1) {
                itemView.setBackground(U.getDrawable(R.drawable.qiandao_dangtianbj));
                mTvCover.setVisibility(View.GONE);
            } else if (model.getState() == 2) {
                itemView.setBackground(U.getDrawable(R.drawable.qiandao_morenbj));
                mTvCover.setVisibility(View.VISIBLE);
                mOk.setVisibility(View.VISIBLE);
            } else if (model.getState() == 3) {
                itemView.setBackground(U.getDrawable(R.drawable.qiandao_morenbj));
                mTvCover.setVisibility(View.VISIBLE);
                mOk.setVisibility(View.GONE);
            } else if (model.getState() == 4) {
                itemView.setBackground(U.getDrawable(R.drawable.qiandao_morenbj));
                mTvCover.setVisibility(View.GONE);
            }
        }
    }
}