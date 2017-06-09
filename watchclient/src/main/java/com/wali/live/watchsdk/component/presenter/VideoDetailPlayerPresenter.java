package com.wali.live.watchsdk.component.presenter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.view.VideoDetailPlayerView;
import com.wali.live.watchsdk.videodetail.VideoDetailController;
import com.wali.live.watchsdk.watch.ReplaySdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

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

    public VideoDetailPlayerPresenter(@NonNull IComponentController componentController,
                                      RoomBaseDataModel myRoomData,
                                      Activity activity) {
        super(componentController);
        mMyRoomData = myRoomData;
        mActivity = activity;
        registerAction(VideoDetailController.MSG_PLAYER_RESUME);
        registerAction(VideoDetailController.MSG_PLAYER_PAUSE);
        registerAction(VideoDetailController.MSG_PLAYER_STOP);
        registerAction(VideoDetailController.MSG_PLAYER_FULL_SCREEN);
        registerAction(VideoDetailController.MSG_PLAYER_SEEK);
        registerAction(VideoDetailController.MSG_PLAYER_PLAYING);
        registerAction(VideoDetailController.MSG_NEW_DETAIL_REPLAY);
        registerAction(VideoDetailController.MSG_PLAYER_SHOW_BTN);
        registerAction(VideoDetailController.MSG_PLAYER_HIDE_BTN);
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

    public class Action implements ComponentPresenter.IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null || mActivity == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                case VideoDetailController.MSG_NEW_DETAIL_REPLAY:
                    mView.onResetPlayer();
                    mView.onStartPlayer();
                    break;
                case VideoDetailController.MSG_PLAYER_PLAYING:
                    mView.onPlaying();
                    break;
                case VideoDetailController.MSG_PLAYER_SEEK:
                    mView.onSeekPlayer((long) params.getItem(0));
                    break;
                case VideoDetailController.MSG_PLAYER_RESUME:
                    mView.onResumePlayer();
                    break;
                case VideoDetailController.MSG_PLAYER_PAUSE:
                    mView.onPausePlayer();
                    break;
                case VideoDetailController.MSG_PLAYER_STOP:
                    mView.onStopPlayer();
                    break;
                case VideoDetailController.MSG_PLAYER_FULL_SCREEN:
                    RoomInfo roomInfo = RoomInfo.Builder.newInstance(mMyRoomData.getUid(), mMyRoomData.getRoomId(), mMyRoomData.getVideoUrl())
                            .setLiveType(mMyRoomData.getLiveType())
                            .setGameId(mMyRoomData.getGameId())
                            .setEnableShare(mMyRoomData.getEnableShare())
                            .setStartTime(mView.onGetPlayingTime())
                            .build();
                    ReplaySdkActivity.openActivity(mActivity, roomInfo);
                    mView.onClickFullScreen();
                    break;
                case VideoDetailController.MSG_PLAYER_SHOW_BTN:
                    mView.showPlayBtn(true);
                    break;
                case VideoDetailController.MSG_PLAYER_HIDE_BTN:
                    mView.showPlayBtn(false);
                default:
                    break;
            }
            return false;
        }
    }
}
