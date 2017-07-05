package com.wali.live.watchsdk.videodetail.presenter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.event.EventClass;
import com.wali.live.watchsdk.component.view.VideoDetailPlayerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.wali.live.component.ComponentController.MSG_NEW_DETAIL_REPLAY;
import static com.wali.live.component.ComponentController.MSG_PLAYER_DETAIL_SCREEN;
import static com.wali.live.component.ComponentController.MSG_PLAYER_FULL_SCREEN;
import static com.wali.live.component.ComponentController.MSG_PLAYER_PAUSE;
import static com.wali.live.component.ComponentController.MSG_PLAYER_START;

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

    public long getCurrentPosition() {
        return mView != null ? mView.onGetPlayingTime() : 0;
    }

    public VideoDetailPlayerPresenter(
            @NonNull IComponentController componentController,
            RoomBaseDataModel myRoomData,
            Activity activity) {
        super(componentController);
        mMyRoomData = myRoomData;
        mActivity = activity;
        registerAction();
        EventBus.getDefault().register(this);
    }

    public void registerAction() {
        registerAction(MSG_PLAYER_START);
        registerAction(MSG_PLAYER_PAUSE);
        registerAction(MSG_NEW_DETAIL_REPLAY);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mView != null) {
            mView.onDestroy();
        }
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
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
                mComponentController.onEvent(MSG_PLAYER_FULL_SCREEN);
                break;
            case EventClass.FeedsVideoEvent.TYPE_FULLSCREEN_TO_SMALL:
                mComponentController.onEvent(MSG_PLAYER_DETAIL_SCREEN);
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

    public class Action implements ComponentPresenter.IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null || mActivity == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
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
}
