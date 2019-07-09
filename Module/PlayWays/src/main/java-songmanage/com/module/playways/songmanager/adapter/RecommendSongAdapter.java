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

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.R;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Common.StandPlayType;

public class RecommendSongAdapter extends DiffAdapter<SongModel, RecyclerView.ViewHolder> {

    boolean isOwner;
    RecyclerOnItemClickListener<SongModel> mListener;

    public RecommendSongAdapter(boolean isOwner, RecyclerOnItemClickListener<SongModel> l) {
        this.isOwner = isOwner;
        this.mListener = l;
    }

    Drawable pk = new DrawableCreator.Builder()
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f))
            .setCornersRadius(U.getDisplayUtils().dip2px(10))
            .setSolidColor(Color.parseColor("#CB5883"))
            .build();

    Drawable togather = new DrawableCreator.Builder()
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f))
            .setCornersRadius(U.getDisplayUtils().dip2px(10))
            .setSolidColor(Color.parseColor("#7088FF"))
            .build();

    Drawable game = new DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#61B14F"))
            .setCornersRadius(U.getDisplayUtils().dip2px(10))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f))
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .build();

    Drawable freeMic = new DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#C856E0"))
            .setCornersRadius(U.getDisplayUtils().dip2px(10))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f))
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .build();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_song_item_layout, parent, false);
        ItemHolder viewHolder = new ItemHolder(view, isOwner);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SongModel model = mDataList.get(position);

        ItemHolder reportItemHolder = (ItemHolder) holder;
        reportItemHolder.bind(model, position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ExTextView mSongNameTv;
        ExTextView mSelectTv;
        ExTextView mSongTag;
        TextView mSongDesc;

        SongModel mSongModel;

        public ItemHolder(View itemView, boolean isOwner) {
            super(itemView);
            mSongNameTv = (ExTextView) itemView.findViewById(R.id.song_name_tv);
            mSelectTv = (ExTextView) itemView.findViewById(R.id.select_tv);
            mSongTag = (ExTextView) itemView.findViewById(R.id.song_tag);
            mSongDesc = (TextView) itemView.findViewById(R.id.song_desc);

            if (isOwner) {
                mSelectTv.setText("点歌");
            } else {
                mSelectTv.setText("想唱");
            }

            mSelectTv.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    if (mListener != null) {
                        mListener.onItemClicked(v, -1, mSongModel);
                    }
                }
            });
        }

        public void bind(SongModel model, int position) {
            mSongModel = model;
            mSongNameTv.setText("《" + model.getDisplaySongName() + "》");
            mSongTag.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(mSongModel.getSongDesc())) {
                mSongDesc.setVisibility(View.GONE);
            } else {
                mSongDesc.setVisibility(View.VISIBLE);
                mSongDesc.setText(mSongModel.getSongDesc());
            }
            if (model.getPlayType() == StandPlayType.PT_SPK_TYPE.getValue()) {
                mSongTag.setBackground(pk);
                mSongTag.setText("PK");
            } else if (model.getPlayType() == StandPlayType.PT_CHO_TYPE.getValue()) {
                mSongTag.setBackground(togather);
                mSongTag.setText("合唱");
            } else if (model.getPlayType() == StandPlayType.PT_MINI_GAME_TYPE.getValue()) {
                mSongTag.setBackground(game);
                mSongTag.setText("双人游戏");
            } else if (model.getPlayType() == StandPlayType.PT_FREE_MICRO.getValue()) {
                ;
                mSongTag.setBackground(freeMic);
                mSongTag.setText("多人游戏");
            } else {
                mSongTag.setVisibility(View.GONE);
            }
        }
    }
}