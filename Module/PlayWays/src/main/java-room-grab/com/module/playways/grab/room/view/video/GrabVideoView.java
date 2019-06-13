package com.module.playways.grab.room.view.video;

import android.view.TextureView;
import android.view.View;
import android.view.ViewStub;

import com.common.core.myinfo.MyUserInfoManager;
import com.module.playways.grab.room.GrabRoomData;
import com.zq.mediaengine.kit.ZqEngineKit;

public class GrabVideoView {
    ViewStub mViewStub;
    //    RelativeLayout mParentView;
    TextureView mMainVideoView;
//    TextureView mSubVideoView;

    private GrabRoomData mRoomData;

    public GrabVideoView(ViewStub viewStub) {
        mViewStub = viewStub;
    }

    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
    }

    void infalte() {
        mMainVideoView = (TextureView) mViewStub.inflate();
//        mMainVideoView = mParentView.findViewById(R.id.main_video_view);
//        mSubVideoView = mParentView.findViewById(R.id.sub_video_view);
    }

    public void bindVideoStream(int userId) {
        if (mMainVideoView == null) {
            infalte();
        }
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
        if (mMainVideoView == null) {
            infalte();
        }
        mMainVideoView.setVisibility(View.VISIBLE);
//        mSubVideoView.setVisibility(View.GONE);
    }
}
