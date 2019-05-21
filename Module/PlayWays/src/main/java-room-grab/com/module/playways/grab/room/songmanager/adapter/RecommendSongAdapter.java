package com.module.playways.grab.room.songmanager.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExFrameLayout;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.songmanager.event.AddSongEvent;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Common.ESex;

import org.greenrobot.eventbus.EventBus;

public class RecommendSongAdapter extends DiffAdapter<SongModel, RecyclerView.ViewHolder> {
    GrabRoomData mGrabRoomData;

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
        ExFrameLayout mFrameLayout;
        BaseImageView mSingerIconIv;
        ExTextView mSongNameTv;
        ExTextView mSingerNameTv;
        ExTextView mSelectTv;

        SongModel mSongModel;

        public ItemHolder(View itemView) {
            super(itemView);
            mFrameLayout = (ExFrameLayout) itemView.findViewById(R.id.frame_layout);
            mSingerIconIv = (BaseImageView) itemView.findViewById(R.id.singer_icon_iv);
            mSongNameTv = (ExTextView) itemView.findViewById(R.id.song_name_tv);
            mSingerNameTv = (ExTextView) itemView.findViewById(R.id.singer_name_tv);
            mSelectTv = (ExTextView) itemView.findViewById(R.id.select_tv);
            mSelectTv.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    EventBus.getDefault().post(new AddSongEvent(mSongModel));
                }
            });
        }

        public void bind(SongModel model, int position) {
            mSongModel = model;
            mSongNameTv.setText(model.getItemName());
            mSingerNameTv.setText(model.getOwner());
            AvatarUtils.loadAvatarByUrl(mSingerIconIv,
                    AvatarUtils.newParamsBuilder(model.getCover())
                            .setCornerRadius(U.getDisplayUtils().dip2px(5))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setBorderColorBySex(MyUserInfoManager.getInstance().getSex() == ESex.SX_MALE.getValue())
                            .build());
        }
    }
}