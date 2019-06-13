package com.module.playways.grab.room.view.video;

import android.view.TextureView;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;

import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;

public class GrabVideoView {
    ViewStub mViewStub;
    RelativeLayout mParentView;
    TextureView mMainVideoView;
    TextureView mSubVideoView;

    private GrabRoomData mRoomData;

    public GrabVideoView(ViewStub viewStub) {
        mViewStub = viewStub;
    }

    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
    }

    void infalte() {
        mParentView = (RelativeLayout) mViewStub.inflate();
        mMainVideoView = mParentView.findViewById(R.id.main_video_view);
        mSubVideoView = mParentView.findViewById(R.id.sub_video_view);
    }

    public void bindVideoStream(int userId) {
        if (mParentView == null) {
            infalte();
        }
        mMainVideoView.setVisibility(View.VISIBLE);
        mSubVideoView.setVisibility(View.GONE);
    }

    public void bindVideoStream(int userID1, int userID2) {
        if (mParentView == null) {
            infalte();
        }
        mMainVideoView.setVisibility(View.VISIBLE);
        mSubVideoView.setVisibility(View.GONE);
    }
}
