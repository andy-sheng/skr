package com.module.playways.grab.room.songmanager;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;

import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.R;
import com.zq.live.proto.Common.StandPlayType;

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

    public void deleteSong(GrabRoomSongModel grabRoomSongModel) {
        int position = -1;
        for (int i = 0; i < mDataList.size(); i++) {
            if (mDataList.get(i) == grabRoomSongModel) {
                position = i;
            }
        }

        if (position >= 0) {
            mDataList.remove(position);
            notifyItemRemoved(position);//注意这里
            if (position != mDataList.size()) {
                notifyItemRangeChanged(position, mDataList.size() - position);
            }
        }
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
        int mPosition;

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
            mPosition = position;
            mTvSongName.setText(model.getItemName());
            mTvAuther.setText(model.getOwner());

            if (mGrabRoomData.hasGameBegin()) {
                if (mGrabRoomData.getRealRoundSeq() == model.getRoundSeq()) {
                    mTvManage.setEnabled(false);
                    mTvManage.setBackground(U.getDrawable(R.drawable.fz_yanchangzhong));
                } else if (mGrabRoomData.getRealRoundSeq() + 1 == model.getRoundSeq()) {
                    mTvManage.setEnabled(false);
                    mTvManage.setBackground(U.getDrawable(R.drawable.fz_yijiazai));
                } else {
                    mTvManage.setEnabled(true);
                    mTvManage.setBackground(U.getDrawable(R.drawable.fz_shanchu));
                }
            } else {
                if (position == 0) {
                    mTvManage.setEnabled(false);
                    mTvManage.setBackground(U.getDrawable(R.drawable.fz_yijiazai));
                } else if (position == 1) {
                    mTvManage.setEnabled(false);
                    mTvManage.setBackground(U.getDrawable(R.drawable.fz_yijiazai));
                } else {
                    mTvManage.setEnabled(true);
                    mTvManage.setBackground(U.getDrawable(R.drawable.fz_shanchu));
                }
            }

        }
    }

    public interface OnClickDeleteListener {
        void onClick(GrabRoomSongModel grabRoomSongModel);
    }
}