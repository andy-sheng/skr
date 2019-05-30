package com.module.playways.grab.room.songmanager.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.DiffAdapter;

import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.R;
import com.module.playways.grab.room.songmanager.model.GrabRoomSongModel;
import com.zq.live.proto.Common.StandPlayType;

public class ManageSongAdapter extends DiffAdapter<GrabRoomSongModel, RecyclerView.ViewHolder> {
    OnClickDeleteListener mOnClickDeleteListener;

    GrabRoomData mGrabRoomData;

    Drawable mGrayDrawable;
    Drawable mRedDrawable;

    Drawable mChorusDrawable;
    Drawable mPKDrawable;
    Drawable mMiniGameDrawable;

    public ManageSongAdapter() {
        mGrayDrawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                .setSolidColor(Color.parseColor("#B1AC99"))
                .setStrokeColor(Color.parseColor("#3B4E79"))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f))
                .setCornersRadius(U.getDisplayUtils().dip2px(16))
                .build();
        mRedDrawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                .setSolidColor(Color.parseColor("#FF8AB6"))
                .setStrokeColor(Color.parseColor("#3B4E79"))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f))
                .setCornersRadius(U.getDisplayUtils().dip2px(16))
                .build();

        mChorusDrawable = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#7088FF"))
                .setCornersRadius(U.getDisplayUtils().dip2px(10))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f))
                .setStrokeColor(U.getColor(R.color.white_trans_70))
                .build();

        mPKDrawable = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#E55088"))
                .setCornersRadius(U.getDisplayUtils().dip2px(10))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f))
                .setStrokeColor(U.getColor(R.color.white_trans_70))
                .build();

        mMiniGameDrawable = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#61B14F"))
                .setCornersRadius(U.getDisplayUtils().dip2px(10))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f))
                .setStrokeColor(U.getColor(R.color.white_trans_70))
                .build();
    }

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
        TextView mSongTagTv;
        ExTextView mTvManage;

        GrabRoomSongModel mSongModel;
        int mPosition;

        public ItemHolder(View itemView) {
            super(itemView);
            mTvSongName = (ExTextView) itemView.findViewById(R.id.tv_song_name);
            mSongTagTv = (TextView) itemView.findViewById(R.id.song_tag_tv);
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
            this.mPosition = position;

            if (mGrabRoomData.hasGameBegin()) {
                if (mGrabRoomData.getRealRoundSeq() == model.getRoundSeq()) {
                    mTvManage.setEnabled(false);
                    mTvManage.setText("演唱中");
                    mTvManage.setBackground(mGrayDrawable);
                } else if (mGrabRoomData.getRealRoundSeq() + 1 == model.getRoundSeq()) {
                    mTvManage.setEnabled(false);
                    mTvManage.setText("已加载");
                    mTvManage.setBackground(mGrayDrawable);
                } else {
                    mTvManage.setText("删除");
                    mTvManage.setEnabled(true);
                    mTvManage.setBackground(mRedDrawable);
                }
            } else {
                if (position == 0) {
                    mTvManage.setText("演唱中");
                    mTvManage.setEnabled(false);
                    mTvManage.setBackground(mGrayDrawable);
                } else if (position == 1) {
                    mTvManage.setText("已加载");
                    mTvManage.setEnabled(false);
                    mTvManage.setBackground(mGrayDrawable);
                } else {
                    mTvManage.setText("删除");
                    mTvManage.setEnabled(true);
                    mTvManage.setBackground(mRedDrawable);
                }
            }

            if (model.getPlayType() == StandPlayType.PT_SPK_TYPE.getValue()) {
                mTvSongName.setPadding(0, 0, U.getDisplayUtils().dip2px(34 + 84), 0);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSongTagTv.getLayoutParams();
                layoutParams.width = U.getDisplayUtils().dip2px(34);
                layoutParams.leftMargin = -U.getDisplayUtils().dip2px(34 + 84);
                mSongTagTv.setLayoutParams(layoutParams);
                mSongTagTv.setText("PK");
                mSongTagTv.setVisibility(View.VISIBLE);
                mSongTagTv.setBackground(mPKDrawable);
                mTvSongName.setText("《" + model.getDisplaySongName() + "》");
            } else if (model.getPlayType() == StandPlayType.PT_CHO_TYPE.getValue()) {
                mTvSongName.setPadding(0, 0, U.getDisplayUtils().dip2px(34 + 84), 0);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSongTagTv.getLayoutParams();
                layoutParams.width = U.getDisplayUtils().dip2px(34 + 84);
                layoutParams.leftMargin = -U.getDisplayUtils().dip2px(34);
                mSongTagTv.setLayoutParams(layoutParams);
                mSongTagTv.setText("合唱");
                mSongTagTv.setVisibility(View.VISIBLE);
                mSongTagTv.setBackground(mChorusDrawable);
                mTvSongName.setText("《" + model.getDisplaySongName() + "》");
            } else if (model.getPlayType() == StandPlayType.PT_MINI_GAME_TYPE.getValue()) {
                mTvSongName.setPadding(0, 0, U.getDisplayUtils().dip2px(58 + 84), 0);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSongTagTv.getLayoutParams();
                U.getDisplayUtils().dip2px(58);
                layoutParams.leftMargin = -U.getDisplayUtils().dip2px(58 + 84);
                mSongTagTv.setLayoutParams(layoutParams);
                mSongTagTv.setText("双人游戏");
                mSongTagTv.setVisibility(View.VISIBLE);
                mSongTagTv.setBackground(mMiniGameDrawable);
                mTvSongName.setText("【" + model.getItemName() + "】");
            } else {
                mTvSongName.setPadding(0, 0, U.getDisplayUtils().dip2px(84), 0);
                mSongTagTv.setVisibility(View.GONE);
                mTvSongName.setText("《" + model.getDisplaySongName() + "》");
            }
        }
    }

    public interface OnClickDeleteListener {
        void onClick(GrabRoomSongModel grabRoomSongModel);
    }
}