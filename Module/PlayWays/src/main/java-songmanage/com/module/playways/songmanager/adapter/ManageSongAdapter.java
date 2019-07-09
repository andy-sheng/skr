package com.module.playways.songmanager.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.DiffAdapter;
import com.module.playways.R;
import com.module.playways.songmanager.SongManageData;
import com.module.playways.songmanager.model.GrabRoomSongModel;
import com.zq.live.proto.Common.StandPlayType;

public class ManageSongAdapter extends DiffAdapter<GrabRoomSongModel, RecyclerView.ViewHolder> {
    OnClickDeleteListener mOnClickDeleteListener;

    SongManageData mGrabRoomData;

    Drawable mGrayDrawable;
    Drawable mRedDrawable;

    Drawable mChorusDrawable;
    Drawable mPKDrawable;
    Drawable mMiniGameDrawable;
    Drawable mFreeMicDrawable;

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

        mFreeMicDrawable = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#C856E0"))
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
        MyLog.d(TAG, "onBindViewHolder" + " model=" + model + " position=" + position);
        ItemHolder reportItemHolder = (ItemHolder) holder;
        reportItemHolder.bind(model, position);
    }

    public void setGrabRoomData(SongManageData grabRoomData) {
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
        TextView mTvSongDesc;
        ExTextView mTvManage;

        GrabRoomSongModel mSongModel;
        int mPosition;

        public ItemHolder(View itemView) {
            super(itemView);
            mTvSongName = (ExTextView) itemView.findViewById(R.id.tv_song_name);
            mSongTagTv = (TextView) itemView.findViewById(R.id.song_tag_tv);
            mTvManage = (ExTextView) itemView.findViewById(R.id.tv_manage);
            mTvSongDesc = (TextView) itemView.findViewById(R.id.tv_song_desc);

            mTvManage.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    if (mOnClickDeleteListener != null) {
                        mOnClickDeleteListener.onClick(mSongModel);
                    }
                }
            });
        }

        public void bind(GrabRoomSongModel model, int position) {
            this.mSongModel = model;
            this.mPosition = position;

            mTvManage.setText("");
            mTvManage.setEnabled(false);

            if (TextUtils.isEmpty(model.getSongDesc())) {
                mTvSongDesc.setVisibility(View.GONE);
            } else {
                mTvSongDesc.setVisibility(View.VISIBLE);
                mTvSongDesc.setText(model.getSongDesc());
            }

            if (mGrabRoomData.isDoubleRoom()) {
                if (model.isCouldDelete()) {
                    mTvManage.setVisibility(View.VISIBLE);
                    mTvManage.setText("删除");
                    mTvManage.setEnabled(true);
                    mTvManage.setBackground(mRedDrawable);
                } else {
                    mTvManage.setVisibility(View.GONE);
                }
            } else {
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
            }

            if (model.getPlayType() == StandPlayType.PT_SPK_TYPE.getValue()) {
                mSongTagTv.setText("PK");
                mSongTagTv.setVisibility(View.VISIBLE);
                mSongTagTv.setBackground(mPKDrawable);
                mTvSongName.setText("《" + model.getDisplaySongName() + "》");
            } else if (model.getPlayType() == StandPlayType.PT_CHO_TYPE.getValue()) {
                mSongTagTv.setText("合唱");
                mSongTagTv.setVisibility(View.VISIBLE);
                mSongTagTv.setBackground(mChorusDrawable);
                mTvSongName.setText("《" + model.getDisplaySongName() + "》");
            } else if (model.getPlayType() == StandPlayType.PT_MINI_GAME_TYPE.getValue()) {
                mSongTagTv.setText("双人游戏");
                mSongTagTv.setVisibility(View.VISIBLE);
                mSongTagTv.setBackground(mMiniGameDrawable);
                mTvSongName.setText("【" + model.getItemName() + "】");
            } else if (model.getPlayType() == StandPlayType.PT_FREE_MICRO.getValue()) {
                mSongTagTv.setText("多人游戏");
                mSongTagTv.setVisibility(View.VISIBLE);
                mSongTagTv.setBackground(mFreeMicDrawable);
                mTvSongName.setText("【" + model.getItemName() + "】");
            } else {
                mSongTagTv.setVisibility(View.GONE);
                mTvSongName.setText("《" + model.getDisplaySongName() + "》");
            }
        }
    }

    public interface OnClickDeleteListener {
        void onClick(GrabRoomSongModel grabRoomSongModel);
    }
}