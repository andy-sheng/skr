package com.wali.live.watchsdk.videodetail.presenter;

import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.view.Surface;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.videodetail.view.DetailPlayerView;
import com.wali.live.watchsdk.videothird.data.PullStreamerPresenter;
import com.xiaomi.player.Player;

import static com.wali.live.component.BaseSdkController.MSG_BACKGROUND_CLICK;
import static com.wali.live.component.BaseSdkController.MSG_NEW_DETAIL_REPLAY;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_ON_STREAM_RECONNECT;
import static com.wali.live.component.BaseSdkController.MSG_ON_STREAM_SUCCESS;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_COMPLETED;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_ERROR;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_FULL_SCREEN;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_PREPARED;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_START;
import static com.wali.live.component.BaseSdkController.MSG_UPDATE_PLAY_PROGRESS;

/**
 * Created by yangli on 2017/09/25.
 *
 * @module 详情-播放控制表现
 */
public class DetailPlayerPresenter extends ComponentPresenter<DetailPlayerView.IView>
        implements DetailPlayerView.IPresenter {
    private static final String TAG = "DetailPlayerPresenter";

    private PullStreamerPresenter mStreamerPresenter;
    private RoomBaseDataModel mMyRoomData;

    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private Surface mSurface;

    private boolean mIsLandscape = false;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public DetailPlayerPresenter(
            @NonNull IEventController controller,
            @NonNull PullStreamerPresenter streamerPresenter,
            @NonNull RoomBaseDataModel myRoomData) {
        super(controller);
        mStreamerPresenter = streamerPresenter;
        mMyRoomData = myRoomData;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_BACKGROUND_CLICK);

        registerAction(MSG_PLAYER_START);
        registerAction(MSG_NEW_DETAIL_REPLAY);
        registerAction(MSG_PLAYER_PREPARED);
        registerAction(MSG_PLAYER_ERROR);
        registerAction(MSG_PLAYER_COMPLETED);
        registerAction(MSG_UPDATE_PLAY_PROGRESS);

        registerAction(MSG_ON_STREAM_RECONNECT);
        registerAction(MSG_ON_STREAM_SUCCESS);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
    }

    @Override
    public void startPlay() {
        if (mStreamerPresenter.isStarted()) {
            mStreamerPresenter.resumeWatch();
        } else {
            mStreamerPresenter.startWatch();
        }
    }

    @Override
    public void pausePlay() {
        mView.showLoading(false);
        mStreamerPresenter.pauseWatch();
    }

    @Override
    public void switchToFullScreen() {
        postEvent(MSG_PLAYER_FULL_SCREEN);
    }

    @Override
    public void seekTo(float progress) {
        mView.showLoading(false);
        mStreamerPresenter.seekTo((long) (progress * 1000));
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        MyLog.w(TAG, "onSurfaceTextureAvailable");
        if (mSurface == null) {
            mSurface = new Surface(surface);
        }
        onSurfaceTextureSizeChanged(surface, width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        MyLog.w(TAG, "onSurfaceTextureSizeChanged");
        if (mSurfaceWidth != width || mSurfaceHeight != height) {
            MyLog.w(TAG, "onSurfaceTextureSizeChanged width=" + width + ", height=" + width);
            mSurfaceWidth = width;
            mSurfaceHeight = height;
            mStreamerPresenter.setSurface(mSurface);
            mStreamerPresenter.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFit, width, height);
            mStreamerPresenter.shiftUp(0.2f);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        MyLog.w(TAG, "onSurfaceTextureDestroyed");
        if (mSurface != null) {
            mSurfaceWidth = mSurfaceHeight = 0;
            mSurface.release();
            mSurface = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        mIsLandscape = isLandscape;
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                onOrientation(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                onOrientation(true);
                return true;
            case MSG_UPDATE_PLAY_PROGRESS:
                mView.onUpdateProgress((int) (mStreamerPresenter.getCurrentPosition() / 1000));
                return true;
            case MSG_BACKGROUND_CLICK:
                mView.onChangeVisibility();
                return true;
            case MSG_ON_STREAM_RECONNECT:
                mView.showLoading(true);
                return true;
            case MSG_ON_STREAM_SUCCESS:
                mView.showLoading(false);
                return true;
            case MSG_PLAYER_PREPARED:
                mView.onUpdateDuration((int) (mStreamerPresenter.getDuration() / 1000));
                return true;
            case MSG_PLAYER_ERROR:
            case MSG_PLAYER_COMPLETED:
                mView.reset();
                return true;
            case MSG_PLAYER_START:
            case MSG_NEW_DETAIL_REPLAY:
                mStreamerPresenter.stopWatch();
                mStreamerPresenter.setOriginalStreamUrl(mMyRoomData.getVideoUrl());
                startPlay();
                return true;
            default:
                break;
        }
        return false;
    }
}
