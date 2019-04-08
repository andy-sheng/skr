package com.zq.person.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.zq.person.holder.PhotoHeadHolder;
import com.zq.person.holder.PhotoViewHolder;
import com.zq.person.model.PhotoModel;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter {

    List<PhotoModel> mDataList = new ArrayList<>();
    RecyclerOnItemClickListener mListener;

    int totalCount;

    private int PHOTO_HEAD_TYPE = 0;
    private int PHOTO_ITEM_TYPE = 1;

    public PhotoAdapter(RecyclerOnItemClickListener mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == PHOTO_ITEM_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item_view_layout, parent, false);
            PhotoViewHolder viewHolder = new PhotoViewHolder(view, mListener);
            return viewHolder;
        } else if (viewType == PHOTO_HEAD_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item_view_layout, parent, false);
            PhotoHeadHolder viewHolder = new PhotoHeadHolder(view);
            return viewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            ((PhotoHeadHolder) holder).bindData(totalCount);
        } else {
            PhotoModel photoModel = mDataList.get(position - 1);
            ((PhotoViewHolder) holder).bindData(photoModel, position - 1);
        }

    }


    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        if (mDataList.size() == 0) {
            return 0;
        }
        return mDataList.size() + 1;
    }

    public List<PhotoModel> getDataList() {
        return mDataList;
    }

    public void setDataList(List<PhotoModel> dataList) {
        mDataList = dataList;
    }
}
