package com.wali.live.watchsdk.watch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.WindowManager;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.personinfo.fragment.FloatPersonInfoFragment;
import com.wali.live.watchsdk.videodetail.VideoDetailController;
import com.wali.live.watchsdk.videodetail.VideoDetailView;
import com.wali.live.watchsdk.watch.event.WatchOrReplayActivityCreated;
import com.wali.live.watchsdk.watch.model.RoomInfo;
import com.wali.live.watchsdk.watch.presenter.VideoShowPresenter;
import com.wali.live.watchsdk.watch.view.IWatchVideoView;

import org.greenrobot.eventbus.Subscribe;

import static com.wali.live.component.ComponentController.MSG_SHOW_PERSONAL_INFO;

/**
 * Created by yangli on 2017/5/26.
 */
public class VideoDetailSdkActivity extends BaseComponentSdkActivity implements IWatchVideoView {

    private VideoDetailController mComponentController;
    private VideoDetailView mSdkView;
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
        mComponentController = new VideoDetailController(mMyRoomData);
        mSdkView = new VideoDetailView(this, mComponentController);
        mSdkView.setupSdkView();

        Action action = new Action();
        mComponentController.registerAction(MSG_SHOW_PERSONAL_INFO, action);
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
        if (mSdkView != null) {
            mSdkView.releaseSdkView();
        }
    }

    @Subscribe
    public void onEvent(WatchOrReplayActivityCreated event) {
    }

    @Override
    public boolean isStatusBarDark() {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyLog.w(TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " foreground=" + mIsForeground);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case ReplaySdkActivity.REQUEST_REPLAY:
                if (data != null) {
                    long timeStamp = data.getLongExtra(ReplaySdkActivity.EXT_REPLAYED_TIME, 0);
                    mComponentController.onEvent(VideoDetailController.MSG_PLAYER_SEEK_FROM_REPLAY,
                            new ComponentPresenter.Params().putItem(timeStamp));
                }
                break;
            default:
                break;
        }
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
            mComponentController.onEvent(VideoDetailController.MSG_PLAYER_START);
        }
    }

    @Override
    public void updateRoomInfo(String roomId, String videoUrl) {

    }

    @Override
    public void notifyLiveEnd() {

    }

    private class Action implements ComponentPresenter.IAction {
        @Override
        public boolean onAction(int source, @Nullable ComponentPresenter.Params params) {
            switch (source) {
                case MSG_SHOW_PERSONAL_INFO:
                    FloatPersonInfoFragment.openFragment(VideoDetailSdkActivity.this, (long) params.getItem(0),
                            mMyRoomData.getUid(), mMyRoomData.getRoomId(), mMyRoomData.getVideoUrl(),
                            null, mMyRoomData.getEnterRoomTime());
                    return true;
                default:
                    break;
            }
            return false;
        }
    }
}
