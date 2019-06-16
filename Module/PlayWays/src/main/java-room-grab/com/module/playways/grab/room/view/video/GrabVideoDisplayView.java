package com.module.playways.grab.room.view.video;

import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewStub;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.engine.EngineEvent;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.view.ExViewStub;
import com.zq.mediaengine.capture.CameraCapture;
import com.zq.mediaengine.kit.ZqEngineKit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class GrabVideoDisplayView extends ExViewStub {
    public final static String TAG = "GrabVideoDisplayView";

    //    RelativeLayout mParentView;
    TextureView mMainVideoView;
//    TextureView mSubVideoView;

    private GrabRoomData mRoomData;

    int mMainUserId = 0, mLeftUserId = 0, mRightUserId = 0;


    public GrabVideoDisplayView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
        mRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mMainVideoView = (TextureView) mParentView;
        config();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
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
        mMainUserId = userId;
        tryBindMainVideoStream();
    }

    public void bindVideoStream(int userID1, int userID2) {
        tryInflate();
        mMainVideoView.setVisibility(View.VISIBLE);
        mLeftUserId = userID1;
        mRightUserId = userID2;
        tryBindLeftVideoStream();
        tryBindRightVideoStream();
//        mSubVideoView.setVisibility(View.GONE);
    }

    public void reset() {
        //TODO 停止摄像头预览 还要解绑VideoStream  等程乐提供
        ZqEngineKit.getInstance().stopCameraPreview();
        mMainUserId = 0;
        mLeftUserId = 0;
        mRightUserId = 0;
    }

    void tryBindMainVideoStream() {
        MyLog.d(TAG,"tryBindMainVideoStream mMainUserId"+mMainUserId );
        if (mMainUserId == MyUserInfoManager.getInstance().getUid()) {
            // 是自己
            ZqEngineKit.getInstance().setLocalVideoRect(0, 0, 1, 1, 1);
            ZqEngineKit.getInstance().startCameraPreview();
        } else {
            // 别人唱，两种情况。一是我绑定时别人的首帧视频流已经过来了，这是set没问题。
            // 但如果set时别人的视频流还没过来，
            ZqEngineKit.getInstance().setRemoteVideoRect(mMainUserId, 0, 0, 1, 1, 1);
        }
    }

    void tryBindLeftVideoStream() {
        MyLog.d(TAG,"tryBindLeftVideoStream mLeftUserId="+mLeftUserId );
        if (mLeftUserId == MyUserInfoManager.getInstance().getUid()) {
            // 是自己
            ZqEngineKit.getInstance().setLocalVideoRect(0, 0, 0.5f, 1, 1);
            ZqEngineKit.getInstance().startCameraPreview();
        } else {
            // 别人唱，两种情况。一是我绑定时别人的首帧视频流已经过来了，这是set没问题。
            // 但如果set时别人的视频流还没过来，
            ZqEngineKit.getInstance().setRemoteVideoRect(mLeftUserId, 0, 0,0.5f, 1, 1);
        }
    }

    void tryBindRightVideoStream() {
        MyLog.d(TAG,"tryBindRightVideoStream mRightUserId="+mRightUserId );
        if (mRightUserId == MyUserInfoManager.getInstance().getUid()) {
            // 是自己
            ZqEngineKit.getInstance().setLocalVideoRect(0.5f, 0, 0.5f, 1, 1);
            ZqEngineKit.getInstance().startCameraPreview();
        } else {
            // 别人唱，两种情况。一是我绑定时别人的首帧视频流已经过来了，这是set没问题。
            // 但如果set时别人的视频流还没过来，
            ZqEngineKit.getInstance().setRemoteVideoRect(mRightUserId, 0.5f, 0, 0.5f, 1, 1);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(EngineEvent event) {
        MyLog.e(TAG, "onEvent " + event);
        if (event.getType() == EngineEvent.TYPE_FIRST_REMOTE_VIDEO_DECODED) {
            int userId = event.getUserStatus().getUserId();
            if (userId == mMainUserId) {
                tryBindMainVideoStream();
            } else if (userId == mLeftUserId) {
                tryBindLeftVideoStream();
            } else if (userId == mRightUserId) {
                tryBindRightVideoStream();
            }
        } else if (event.getType() == EngineEvent.TYPE_USER_LEAVE) {
//            int userId = event.getUserStatus().getUserId();
//            ZqEngineKit.getInstance().setLocalVideoRect(0, 0, 1.0f, 1.0f, 1.0f);
        }
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        super.onViewAttachedToWindow(v);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
        EventBus.getDefault().unregister(this);
    }
}
