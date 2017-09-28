package com.wali.live.watchsdk.videodetail.presenter;

import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.view.Surface;

import com.base.log.MyLog;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.videodetail.view.DetailPlayerView;
import com.wali.live.watchsdk.videothird.data.PullStreamerPresenter;
import com.xiaomi.player.Player;

import static com.wali.live.component.BaseSdkController.MSG_BACKGROUND_CLICK;
import static com.wali.live.component.BaseSdkController.MSG_NEW_FEED_URL;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_COMPLETED;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_ERROR;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_HIDE_LOADING;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_PAUSE;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_READY;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_SHOW_LOADING;
import static com.wali.live.component.BaseSdkController.MSG_SEEK_COMPLETED;
import static com.wali.live.component.BaseSdkController.MSG_SWITCH_TO_REPLAY_MODE;
import static com.wali.live.component.BaseSdkController.MSG_UPDATE_PLAY_PROGRESS;
import static com.wali.live.component.BaseSdkController.MSG_VIDEO_SIZE_CHANGED;

/**
 * Created by yangli on 2017/09/25.
 *
 * @module 详情-播放控制表现
 */
public class DetailPlayerPresenter extends ComponentPresenter<DetailPlayerView.IView>
        implements DetailPlayerView.IPresenter {
    private static final String TAG = "DetailPlayerPresenter";

    private PullStreamerPresenter mStreamerPresenter;

    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private Surface mSurface;

    private boolean mIsLandscape = false;
    private boolean mIsDetailMode = true;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public void setIsDetailMode(boolean isDetailMode) {
        mIsDetailMode = isDetailMode;
    }

    public DetailPlayerPresenter(
            @NonNull IEventController controller,
            @NonNull PullStreamerPresenter streamerPresenter) {
        super(controller);
        mStreamerPresenter = streamerPresenter;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_BACKGROUND_CLICK);

        registerAction(MSG_NEW_FEED_URL);

        registerAction(MSG_PLAYER_PAUSE);
        registerAction(MSG_UPDATE_PLAY_PROGRESS);
        registerAction(MSG_PLAYER_SHOW_LOADING);
        registerAction(MSG_PLAYER_HIDE_LOADING);
        registerAction(MSG_PLAYER_READY);
        registerAction(MSG_PLAYER_ERROR);
        registerAction(MSG_SEEK_COMPLETED);
        registerAction(MSG_PLAYER_COMPLETED);
        registerAction(MSG_VIDEO_SIZE_CHANGED);
        if (mView != null) {
            mView.start();
            mView.showLoading(true);
        }
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        if (mView != null) {
            mView.stop();
        }
    }

    @Override
    public void startPlay() {
        mView.showLoading(true);
        if (mStreamerPresenter.isStarted()) {
            mStreamerPresenter.resumeWatch();
        } else {
            mStreamerPresenter.startWatch();
        }
    }

    @Override
    public void pausePlay() {
        mStreamerPresenter.pauseWatch();
    }

    @Override
    public void switchToFullScreen() {
        postEvent(MSG_SWITCH_TO_REPLAY_MODE);
    }

    @Override
    public void seekTo(float progress) {
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
            mStreamerPresenter.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFit,
                    mSurfaceWidth, mSurfaceHeight);
            updateShiftUp();
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

    private void updateShiftUp() {
        if (mIsDetailMode || mIsLandscape) {
            mStreamerPresenter.shiftUp(0);
            return;
        }
        if (mSurfaceWidth == 0 || mSurfaceHeight == 0) {
            mStreamerPresenter.shiftUp(0);
        } else if (mVideoWidth == 0 || mVideoHeight == 0) {
            mStreamerPresenter.shiftUp(0);
        } else if (mVideoWidth * 16 > mVideoHeight * 9) {
            float ratio = (mSurfaceHeight - mSurfaceWidth * 9 / 16) * 0.25f / mSurfaceHeight;
            mStreamerPresenter.shiftUp(ratio);
        } else {
            mStreamerPresenter.shiftUp(0);
        }
    }

    private void onVideoSizeChange(int width, int height) {
        if (mVideoWidth != width || mVideoHeight != height) {
            mVideoWidth = width;
            mVideoHeight = height;
            updateShiftUp();
        }
    }

    private void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        mIsLandscape = isLandscape;
        updateShiftUp();
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
            case MSG_BACKGROUND_CLICK:
                mView.onChangeVisibility();
                return true;
            case MSG_PLAYER_PAUSE:
                pausePlay();
                mView.pause();
                return true;
            case MSG_NEW_FEED_URL:
                mStreamerPresenter.stopWatch();
                mStreamerPresenter.setOriginalStreamUrl((String) params.getItem(0));
                startPlay();
                return true;
            case MSG_UPDATE_PLAY_PROGRESS:
                mView.onUpdateProgress((int) (mStreamerPresenter.getCurrentPosition() / 1000));
                return true;
            case MSG_PLAYER_HIDE_LOADING:
            case MSG_SEEK_COMPLETED: // fall through
                mView.showLoading(false);
                return true;
            case MSG_PLAYER_SHOW_LOADING:
                mView.showLoading(true);
                return true;
            case MSG_PLAYER_READY:
                mView.onUpdateDuration((int) (mStreamerPresenter.getDuration() / 1000));
                return true;
            case MSG_PLAYER_ERROR:
            case MSG_PLAYER_COMPLETED: // fall through
                mView.reset();
                return true;
            case MSG_VIDEO_SIZE_CHANGED:
                onVideoSizeChange((int) params.getItem(0), (int) params.getItem(1));
                return true;
            default:
                break;
        }
        return false;
    }
}
