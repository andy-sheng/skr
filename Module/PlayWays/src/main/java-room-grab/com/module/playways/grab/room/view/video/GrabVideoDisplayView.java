package com.module.playways.grab.room.view.video;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.support.constraint.ConstraintLayout;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebugLogView;
import com.common.view.ex.ExTextView;
import com.engine.EngineEvent;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.common.view.ExViewStub;
import com.module.playways.grab.room.ui.GrabWidgetAnimationController;
import com.module.playways.grab.room.view.SingCountDownView2;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.zq.mediaengine.capture.CameraCapture;
import com.zq.mediaengine.kit.ZqEngineKit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class GrabVideoDisplayView extends ExViewStub {
    public final static String TAG = "GrabVideoDisplayView";

    TextureView mMainVideoView;
    BaseImageView mLeftAvatarIv;
    BaseImageView mRightAvatarIv;
    TextView mLeftTipsTv;
    TextView mRightTipsTv;
    SingCountDownView2 mSingCountDownView;
    ImageView mBeautySettingBtn;
    ExTextView mLeftNameTv;
    ExTextView mRightNameTv;
    SelfSingCardView.Listener mListener;
    private GrabRoomData mRoomData;

    int mMainUserId = 0, mLeftUserId = 0, mRightUserId = 0;

    public GrabVideoDisplayView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
        mRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mMainVideoView = mParentView.findViewById(R.id.main_video_view);
        {
            mLeftAvatarIv = mParentView.findViewById(R.id.left_avatar_iv);
            ViewGroup.LayoutParams lp = mLeftAvatarIv.getLayoutParams();
            lp.width = U.getDisplayUtils().getScreenWidth() / 2;
            mLeftTipsTv = mParentView.findViewById(R.id.left_tips_tv);
            mLeftNameTv = mParentView.findViewById(R.id.left_name_tv);
        }
        {
            mRightAvatarIv = mParentView.findViewById(R.id.right_avatar_iv);
            ViewGroup.LayoutParams lp = mRightAvatarIv.getLayoutParams();
            lp.width = U.getDisplayUtils().getScreenWidth() / 2;
            mRightTipsTv = mParentView.findViewById(R.id.right_tips_tv);
            mRightNameTv = mParentView.findViewById(R.id.right_name_tv);
        }
        mSingCountDownView = mParentView.findViewById(R.id.sing_count_down_view);
        mSingCountDownView.setListener(mListener);
        mBeautySettingBtn = mParentView.findViewById(R.id.beauty_setting_btn);
        mBeautySettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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
        if (userId == MyUserInfoManager.getInstance().getUid()) {
            mBeautySettingBtn.setVisibility(View.VISIBLE);
        } else {
            mBeautySettingBtn.setVisibility(View.GONE);
        }
    }

    public void bindVideoStream(UserInfoModel userID1, UserInfoModel userID2,boolean needBindVideo) {
        MyLog.d(TAG,"bindVideoStream needBindVideo="+ needBindVideo);
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
        if(needBindVideo){
            mLeftAvatarIv.setVisibility(View.VISIBLE);
            AvatarUtils.loadAvatarByUrl(mLeftAvatarIv, AvatarUtils.newParamsBuilder(userID1.getAvatar())
                    .setBlur(true)
                    .build()
            );
            mRightAvatarIv.setVisibility(View.VISIBLE);
            AvatarUtils.loadAvatarByUrl(mRightAvatarIv, AvatarUtils.newParamsBuilder(userID2.getAvatar())
                    .setBlur(true)
                    .build()
            );
            tryBindLeftVideoStream();
            tryBindRightVideoStream();
        }
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (mSingCountDownView != null) {
            if (infoModel != null) {
                mSingCountDownView.startPlay(0, infoModel.getSingTotalMs(), true);
            }
        }
        if (mRightUserId == MyUserInfoManager.getInstance().getUid() || mLeftUserId == MyUserInfoManager.getInstance().getUid()) {
            mBeautySettingBtn.setVisibility(View.VISIBLE);
        } else {
            mBeautySettingBtn.setVisibility(View.GONE);
        }
    }

    public void reset() {
        ZqEngineKit.getInstance().stopCameraPreview();
        ZqEngineKit.getInstance().unbindAllRemoteVideo();
        mMainUserId = 0;
        mLeftUserId = 0;
        mRightUserId = 0;
        if (mParentView != null) {
            mLeftAvatarIv.setVisibility(View.GONE);
            mLeftTipsTv.setVisibility(View.GONE);
            mLeftNameTv.setVisibility(View.GONE);
            mRightAvatarIv.setVisibility(View.GONE);
            mRightTipsTv.setVisibility(View.GONE);
            mRightNameTv.setVisibility(View.GONE);
            mSingCountDownView.reset();
            setMarginTop(0);
        }
    }

    void tryBindMainVideoStream() {
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
        if (mLeftUserId == MyUserInfoManager.getInstance().getUid()) {
            // 是自己
            ZqEngineKit.getInstance().setLocalVideoRect(0, 0, 0.5f, 1, 1);
            ZqEngineKit.getInstance().startCameraPreview();
            mLeftAvatarIv.setVisibility(View.GONE);
            mLeftTipsTv.setVisibility(View.GONE);
        } else {
            // 别人唱，两种情况。一是我绑定时别人的首帧视频流已经过来了，这是set没问题。
            // 但如果set时别人的视频流还没过来，
            if (ZqEngineKit.getInstance().isFirstVideoDecoded(mLeftUserId)) {
                ZqEngineKit.getInstance().bindRemoteVideoRect(mLeftUserId, 0, 0, 0.5f, 1, 1);
                mLeftAvatarIv.setVisibility(View.GONE);
                mLeftTipsTv.setVisibility(View.GONE);
            } else {
                DebugLogView.println(TAG,mLeftUserId+"首帧还没到!!!");
                mLeftAvatarIv.setVisibility(View.VISIBLE);
            }
        }
    }

    void tryBindRightVideoStream() {
        if (mRightUserId == MyUserInfoManager.getInstance().getUid()) {
            // 是自己
            ZqEngineKit.getInstance().setLocalVideoRect(0.5f, 0, 0.5f, 1, 1);
            ZqEngineKit.getInstance().startCameraPreview();
            mRightAvatarIv.setVisibility(View.GONE);
            mRightTipsTv.setVisibility(View.GONE);
        } else {
            // 别人唱，两种情况。一是我绑定时别人的首帧视频流已经过来了，这是set没问题。
            // 但如果set时别人的视频流还没过来，
            if (ZqEngineKit.getInstance().isFirstVideoDecoded(mRightUserId)) {
                ZqEngineKit.getInstance().bindRemoteVideoRect(mRightUserId, 0.5f, 0, 0.5f, 1, 1);
                mRightAvatarIv.setVisibility(View.GONE);
                mRightTipsTv.setVisibility(View.GONE);
            } else {
                DebugLogView.println(TAG,mRightUserId+"首帧还没到!!!");
                mRightAvatarIv.setVisibility(View.VISIBLE);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EngineEvent event) {
        if (event.type != EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION
                && event.type != EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER) {
            DebugLogView.println(TAG,  event.toString());
        }
        if (mParentView == null) {
            return;
        }
        if (event.getType() == EngineEvent.TYPE_FIRST_REMOTE_VIDEO_DECODED) {
            int userId = event.getUserStatus().getUserId();
            if (userId == mMainUserId) {
                tryBindMainVideoStream();
            } else if (userId == mLeftUserId) {
                tryBindLeftVideoStream();
            } else if (userId == mRightUserId) {
                tryBindRightVideoStream();
            }
        } else if(event.getType() == EngineEvent.TYPE_USER_ROLE_CHANGE){
            EngineEvent.RoleChangeInfo roleChangeInfo = event.getObj();
            if(roleChangeInfo.getNewRole()==2){
                ZqEngineKit.getInstance().stopCameraPreview();
                //变成观众了
                if (MyUserInfoManager.getInstance().getUid() == mLeftUserId) {
                    mLeftAvatarIv.setVisibility(View.VISIBLE);
                    mLeftTipsTv.setVisibility(View.VISIBLE);
                } else if (MyUserInfoManager.getInstance().getUid() == mRightUserId) {
                    mRightAvatarIv.setVisibility(View.VISIBLE);
                    mRightTipsTv.setVisibility(View.VISIBLE);
                }
            }
        }else if (event.getType() == EngineEvent.TYPE_USER_LEAVE) {
            int userId = event.getUserStatus().getUserId();
            ZqEngineKit.getInstance().unbindRemoteVideo(userId);
            if (userId == mLeftUserId) {
                mLeftAvatarIv.setVisibility(View.VISIBLE);
                mLeftTipsTv.setVisibility(View.VISIBLE);
            } else if (userId == mRightUserId) {
                mRightAvatarIv.setVisibility(View.VISIBLE);
                mRightTipsTv.setVisibility(View.VISIBLE);
            }
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

    private int getTranslateY(boolean open, boolean topContentViewVisiable) {
        int ty = 0;
        if (mParentView != null) {
            ViewGroup.LayoutParams lp = mParentView.getLayoutParams();
            if (open) {
                if (topContentViewVisiable && lp.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                    ty = U.getDisplayUtils().dip2px(58);
                } else {
                    ty = U.getDisplayUtils().dip2px(14);
                }
            } else {
                if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                    ty = U.getDisplayUtils().dip2px(88);
                } else {
                    ty = U.getDisplayUtils().dip2px(14);
                }
            }
        }
        return ty;
    }

    /**
     * 内部的两个小部件的动画
     *
     * @param open
     * @param topContentViewVisiable
     * @return
     */
    public List<Animator> getInnerAnimator(boolean open, boolean topContentViewVisiable) {
        if (mSingCountDownView == null) {
            return null;
        }
        int ty = getTranslateY(open, topContentViewVisiable);

        List<Animator> list = new ArrayList<>();

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(mSingCountDownView, View.TRANSLATION_Y, mSingCountDownView.getTranslationY(), ty);
        list.add(objectAnimator1);

        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(mBeautySettingBtn, View.TRANSLATION_Y, mSingCountDownView.getTranslationY(), ty);
        list.add(objectAnimator2);
        return list;
    }

    public void setListener(SelfSingCardView.Listener listener) {
        mListener = listener;
    }

    public void adjustViewPostion(boolean open, boolean topContentViewVisiable) {
        int translateY = getTranslateY(open, topContentViewVisiable);
        if (mSingCountDownView != null) {
            mSingCountDownView.setTranslationY(translateY);
        }
        if (mBeautySettingBtn != null) {
            mBeautySettingBtn.setTranslationY(translateY);
        }
    }

    public void setMarginTop(int px) {
        if (mParentView != null) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mParentView.getLayoutParams();
            lp.topMargin = px;
        }
    }

    /**
     * 需要额外往下移动的数值，如全屏到非全屏时，需要加个顶部栏
     *
     * @return
     */
    public int getExtraTranslateYWhenOpen(int openType) {
        if (openType == GrabWidgetAnimationController.OPEN_TYPE_FOR_LYRIC) {
            // 演唱者
            return U.getStatusBarUtil().getStatusBarHeight(U.app());
        } else {
            // 非演唱者
            if (mParentView != null) {
                ViewGroup.LayoutParams lp = mParentView.getLayoutParams();
                if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                    return U.getStatusBarUtil().getStatusBarHeight(U.app());
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }
    }

}
