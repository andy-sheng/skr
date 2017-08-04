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
import com.thornbirds.component.IEventObserver;
import com.thornbirds.component.IParams;
import com.wali.live.componentwrapper.BaseSdkView;
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

import static com.wali.live.componentwrapper.BaseSdkController.MSG_ON_BACK_PRESSED;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_PLAYER_DETAIL_SCREEN;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_PLAYER_FULL_SCREEN;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_PLAYER_PAUSE;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_PLAYER_ROTATE_ORIENTATION;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_PLAYER_START;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_SHOW_PERSONAL_INFO;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_UPDATE_START_TIME;

/**
 * Created by yangli on 2017/5/26.
 */
public class VideoDetailSdkActivity extends BaseComponentSdkActivity implements IWatchVideoView {

    private final Action mAction = new Action();
    private VideoDetailController mController;
    private WeakReference<VideoDetailView> mDetailViewRef = new WeakReference<>(null);
    private WeakReference<ReplaySdkView> mFullScreenViewRef = new WeakReference<>(null);
    private BaseSdkView mSdkView;

    private long mVideoStartTime = -1;

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
        mController = new VideoDetailController(mMyRoomData, mRoomChatMsgManager);
        mController.setupController(this);
        switchToDetailMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mController.postEvent(MSG_PLAYER_PAUSE);
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
        if (mController != null) {
            mController.release();
            mController = null;
        }
        if (mSdkView != null) {
            mSdkView.stopView();
            mSdkView.release();
            mSdkView = null;
        }
        if (mDetailViewRef.get() != null) {
            mDetailViewRef.get().release();
        }
        if (mFullScreenViewRef.get() != null) {
            mFullScreenViewRef.get().release();
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
        if (mController != null) {
            mController.postEvent(MSG_ON_ORIENT_LANDSCAPE);
        }
    }

    protected void orientPortrait() {
        if (mController != null) {
            mController.postEvent(MSG_ON_ORIENT_PORTRAIT);
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
            mSdkView.stopView();
        }
        forcePortrait();
        if (compoundView == null) {
            compoundView = new VideoDetailView(this, mController);
            compoundView.setupView();
            mDetailViewRef = new WeakReference<>(compoundView);
        }
        compoundView.startView();
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
            mSdkView.stopView();
        }
        openOrientation();
        if (compoundView == null) {
            compoundView = new ReplaySdkView(this, mController);
            compoundView.setupView();
            mFullScreenViewRef = new WeakReference<>(compoundView);
        }
        compoundView.startView(mVideoStartTime);
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
        } else if (mController != null && mController.postEvent(MSG_ON_BACK_PRESSED)) {
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
            mController.postEvent(MSG_PLAYER_START);
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


    private class Action implements IEventObserver {

        private void registerAction() {
            mController.registerObserverForEvent(MSG_PLAYER_FULL_SCREEN, this);
            mController.registerObserverForEvent(MSG_SHOW_PERSONAL_INFO, this);
            mController.registerObserverForEvent(MSG_UPDATE_START_TIME, this);
            mController.registerObserverForEvent(MSG_PLAYER_DETAIL_SCREEN, this);
            mController.registerObserverForEvent(MSG_PLAYER_ROTATE_ORIENTATION, this);
        }

        private void unregisterAction() {
            if (mController != null) {
                mController.unregisterObserver(this);
            }
        }

        @Override
        public boolean onEvent(int event, IParams params) {
            switch (event) {
                case MSG_SHOW_PERSONAL_INFO:
                    startShowFloatPersonInfo((long) params.getItem(0));
                    return true;
                case MSG_PLAYER_FULL_SCREEN:
                    if (mVideoStartTime >= 0) {
                        switchToReplayMode();
                    }
                    break;
                case MSG_PLAYER_DETAIL_SCREEN:
                    if (mVideoStartTime >= 0) {
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
