package com.wali.live.watchsdk.watch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import com.base.event.SdkEventClass;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.wali.live.component.BaseSdkView;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.event.UserActionEvent;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.personinfo.fragment.FloatPersonInfoFragment;
import com.wali.live.watchsdk.ranking.RankingPagerFragment;
import com.wali.live.watchsdk.videodetail.ReplaySdkView;
import com.wali.live.watchsdk.videodetail.VideoDetailController;
import com.wali.live.watchsdk.videodetail.VideoDetailView;
import com.wali.live.watchsdk.watch.event.WatchOrReplayActivityCreated;
import com.wali.live.watchsdk.watch.model.RoomInfo;
import com.wali.live.watchsdk.watch.presenter.VideoShowPresenter;
import com.wali.live.watchsdk.watch.view.IWatchVideoView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

import static com.wali.live.component.ComponentController.MSG_ON_BACK_PRESSED;
import static com.wali.live.component.ComponentController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.ComponentController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.ComponentController.MSG_PLAYER_DETAIL_SCREEN;
import static com.wali.live.component.ComponentController.MSG_PLAYER_FULL_SCREEN;
import static com.wali.live.component.ComponentController.MSG_PLAYER_ROTATE_ORIENTATION;
import static com.wali.live.component.ComponentController.MSG_PLAYER_START;
import static com.wali.live.component.ComponentController.MSG_SHOW_PERSONAL_INFO;
import static com.wali.live.component.ComponentController.MSG_UPDATE_START_TIME;

/**
 * Created by yangli on 2017/5/26.
 */
public class VideoDetailSdkActivity extends BaseComponentSdkActivity implements IWatchVideoView {

    private final Action mAction = new Action();

    private long mVideoStartTime;

    private VideoDetailController mComponentController;
    private WeakReference<VideoDetailView> mDetailViewRef = new WeakReference<>(null);
    private WeakReference<ReplaySdkView> mFullScreenViewRef = new WeakReference<>(null);
    private BaseSdkView mSdkView;

