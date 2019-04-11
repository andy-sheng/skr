package com.zq.person.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.zq.person.holder.PhotoUpdateHolder;
import com.zq.person.holder.PhotoViewHolder;
import com.zq.person.model.PhotoModel;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter {

    List<PhotoModel> mDataList = new ArrayList<>();
    RecyclerOnItemClickListener mListener;

    boolean mHasUpdate;

    private int PHOTO_ADD_TYPE = 0;
    private int PHOTO_ITEM_TYPE = 1;

    public PhotoAdapter(RecyclerOnItemClickListener mListener, boolean hasUpdate) {
        this.mListener = mListener;
        this.mHasUpdate = hasUpdate;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == PHOTO_ITEM_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item_view_layout, parent, false);
            PhotoViewHolder viewHolder = new PhotoViewHolder(view, mListener);
            return viewHolder;
        } else if (viewType == PHOTO_ADD_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_add_view_layout, parent, false);
            PhotoUpdateHolder viewHolder = new PhotoUpdateHolder(view, mListener);
            return viewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (mHasUpdate) {
            if (position == 0) {
                ((PhotoUpdateHolder) holder).bindData(position);
            } else {
                PhotoModel photoModel = mDataList.get(position - 1);
                ((PhotoViewHolder) holder).bindData(photoModel, position);
            }
        } else {
            ((PhotoViewHolder) holder).bindData(mDataList.get(position), position - 1);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mHasUpdate) {
            return PHOTO_ADD_TYPE;
        }
        return PHOTO_ITEM_TYPE;
    }

    @Override
    public int getItemCount() {
        if (mHasUpdate) {
            return mDataList.size() + 1;
        }

        return mDataList.size();
    }

    public List<PhotoModel> getDataList() {
        return mDataList;
    }

    public void setDataList(List<PhotoModel> dataList) {
        if (dataList != null) {
            mDataList.clear();
            mDataList.addAll(dataList);
            notifyDataSetChanged();
        }
    }

    /**
     * 头部插入某条数据
     *
     * @param data
     */
    public void insertFirst(PhotoModel data) {
        mDataList.add(0, data);
        if (mHasUpdate) {
            notifyItemInserted(1);
        } else {
            notifyItemInserted(0);
        }
    }

    /**
     * 尾部插入一堆数据
     * @param list
     */
    public void insertLast(List<PhotoModel> list) {
        int origin = mDataList.size();
        mDataList.addAll(list);
        if (mHasUpdate) {
            notifyItemRangeInserted(origin + 1, mDataList.size() - origin + 1);
        } else {
            notifyItemRangeInserted(origin, mDataList.size() - origin);
        }
    }
}
