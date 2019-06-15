package com.moudule.playways.beauty.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.R;

import java.util.List;

public class BeautyFiterPaterAdapter extends RecyclerView.Adapter {

    List<BeautyControlPanelView.BeautyViewModel> mDataList;

    int mSelectPosition = 0;  //默认选中的位置

    RecyclerOnItemClickListener<BeautyControlPanelView.BeautyViewModel> mClickListener;
    RecyclerView.LayoutManager mLayoutManager;


    public BeautyFiterPaterAdapter(RecyclerOnItemClickListener<BeautyControlPanelView.BeautyViewModel> listener, RecyclerView.LayoutManager layoutManager) {
        mClickListener = listener;
        mLayoutManager = layoutManager;
    }

    public void setDataList(List<BeautyControlPanelView.BeautyViewModel> dataList) {
        mDataList = dataList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.beauty_holder_item_layout, parent, false);
        BeautyHolder viewHolder = new BeautyHolder(view, mClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BeautyControlPanelView.BeautyViewModel model = mDataList.get(position);
        if (mSelectPosition == position) {
            ((BeautyHolder) holder).bindData(model, position, true);
        } else {
            ((BeautyHolder) holder).bindData(model, position, false);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void setSelectPosition(int selectPosition) {
        if (mSelectPosition != selectPosition) {
            BeautyHolder oldHolder = getHolderByPosition(mSelectPosition);
            if (oldHolder != null) {
                oldHolder.setIsSelected(false);
            }
            mSelectPosition = selectPosition;
            BeautyHolder newHolder = getHolderByPosition(selectPosition);
            if (newHolder != null) {
                newHolder.setIsSelected(true);
            }
        }
    }

    BeautyHolder getHolderByPosition(int playPosition) {
        if (playPosition >= 0) {
            View view = mLayoutManager.findViewByPosition(playPosition);
            if (view != null) {
                return (BeautyHolder) view.getTag();
            }
        }
        return null;
    }
}
