package com.module.playways.grab.room.production;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.log.MyLog;
import com.common.view.recyclerview.DiffAdapter;
import com.module.playways.R;
import com.module.playways.grab.room.model.WorksUploadModel;

public class ResultProducationAdapter extends DiffAdapter<WorksUploadModel, ResultProducationViewHolder> {

    Listener mListener;
    int mPlayPosition = -1;  //选中播放的id

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

    public void setPlayPosition(int playPosition,boolean refresh) {
        MyLog.d(TAG,"setSelectPosition" + " playPosition=" + playPosition);
        if (mPlayPosition != playPosition) {
            int oldSelectPosition = mPlayPosition;
            mPlayPosition = playPosition;
            if (oldSelectPosition >= 0 && refresh) {
                notifyItemChanged(oldSelectPosition);
            }
//            if (mPlayPosition >= 0) {
//                notifyItemChanged(mPlayPosition);
//            }
        }
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