    private VideoShowPresenter mVideoShowPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (isMIUIV6()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_detail_layout);
        initData();
        initView();
    }

    @Override
    public boolean isKeyboardResize() {
        return false;
    }

    @Override
    protected void trySendDataWithServerOnce() {

    }

    @Override
    protected void tryClearData() {
    }

    @Override
    public void onKickEvent(String msg) {
    }

    private void initData() {
        Intent data = getIntent();
        if (data == null) {
            return;
        }
        mRoomInfo = (RoomInfo) data.getParcelableExtra(EXTRA_ROOM_INFO);
        if (mRoomInfo == null) {
            MyLog.e(TAG, "mRoomInfo is null");
            finish();
            return;
        }
        // 填充 mMyRoomData
        mMyRoomData.setRoomId(mRoomInfo.getLiveId());
        mMyRoomData.setUid(mRoomInfo.getPlayerId());
        mMyRoomData.setLiveType(mRoomInfo.getLiveType());
        if (!TextUtils.isEmpty(mRoomInfo.getVideoUrl())) {
            mMyRoomData.setVideoUrl(mRoomInfo.getVideoUrl());
        } else {
            getVideoUrlFromServer();
        }
    }

    private void initView() {
        mComponentController = new VideoDetailController(mMyRoomData, mRoomChatMsgManager);
        mComponentController.setupController(this);
        switchToDetailMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mComponentController.onEvent(VideoDetailController.MSG_PLAYER_PAUSE);
    }

    private void getVideoUrlFromServer() {
        if (mVideoShowPresenter == null) {
            mVideoShowPresenter = new VideoShowPresenter(this);
            addPresent(mVideoShowPresenter);
        }
        mVideoShowPresenter.getVideoUrlOnly(mMyRoomData.getUid(), mMyRoomData.getRoomId());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        MyLog.w(TAG, "onDestroy");
        super.onDestroy();
        if (mComponentController != null) {
            mComponentController.release();
            mComponentController = null;
        }
        if (mSdkView != null) {
            mSdkView.stopSdkView();
            mSdkView = null;
        }
        if (mDetailViewRef.get() != null) {
            mDetailViewRef.get().releaseSdkView();
        }
        if (mFullScreenViewRef.get() != null) {
            mFullScreenViewRef.get().releaseSdkView();
        }
    }

    @Subscribe
    public void onEvent(WatchOrReplayActivityCreated event) {
    }

    @Override
    public boolean isStatusBarDark() {
        return false;
    }

    @Subscribe
    public void onEvent(SdkEventClass.OrientEvent event) {
        if (event.isLandscape()) {
            orientLandscape();
        } else {
            orientPortrait();
        }
    }

    protected void orientLandscape() {
        if (mComponentController != null) {
            mComponentController.onEvent(MSG_ON_ORIENT_LANDSCAPE);
        }
    }

    protected void orientPortrait() {
        if (mComponentController != null) {
            mComponentController.onEvent(MSG_ON_ORIENT_PORTRAIT);
        }
    }

    // 详情播放模式
    private void switchToDetailMode() {
        MyLog.w(TAG, "switchToDetailMode");
        VideoDetailView compoundView = mDetailViewRef.get();
        if (mSdkView != null && mSdkView == compoundView) {
            return;
        }
        if (mSdkView != null) {
            mSdkView.stopSdkView();
        }
        forcePortrait();
        if (compoundView == null) {
            compoundView = new VideoDetailView(this, mComponentController);
            compoundView.setupSdkView();
            mDetailViewRef = new WeakReference<>(compoundView);
        }
        compoundView.startSdkView();
        mSdkView = compoundView;
        $click($(R.id.back_iv), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mAction.unregisterAction();
        mAction.registerAction();
    }

    // 全屏播放模式
    private void switchToReplayMode() {
        MyLog.w(TAG, "switchToReplayMode");
        ReplaySdkView compoundView = mFullScreenViewRef.get();
        if (mSdkView != null && mSdkView == compoundView) {
            return;
        }
        if (mSdkView != null) {
            mSdkView.stopSdkView();
        }
        openOrientation();
        if (compoundView == null) {
            compoundView = new ReplaySdkView(this, mComponentController);
            compoundView.setupSdkView();
            mFullScreenViewRef = new WeakReference<>(compoundView);
        }
        compoundView.startSdkView(mVideoStartTime);
        mSdkView = compoundView;
        mAction.unregisterAction();
        mAction.registerAction();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            // 退出栈弹出
            String fName = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
            if (!TextUtils.isEmpty(fName)) {
                Fragment fragment = fm.findFragmentByTag(fName);
                MyLog.w(TAG, "fragment name=" + fName + ", fragment=" + fragment);
                if (fragmentBackPressed(fragment)) {
                    return;
                }
                FragmentNaviUtils.popFragmentFromStack(this);
                return;
            }
        } else if (mComponentController != null && mComponentController.onEvent(MSG_ON_BACK_PRESSED)) {
            return;
        } else if (mSdkView != null && mSdkView == mFullScreenViewRef.get()) {
            switchToDetailMode();
            return;
        }
        super.onBackPressed();
    }

    public static void openActivity(@NonNull Activity activity, RoomInfo roomInfo) {
        Intent intent = new Intent(activity, VideoDetailSdkActivity.class);
        intent.putExtra(EXTRA_ROOM_INFO, roomInfo);
        activity.startActivity(intent);
    }

    @Override
    public void updateVideoUrl(String videoUrl) {
        if (TextUtils.isEmpty(mMyRoomData.getVideoUrl())
                && !TextUtils.isEmpty(videoUrl)) {
            mMyRoomData.setVideoUrl(videoUrl);
            mComponentController.onEvent(MSG_PLAYER_START);
        }
    }

    @Override
    public void updateRoomInfo(String roomId, String videoUrl) {

    }

    @Override
    public void notifyLiveEnd() {

    }

    private void startShowFloatPersonInfo(long uid) {
        if (uid <= 0) {
            return;
        }
        FloatPersonInfoFragment.openFragment(VideoDetailSdkActivity.this,
                uid, mMyRoomData.getUid(), mMyRoomData.getRoomId(),
                mMyRoomData.getVideoUrl(), null, mMyRoomData.getEnterRoomTime());
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UserActionEvent event) {
        MyLog.e(TAG, "BaseEvent.UserActionEvent");
        // 该类型单独提出用指定的fastdoubleclick，防止fragment的崩溃
        if (event.type == UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO) {
            startShowFloatPersonInfo((Long) event.obj1);
            return;
        }
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
        switch (event.type) {
            case UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_TICKET: {
                long uid = (long) event.obj1;
                int ticket = (int) event.obj2;
                String liveId = (String) event.obj3;
                RankingPagerFragment.openFragment(this, ticket, mMyRoomData.getInitTicket(), uid, liveId,
                        mMyRoomData.isTicketing() ? RankingPagerFragment.PARAM_FROM_CURRENT : RankingPagerFragment.PARAM_FROM_TOTAL,
                        true, isDisplayLandscape());
            }
            break;
        }
    }


    private class Action implements ComponentPresenter.IAction {

        private void registerAction() {
            mComponentController.registerAction(MSG_PLAYER_FULL_SCREEN, this);
            mComponentController.registerAction(MSG_SHOW_PERSONAL_INFO, this);
            mComponentController.registerAction(MSG_UPDATE_START_TIME, this);
            mComponentController.registerAction(MSG_PLAYER_DETAIL_SCREEN, this);
            mComponentController.registerAction(MSG_PLAYER_ROTATE_ORIENTATION, this);
        }

        private void unregisterAction() {
            mComponentController.unregisterAction(this);
        }

        @Override
        public boolean onAction(int source, @Nullable ComponentPresenter.Params params) {
            switch (source) {
                case MSG_SHOW_PERSONAL_INFO:
                    startShowFloatPersonInfo((long) params.getItem(0));
                    return true;
                case MSG_PLAYER_FULL_SCREEN:
                    if (mVideoStartTime > 0) {
                        switchToReplayMode();
                    }
                    break;
                case MSG_PLAYER_DETAIL_SCREEN:
                    if (mVideoStartTime > 0) {
                        switchToDetailMode();
                    }
                    break;
                case MSG_UPDATE_START_TIME:
                    mVideoStartTime = params.getItem(0);
                    break;
                case MSG_PLAYER_ROTATE_ORIENTATION:
                    if (mLandscape) {
                        tempForcePortrait();
                    } else {
                        tempForceLandscape();
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
