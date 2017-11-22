package com.wali.live.watchsdk.fans.pay.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.pay.holder.FansPayHolder;
import com.wali.live.watchsdk.fans.pay.model.FansPayModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 2017/11/21.
 */
public class FansPayAdapter extends RecyclerView.Adapter<FansPayHolder> {
    private List<FansPayModel> mDataList;
    private FansPayModel mSelectedItem;

    public FansPayAdapter() {
        mDataList = new ArrayList();
    }

    public void setDataList(List<FansPayModel> list) {
        mDataList.clear();
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    public FansPayModel getSelectedItem() {
        return mSelectedItem;
    }

    public void setSelectedItem(FansPayModel selectedItem) {
        mSelectedItem = selectedItem;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public FansPayHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fans_pay_item, parent, false);
        return new FansPayHolder(view, this);
    }

    @Override
    public void onBindViewHolder(FansPayHolder holder, int position) {
        holder.bindModel(mDataList.get(position));
    }
}
