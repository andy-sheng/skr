package com.module.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.common.view.recyclerview.DiffAdapter;
import com.module.home.R;
import com.module.home.model.WalletRecordModel;

public class WalletRecordAdapter extends DiffAdapter<WalletRecordModel, RecyclerView.ViewHolder> {

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_record_item_layout, parent, false);
        WalletRecordItemHolder viewHolder = new WalletRecordItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WalletRecordModel model = mDataList.get(position);

        WalletRecordItemHolder reportItemHolder = (WalletRecordItemHolder) holder;
        reportItemHolder.bind(model);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class WalletRecordItemHolder extends RecyclerView.ViewHolder {

        TextView mRecordDescTv;
        TextView mRecordTimeTv;
        TextView mRecordNumTv;

        WalletRecordModel mWalletRecordModel;

        public WalletRecordItemHolder(View itemView) {
            super(itemView);
            mRecordDescTv = (TextView) itemView.findViewById(R.id.record_desc_tv);
            mRecordTimeTv = (TextView) itemView.findViewById(R.id.record_time_tv);
            mRecordNumTv = (TextView) itemView.findViewById(R.id.record_num_tv);
        }

        public void bind(WalletRecordModel model) {
            this.mWalletRecordModel = model;
            mRecordDescTv.setText(model.getRemark());
            mRecordTimeTv.setText(model.getDateTime());
            mRecordNumTv.setText(model.getChangeAmount());
        }
    }
}
