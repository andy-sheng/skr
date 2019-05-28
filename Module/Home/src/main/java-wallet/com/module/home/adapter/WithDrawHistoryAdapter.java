package com.module.home.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.common.view.recyclerview.DiffAdapter;
import com.module.home.R;
import com.module.home.model.WalletRecordModel;
import com.module.home.model.WithDrawHistoryModel;

public class WithDrawHistoryAdapter extends DiffAdapter<WithDrawHistoryModel, RecyclerView.ViewHolder> {

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.withdraw_history_item, parent, false);
        ItemHolder viewHolder = new ItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WithDrawHistoryModel model = mDataList.get(position);

        ItemHolder reportItemHolder = (ItemHolder) holder;
        reportItemHolder.bind(model);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        TextView mTvWithDraw;
        TextView mTvData;
        TextView mTvCash;
        TextView mTvState;

        WithDrawHistoryModel mWalletRecordModel;

        public ItemHolder(View itemView) {
            super(itemView);
            mTvWithDraw = (TextView) itemView.findViewById(R.id.tv_with_draw);
            mTvData = (TextView) itemView.findViewById(R.id.tv_data);
            mTvCash = (TextView) itemView.findViewById(R.id.tv_cash);
            mTvState = (TextView) itemView.findViewById(R.id.tv_state);
        }

        public void bind(WithDrawHistoryModel model) {
            this.mWalletRecordModel = model;
            mTvWithDraw.setText(model.getDesc());
            mTvData.setText(model.getDateTime());
            mTvState.setText(model.getStatusDesc());
            int state = model.getStatus();

            String result = String.format("%.2f", ((float)model.getAmount() / 100000f) * -1);
            mTvCash.setText(result);

            /**
             * WFS_UNKNOWN = 0; //未知
             *     WFS_AUDIT = 1;//审核中
             *     WFS_SUCCESS =2; //提现成功
             *     WFS_FAIL =3; //提现失败
             */
            if (1 == state) {
                mTvState.setTextColor(Color.parseColor("#EF5E85"));
            } else if (2 == state) {
                mTvState.setTextColor(Color.parseColor("#B7BED5"));
            } else if (3 == state) {
                mTvState.setTextColor(Color.parseColor("#EF5E85"));
            }
        }
    }
}