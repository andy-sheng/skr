package com.zq.person.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.zq.person.holder.PhotoViewHolder;
import com.zq.person.model.PhotoModel;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter {
    List<PhotoModel> mDataList = new ArrayList<>();
    RecyclerOnItemClickListener mListener;

    public PhotoAdapter(RecyclerOnItemClickListener mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item_view_layout, parent, false);
        PhotoViewHolder viewHolder = new PhotoViewHolder(view, mListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PhotoModel photoModel = mDataList.get(position);
        ((PhotoViewHolder) holder).bindData(photoModel, position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public List<PhotoModel> getDataList() {
        return mDataList;
    }

    public void setDataList(List<PhotoModel> dataList) {
        mDataList = dataList;
    }
}
