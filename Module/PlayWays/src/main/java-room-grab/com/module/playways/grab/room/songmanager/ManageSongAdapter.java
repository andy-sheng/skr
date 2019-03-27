package com.module.playways.grab.room.songmanager;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;

import com.module.playways.grab.room.GrabRoomData;
import com.module.rank.R;

public class ManageSongAdapter extends DiffAdapter<GrabRoomSongModel, RecyclerView.ViewHolder> {
    OnClickDeleteListener mOnClickDeleteListener;

    GrabRoomData mGrabRoomData;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.manage_song_item_layout, parent, false);
        ItemHolder viewHolder = new ItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GrabRoomSongModel model = mDataList.get(position);

        ItemHolder reportItemHolder = (ItemHolder) holder;
        reportItemHolder.bind(model, position);
    }

    public void setGrabRoomData(GrabRoomData grabRoomData) {
        mGrabRoomData = grabRoomData;
    }

    public void setOnClickDeleteListener(OnClickDeleteListener onClickDeleteListener) {
        mOnClickDeleteListener = onClickDeleteListener;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ExTextView mTvSongName;
        ExTextView mTvAuther;
        ExTextView mTvManage;

        GrabRoomSongModel mSongModel;

        public ItemHolder(View itemView) {
            super(itemView);
            mTvSongName = (ExTextView) itemView.findViewById(R.id.tv_song_name);
            mTvAuther = (ExTextView) itemView.findViewById(R.id.tv_auther);
            mTvManage = (ExTextView) itemView.findViewById(R.id.tv_manage);

            mTvManage.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    mOnClickDeleteListener.onClick(mSongModel);
                }
            });
        }

        public void bind(GrabRoomSongModel model, int position) {
            this.mSongModel = model;
            mTvSongName.setText(model.getItemName());
            mTvAuther.setText(model.getOwner());

            if (mGrabRoomData.hasGameBegin()) {
                if (mGrabRoomData.getRealRoundSeq() == model.getRoundSeq()) {
                    mTvManage.setEnabled(false);
                    mTvManage.setText("演唱中");
                } else if (mGrabRoomData.getRealRoundSeq() + 1 == model.getRoundSeq()) {
                    mTvManage.setEnabled(false);
                    mTvManage.setText("下发中");
                } else {
                    mTvManage.setEnabled(true);
                    mTvManage.setText("删除");
                }
            } else {
                if (position == 0) {
                    mTvManage.setEnabled(false);
                    mTvManage.setText("已加载");
                } else if (position == 1) {
                    mTvManage.setEnabled(false);
                    mTvManage.setText("已加载");
                } else {
                    mTvManage.setEnabled(true);
                    mTvManage.setText("删除");
                }
            }

        }
    }

    public interface OnClickDeleteListener {
        void onClick(GrabRoomSongModel grabRoomSongModel);
    }
}