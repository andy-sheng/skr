package com.module.playways.grab.room.view.video;

import android.content.pm.ActivityInfo;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.common.core.myinfo.MyUserInfoManager;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.view.ExViewStub;
import com.zq.mediaengine.capture.CameraCapture;
import com.zq.mediaengine.kit.ZqEngineKit;

public class GrabVideoDisplayView extends ExViewStub {
    //    RelativeLayout mParentView;
    TextureView mMainVideoView;
//    TextureView mSubVideoView;

    private GrabRoomData mRoomData;

    public GrabVideoDisplayView(ViewStub viewStub,GrabRoomData roomData) {
        super(viewStub);
        mRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mMainVideoView = (TextureView) mParentView;
        config();
    }

    protected void config() {
        // 设置推流分辨率
        ZqEngineKit.getInstance().setPreviewResolution(ZqEngineKit.VIDEO_RESOLUTION_720P);
        ZqEngineKit.getInstance().setTargetResolution(ZqEngineKit.VIDEO_RESOLUTION_360P);

        // 设置推流帧率
        ZqEngineKit.getInstance().setPreviewFps(30);
        ZqEngineKit.getInstance().setTargetFps(30);

        // 设置视频方向（横屏、竖屏）
        //mIsLandscape = false;
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ZqEngineKit.getInstance().setRotateDegrees(0);

        // 选择前后摄像头
        ZqEngineKit.getInstance().setCameraFacing(CameraCapture.FACING_FRONT);

        // 设置预览View
        ZqEngineKit.getInstance().setDisplayPreview(mMainVideoView);
    }

    public void bindVideoStream(int userId) {
        tryInflate();

        mMainVideoView.setVisibility(View.VISIBLE);
//        mSubVideoView.setVisibility(View.GONE);
        // 怎么解绑
        if (userId == MyUserInfoManager.getInstance().getUid()) {
            // 是自己
            ZqEngineKit.getInstance().setLocalVideoRect(0, 0, 1, 1, 1);
            ZqEngineKit.getInstance().startCameraPreview();
        } else {
            // 别人唱，两种情况。一是我绑定时别人的首帧视频流已经过来了，这是set没问题。
            // 但如果set时别人的视频流还没过来，
            ZqEngineKit.getInstance().setRemoteVideoRect(userId, 0, 1, 1, 1, 1);
        }
    }

    public void bindVideoStream(int userID1, int userID2) {
        tryInflate();
        mMainVideoView.setVisibility(View.VISIBLE);
//        mSubVideoView.setVisibility(View.GONE);
    }

    public void reset() {
        //TODO 停止摄像头预览 还要解绑VideoStream  等程乐提供
        ZqEngineKit.getInstance().stopCameraPreview();
    }
}
