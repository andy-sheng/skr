package com.example.rxretrofit.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.common.rxretrofit.download.DownInfo;
import com.common.rxretrofit.download.DownLoadListener.HttpDownOnNextListener;
import com.common.rxretrofit.download.HttpDownManager;
import com.common.utils.U;
import com.wali.live.moduletest.R;

import java.util.List;

public class DownAdapter extends RecyclerView.Adapter {

    Context mContext;
    List<DownInfo> mDatas;

    public DownAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<DownInfo> list) {
        this.mDatas = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.down_load_recycle_item, parent, false);
        return new RecycleHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DownInfo downInfo = mDatas.get(position);
        ((RecycleHolder) holder).bindData(downInfo);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

}