package com.module.rankingmode.room.viewer;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.BaseImageView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rankingmode.R;
import com.module.rankingmode.room.viewer.RoomViewerModel;

public class RoomViewerHolder extends RecyclerView.ViewHolder {
    RoomViewerModel mRoomViewerModel;

    RecyclerOnItemClickListener mRecyclerOnItemClickListener;
    BaseImageView mAvatarIv;

    public RoomViewerHolder(View itemView) {
        super(itemView);
        mAvatarIv = (BaseImageView) itemView.findViewById(R.id.avatar_iv);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecyclerOnItemClickListener != null) {
                    mRecyclerOnItemClickListener.onItemClicked(itemView, -1, mRoomViewerModel);
                }
            }
        });
    }

    public void bind(int position, RoomViewerModel roomViewerModel) {
        mRoomViewerModel = roomViewerModel;
        long uid = Long.parseLong(roomViewerModel.getUserId());
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                .setCircle(true)
                .build()
        );
    }

    public void setListener(RecyclerOnItemClickListener recyclerOnItemClickListener) {
        mRecyclerOnItemClickListener = recyclerOnItemClickListener;
    }
}
