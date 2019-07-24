package com.module.playways.grab.room.production;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.DiffAdapter;
import com.module.playways.R;
import com.module.playways.grab.room.model.WorksUploadModel;

public class ResultProducationAdapter extends DiffAdapter<WorksUploadModel, ResultProducationViewHolder> {

    Listener mListener;
    int mPlayPosition = -1;  //选中播放的id
    LinearLayoutManager mLinearLayoutManager;

    public ResultProducationAdapter(Listener listener, LinearLayoutManager linearLayoutManager) {
        this.mListener = listener;
        this.mLinearLayoutManager = linearLayoutManager;
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
        WorksUploadModel wonderfulMomentModel = mDataList.get(position);
        if (mPlayPosition == position) {
            holder.bindData(position, wonderfulMomentModel, true);
        } else {
            holder.bindData(position, wonderfulMomentModel, false);
        }
    }

    public int getPlayPosition() {
        return mPlayPosition;
    }

    public void setPlayPosition(int selectPlayPosition) {
        if (mPlayPosition != selectPlayPosition) {
            ResultProducationViewHolder holder1 = getHolderByPosition(mPlayPosition);
            if (holder1 != null) {
                holder1.setPlayBtn(false);
            }
            mPlayPosition = selectPlayPosition;
            ResultProducationViewHolder holder2 = getHolderByPosition(mPlayPosition);
            if (holder2 != null) {
                holder2.setPlayBtn(true);
            }
        }
    }

    ResultProducationViewHolder getHolderByPosition(int playPosition) {
        if (playPosition >= 0) {
            View view = mLinearLayoutManager.findViewByPosition(mPlayPosition);
            if (view != null) {
                return (ResultProducationViewHolder) view.getTag();
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }



    public interface Listener {
        void onClickPlayBtn(View view,boolean play,int position, WorksUploadModel model);

        void onClickSaveAndShare(int position, WorksUploadModel model);
    }

}
