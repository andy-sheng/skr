package com.module.playways.grab.room.view.video;

import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.common.core.myinfo.MyUserInfoManager;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.view.ExViewStub;
import com.zq.mediaengine.kit.ZqEngineKit;

public class GrabVideoView extends ExViewStub {
    //    RelativeLayout mParentView;
    TextureView mMainVideoView;
//    TextureView mSubVideoView;

    private GrabRoomData mRoomData;

    public GrabVideoView(ViewStub viewStub) {
        super(viewStub);
    }

    @Override
    protected void init(View parentView) {
        mMainVideoView = (TextureView) mParentView;
    }

    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
    }

    public void bindVideoStream(int userId) {
        tryInflate();
        mMainVideoView.setVisibility(View.VISIBLE);
//        mSubVideoView.setVisibility(View.GONE);
        ZqEngineKit.getInstance().setDisplayPreview(mMainVideoView);

        if (userId == MyUserInfoManager.getInstance().getUid()) {
            // 是自己
            ZqEngineKit.getInstance().startCameraPreview(0);
            ZqEngineKit.getInstance().setLocalVideoRect(0, 0, 1, 1, 1);
        } else {
            ZqEngineKit.getInstance().setRemoteVideoRect(userId, 0, 1, 1, 1, 1);
        }
    }

    public void bindVideoStream(int userID1, int userID2) {
        tryInflate();
        mMainVideoView.setVisibility(View.VISIBLE);
//        mSubVideoView.setVisibility(View.GONE);
    }
}
