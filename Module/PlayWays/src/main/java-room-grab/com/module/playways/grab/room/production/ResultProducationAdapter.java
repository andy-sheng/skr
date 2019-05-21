package com.module.playways.grab.room.production;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.DiffAdapter;
import com.module.playways.R;
import com.module.playways.grab.room.model.WonderfulMomentModel;

public class ResultProducationAdapter extends DiffAdapter<WonderfulMomentModel, ResultProducationViewHolder> {

    Listener mListener;
    int mSelectPosition = -1;  //选中播放的id

    public ResultProducationAdapter(Listener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ResultProducationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_production_item_view_layout, parent, false);
        ResultProducationViewHolder viewHolder = new ResultProducationViewHolder(view, mListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ResultProducationViewHolder holder, int position) {
        WonderfulMomentModel wonderfulMomentModel = mDataList.get(position);
        if (mSelectPosition == position) {
            holder.bindData(position, wonderfulMomentModel, true);
        } else {
            holder.bindData(position, wonderfulMomentModel, false);
        }
    }


    public int getSelectPosition() {
        return mSelectPosition;
    }

    public void setSelectPosition(int selectPosition) {
        mSelectPosition = selectPosition;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public interface Listener {
        void onClickPlay(int position, WonderfulMomentModel model);

        void onClickPause(int position, WonderfulMomentModel model);

        void onClickSaveAndShare(int position, WonderfulMomentModel model);
    }

}
