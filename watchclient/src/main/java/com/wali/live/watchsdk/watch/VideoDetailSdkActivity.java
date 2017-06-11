package com.wali.live.watchsdk.watch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.event.EventClass;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.personinfo.fragment.FloatPersonInfoFragment;
import com.wali.live.watchsdk.videodetail.VideoDetailController;
import com.wali.live.watchsdk.videodetail.VideoDetailView;
import com.wali.live.watchsdk.watch.event.WatchOrReplayActivityCreated;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.wali.live.component.ComponentController.MSG_PLAYER_FULL_SCREEN;
import static com.wali.live.component.ComponentController.MSG_SHOW_PERSONAL_INFO;

/**
 * Created by yangli on 2017/5/26.
 */
public class VideoDetailSdkActivity extends BaseComponentSdkActivity {

    private VideoDetailController mComponentController;
    private VideoDetailView mSdkView;
    private boolean mVideoPlayerEnable = true; //詳情頁播放器work。

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
        mMyRoomData.setVideoUrl(mRoomInfo.getVideoUrl());
        mMyRoomData.setLiveType(mRoomInfo.getLiveType());
    }

    private void initView() {
        mComponentController = new VideoDetailController(mMyRoomData);
        mSdkView = new VideoDetailView(this, mComponentController);
        mSdkView.setupSdkView();

        Action action = new Action();
        mComponentController.registerAction(MSG_SHOW_PERSONAL_INFO, action);
        mComponentController.registerAction(MSG_PLAYER_FULL_SCREEN, action);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mComponentController.onEvent(VideoDetailController.MSG_PLAYER_PAUSE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        MyLog.w(TAG, "onDestroy");
        super.onDestroy();
        mComponentController.onEvent(VideoDetailController.MSG_PLAYER_STOP);
    }

    @Subscribe
    public void onEvent(WatchOrReplayActivityCreated event) {
    }

    //视频event 刷新播放按钮等操作
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.FeedsVideoEvent event) {
        MyLog.w(TAG, "onEventMainThread  mVideoPlayerEnable=" + mVideoPlayerEnable
                + " event.type=" + event.mType);
        if (!mVideoPlayerEnable) {
            return;
        }
        switch (event.mType) {
            case EventClass.FeedsVideoEvent.TYPE_START:
                mComponentController.onEvent(VideoDetailController.MSG_PLAYER_HIDE_BTN);
                break;
            case EventClass.FeedsVideoEvent.TYPE_STOP:
                mComponentController.onEvent(VideoDetailController.MSG_PLAYER_SHOW_BTN);
                break;
            case EventClass.FeedsVideoEvent.TYPE_COMPLETION:
                break;
            case EventClass.FeedsVideoEvent.TYPE_ON_CLOSE_ENDLIVE:
                break;
            case EventClass.FeedsVideoEvent.TYPE_FULLSCREEN:
                mComponentController.onEvent(VideoDetailController.MSG_PLAYER_FULL_SCREEN);
                break;
            case EventClass.FeedsVideoEvent.TYPE_PLAYING:
                mComponentController.onEvent(VideoDetailController.MSG_PLAYER_PLAYING);
                break;
            case EventClass.FeedsVideoEvent.TYPE_SET_SEEK:
                break;
            case EventClass.FeedsVideoEvent.TYPE_ON_FEEDS_PLAY_ACT_DESTORY:
                break;
            case EventClass.FeedsVideoEvent.TYPE_ERROR:
                break;
            default:
                break;
        }
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
                    mVideoPlayerEnable = true;
                    long timeStamp = data.getLongExtra(ReplaySdkActivity.EXT_REPLAYED_TIME, 0);
                    mComponentController.onEvent(VideoDetailController.MSG_PLAYER_SEEK,
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

    private class Action implements ComponentPresenter.IAction {
        @Override
        public boolean onAction(int source, @Nullable ComponentPresenter.Params params) {
            switch (source) {
                case MSG_SHOW_PERSONAL_INFO:
                    FloatPersonInfoFragment.openFragment(VideoDetailSdkActivity.this, (long) params.getItem(0),
                            mMyRoomData.getUid(), mMyRoomData.getRoomId(), mMyRoomData.getVideoUrl(),
                            null, mMyRoomData.getEnterRoomTime());
                    return true;
                case MSG_PLAYER_FULL_SCREEN:
                    mVideoPlayerEnable = false;
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
