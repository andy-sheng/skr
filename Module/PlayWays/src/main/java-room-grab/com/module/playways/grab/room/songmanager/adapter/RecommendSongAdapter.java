package com.module.playways.grab.room.songmanager.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.DiffAdapter;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.songmanager.event.AddSongEvent;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Common.StandPlayType;

import org.greenrobot.eventbus.EventBus;

public class RecommendSongAdapter extends DiffAdapter<SongModel, RecyclerView.ViewHolder> {
    GrabRoomData mGrabRoomData;
    Drawable pk = new DrawableCreator.Builder()
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1))
            .setCornersRadius(U.getDisplayUtils().dip2px(70))
            .setSolidColor(Color.parseColor("#CB5883"))
            .build();

    Drawable togather = new DrawableCreator.Builder()
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1))
            .setCornersRadius(U.getDisplayUtils().dip2px(70))
            .setSolidColor(Color.parseColor("#7088FF"))
            .build();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_song_item, parent, false);
        ItemHolder viewHolder = new ItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SongModel model = mDataList.get(position);

        ItemHolder reportItemHolder = (ItemHolder) holder;
        reportItemHolder.bind(model, position);
    }

    public void setGrabRoomData(GrabRoomData grabRoomData) {
        mGrabRoomData = grabRoomData;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ExTextView mSongNameTv;
        ExTextView mHasSingNumTv;
        ExTextView mSelectTv;
        ExTextView mSongTag;

        SongModel mSongModel;

        public ItemHolder(View itemView) {
            super(itemView);
            mSongNameTv = (ExTextView) itemView.findViewById(R.id.song_name_tv);
            mHasSingNumTv = (ExTextView) itemView.findViewById(R.id.has_sing_num_tv);
            mSelectTv = (ExTextView) itemView.findViewById(R.id.select_tv);
            mSongTag = (ExTextView) itemView.findViewById(R.id.song_tag);
            mSelectTv.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    EventBus.getDefault().post(new AddSongEvent(mSongModel));
                }
            });
        }

        public void bind(SongModel model, int position) {
            mSongModel = model;
            mSongNameTv.setText("《" + model.getDisplaySongName() + "》");
            mHasSingNumTv.setText(model.getSingCount() + "人唱过");
            mSongTag.setVisibility(View.VISIBLE);
            if (model.getPlayType() == StandPlayType.PT_SPK_TYPE.getValue()) {
                mSongTag.setBackground(pk);
                mSongTag.setText("PK");
            } else if (model.getPlayType() == StandPlayType.PT_CHO_TYPE.getValue()) {
                mSongTag.setBackground(togather);
                mSongTag.setText("合唱");
            } else {
                mSongTag.setVisibility(View.GONE);
            }
        }
    }
}