package com.wali.live.watchsdk.videodetail.presenter;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.event.EventClass;
import com.wali.live.watchsdk.videodetail.view.VideoDetailPlayerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.wali.live.component.BaseSdkController.MSG_NEW_DETAIL_REPLAY;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_DETAIL_SCREEN;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_FULL_SCREEN;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_PAUSE;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_START;

/**
 * Created by zyh on 2017/05/31.
 *
 * @module 回放详情页的播放view的presenter
 */
public class VideoDetailPlayerPresenter extends ComponentPresenter<VideoDetailPlayerView.IView>
        implements VideoDetailPlayerView.IPresenter {
    private static final String TAG = "VideoDetailPlayerPresenter";

    private RoomBaseDataModel mMyRoomData;
    private Activity mActivity;
    private boolean mVideoPlayerEnable = true; //詳情頁播放器work。

    @Override
    protected String getTAG() {
        return TAG;
    }

    public long getCurrentPosition() {
        return mView != null ? mView.onGetPlayingTime() : 0;
    }

    public VideoDetailPlayerPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel myRoomData,
            @NonNull Activity activity) {
        super(controller);
        mMyRoomData = myRoomData;
        mActivity = activity;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_PLAYER_START);
        registerAction(MSG_PLAYER_PAUSE);
        registerAction(MSG_NEW_DETAIL_REPLAY);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mView != null) {
            mView.onDestroy();
            mView = null;
        }
    }

    @Override
    public void onBackPress() {
        if (mActivity != null) {
            mActivity.finish();
        }
    }

    //视频event 刷新播放按钮等操作
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.FeedsVideoEvent event) {
        MyLog.w(TAG, "onEventMainThread  mVideoPlayerEnable=" + mVideoPlayerEnable
                + " event.type=" + event.mType);
        if (!mVideoPlayerEnable || mView == null) {
            return;
        }
        switch (event.mType) {
            case EventClass.FeedsVideoEvent.TYPE_START:
                mView.showPlayBtn(false);
                break;
            case EventClass.FeedsVideoEvent.TYPE_STOP:
                mView.showPlayBtn(true);
                break;
            case EventClass.FeedsVideoEvent.TYPE_COMPLETION:
                mView.onCompleteState();
                break;
            case EventClass.FeedsVideoEvent.TYPE_ON_CLOSE_ENDLIVE:
                break;
            case EventClass.FeedsVideoEvent.TYPE_SMALL_TO_FULLSCREEN:
                postEvent(MSG_PLAYER_FULL_SCREEN);
                break;
            case EventClass.FeedsVideoEvent.TYPE_FULLSCREEN_TO_SMALL:
                postEvent(MSG_PLAYER_DETAIL_SCREEN);
                break;
            case EventClass.FeedsVideoEvent.TYPE_PLAYING:
                mView.onPlaying();
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
    public boolean onEvent(int event, IParams params) {
        if (mView == null || mActivity == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_PLAYER_START:
                mView.onStartPlayer();
                break;
            case MSG_PLAYER_PAUSE:
                mView.onPausePlayer();
                break;
            case MSG_NEW_DETAIL_REPLAY:
                mView.onResetPlayer();
                mView.onStartPlayer();
                break;
            default:
                break;
        }
        return false;
    }
}
