package com.module.playways.grab.room.view.video;

import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.engine.EngineEvent;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.ExViewStub;
import com.module.playways.grab.room.view.SingCountDownView2;
import com.zq.mediaengine.capture.CameraCapture;
import com.zq.mediaengine.kit.ZqEngineKit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class GrabVideoDisplayView extends ExViewStub {
    public final static String TAG = "GrabVideoDisplayView";

    TextureView mMainVideoView;
    SingCountDownView2 mSingCountDownView;
    ImageView mBeautySettingBtn;
    ExTextView mLeftNameTv;
    ExTextView mRightNameTv;

    private GrabRoomData mRoomData;

    int mMainUserId = 0, mLeftUserId = 0, mRightUserId = 0;

    public GrabVideoDisplayView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
        mRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mMainVideoView = mParentView.findViewById(R.id.main_video_view);
        mSingCountDownView = mParentView.findViewById(R.id.sing_count_down_view);
        mBeautySettingBtn = mParentView.findViewById(R.id.beauty_setting_btn);
        mBeautySettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mLeftNameTv = mParentView.findViewById(R.id.left_name_tv);
        mRightNameTv = mParentView.findViewById(R.id.right_name_tv);
        config();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_room_video_display_view_stub_layout;
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
        setVisibility(View.VISIBLE);
        mLeftNameTv.setVisibility(View.GONE);
        mRightNameTv.setVisibility(View.GONE);

        ViewGroup.LayoutParams lp = mParentView.getLayoutParams();
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mMainUserId = userId;
        tryBindMainVideoStream();
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (mSingCountDownView != null) {
            if (infoModel != null) {
                mSingCountDownView.startPlay(0, infoModel.getSingTotalMs(), true);
            }
        }
    }

    public void bindVideoStream(UserInfoModel userID1, UserInfoModel userID2) {
        tryInflate();
        setVisibility(View.VISIBLE);
        mLeftNameTv.setVisibility(View.VISIBLE);
        mRightNameTv.setVisibility(View.VISIBLE);
        mLeftNameTv.setText(userID1.getNicknameRemark());
        mRightNameTv.setText(userID2.getNicknameRemark());
        ViewGroup.LayoutParams lp = mParentView.getLayoutParams();
        lp.height = U.getDisplayUtils().dip2px(315);
        mLeftUserId = userID1.getUserId();
        mRightUserId = userID2.getUserId();
        tryBindLeftVideoStream();
        tryBindRightVideoStream();
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (mSingCountDownView != null) {
            if (infoModel != null) {
                mSingCountDownView.startPlay(0, infoModel.getSingTotalMs(), true);
            }
        }
    }

    public void reset() {
        //TODO 停止摄像头预览 还要解绑VideoStream  等程乐提供
        ZqEngineKit.getInstance().stopCameraPreview();
        mMainUserId = 0;
        mLeftUserId = 0;
        mRightUserId = 0;
        if (mSingCountDownView != null) {
            mSingCountDownView.reset();
        }
    }

    void tryBindMainVideoStream() {
        MyLog.d(TAG, "tryBindMainVideoStream mMainUserId" + mMainUserId);
        if (mMainUserId == MyUserInfoManager.getInstance().getUid()) {
            // 是自己
            ZqEngineKit.getInstance().setLocalVideoRect(0, 0, 1, 1, 1);
            ZqEngineKit.getInstance().startCameraPreview();
        } else {
            // 别人唱，两种情况。一是我绑定时别人的首帧视频流已经过来了，这是set没问题。
            // 但如果set时别人的视频流还没过来，
            ZqEngineKit.getInstance().bindRemoteVideoRect(mMainUserId, 0, 0, 1, 1, 1);
        }
    }

    void tryBindLeftVideoStream() {
        MyLog.d(TAG, "tryBindLeftVideoStream mLeftUserId=" + mLeftUserId);
        if (mLeftUserId == MyUserInfoManager.getInstance().getUid()) {
            // 是自己
            ZqEngineKit.getInstance().setLocalVideoRect(0, 0, 0.5f, 1, 1);
            ZqEngineKit.getInstance().startCameraPreview();
        } else {
            // 别人唱，两种情况。一是我绑定时别人的首帧视频流已经过来了，这是set没问题。
            // 但如果set时别人的视频流还没过来，
            ZqEngineKit.getInstance().bindRemoteVideoRect(mLeftUserId, 0, 0, 0.5f, 1, 1);
        }
    }

    void tryBindRightVideoStream() {
        MyLog.d(TAG, "tryBindRightVideoStream mRightUserId=" + mRightUserId);
        if (mRightUserId == MyUserInfoManager.getInstance().getUid()) {
            // 是自己
            ZqEngineKit.getInstance().setLocalVideoRect(0.5f, 0, 0.5f, 1, 1);
            ZqEngineKit.getInstance().startCameraPreview();
        } else {
            // 别人唱，两种情况。一是我绑定时别人的首帧视频流已经过来了，这是set没问题。
            // 但如果set时别人的视频流还没过来，
            ZqEngineKit.getInstance().bindRemoteVideoRect(mRightUserId, 0.5f, 0, 0.5f, 1, 1);
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
            int userId = event.getUserStatus().getUserId();
            ZqEngineKit.getInstance().unbindRemoteVideo(userId);
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
