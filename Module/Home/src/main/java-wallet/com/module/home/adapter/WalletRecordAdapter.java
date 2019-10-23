package com.module.home.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.DiffAdapter;
import com.module.home.R;
import com.module.home.databinding.WalletRecordItemLayoutBinding;
import com.module.home.model.WalletRecordModel;

public class WalletRecordAdapter extends DiffAdapter<WalletRecordModel, RecyclerView.ViewHolder> {

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        WalletRecordItemLayoutBinding walletRecordItemLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.wallet_record_item_layout, parent, false);
        WalletRecordItemHolder viewHolder = new WalletRecordItemHolder(walletRecordItemLayoutBinding.getRoot());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WalletRecordModel model = mDataList.get(position);
        WalletRecordItemLayoutBinding walletRecordItemLayoutBinding = DataBindingUtil.getBinding(holder.itemView);
        walletRecordItemLayoutBinding.setModel(model);
        walletRecordItemLayoutBinding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class WalletRecordItemHolder extends RecyclerView.ViewHolder {
        public WalletRecordItemHolder(View itemView) {
            super(itemView);
        }
    }
}
