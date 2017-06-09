package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.engine.player.widget.VideoPlayerTextureView;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.video.widget.player.ReplaySeekBar;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.watch.presenter.VideoPlayerPresenterEx;

/**
 * Created by zyh on 2017/05/31.
 *
 * @module 详情页播放器view
 */
public class VideoDetailPlayerView extends RelativeLayout
        implements IComponentView<VideoDetailPlayerView.IPresenter, VideoDetailPlayerView.IView>, View.OnClickListener {
    private static final String TAG = "VideoDetailPlayerView";

    private RoomBaseDataModel mMyRoomData;
    @Nullable
    protected IPresenter mPresenter;
    private VideoPlayerPresenterEx mVideoPlayerPresenterEx;

    private VideoPlayerTextureView mVideoPlayerView;
    private ReplaySeekBar mDetailSeekBar;
    private ProgressBar mLoadingProgressBar;
    private ImageButton mPlayBtn;
    private BaseImageView mCoverIv;
    private ImageView mBackIv;
    private View mPlayerContainer;

    public VideoDetailPlayerView(Context context) {
        this(context, null);
    }

    public VideoDetailPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoDetailPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public void setMyRoomData(RoomBaseDataModel myRoomData) {
        mMyRoomData = myRoomData;
        if (mMyRoomData != null) {
            AvatarUtils.loadAvatarByUid(mCoverIv, mMyRoomData.getUid(), false);
            startPlayer();
        }
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.float_video_view, this);
        {
            mLoadingProgressBar = $(R.id.loading_iv);
            mDetailSeekBar = $(R.id.detail_seek_bar);
            mCoverIv = $(R.id.cover_iv);
            mPlayBtn = $(R.id.play_button);
            $click(mPlayBtn, this);
            mBackIv = $(R.id.back_iv);
            $click(mBackIv, this);
            mPlayerContainer = $(R.id.player_container);
            $click(mPlayerContainer, this);
        }
        {
            mVideoPlayerView = $(R.id.video_player_texture_view);
            mVideoPlayerPresenterEx = new VideoPlayerPresenterEx(this.getContext(), mVideoPlayerView, mDetailSeekBar, null, false);
            mVideoPlayerPresenterEx.setSeekBarHideDelay(4000);
            mVideoPlayerPresenterEx.setSeekBarFullScreenBtnVisible(true);
        }
    }

    private void showLoadingView() {
        mLoadingProgressBar.setVisibility(VISIBLE);
    }

    private void hideLoadingView() {
        mLoadingProgressBar.setVisibility(GONE);
    }

    private void resetPlayer() {
        MyLog.w(TAG, "resetPlayer");
        if (mVideoPlayerPresenterEx != null) {
            mVideoPlayerPresenterEx.reset();
        }
    }

    private void startPlayer() {
        MyLog.w(TAG, "startPlayer");
        mCoverIv.setVisibility(VISIBLE);
        showLoadingView();
        if (mVideoPlayerPresenterEx != null) {
//            if (!mVideoPlayerPresenterEx.isActivate()) {
            mVideoPlayerPresenterEx.play(mMyRoomData.getVideoUrl());
            mVideoPlayerPresenterEx.setTransMode(VideoPlayerTextureView.TRANS_MODE_CENTER_INSIDE);
//            }
        }
    }

    private void stopPlayer() {
        MyLog.w(TAG, "stopPlayer");
        if (mVideoPlayerPresenterEx != null) {
            mVideoPlayerPresenterEx.destroy();
        }
    }

    private void pausePlayer() {
        MyLog.w(TAG, "pausePlayer");
        if (mVideoPlayerPresenterEx != null) {
            mVideoPlayerPresenterEx.pause();
        }
    }

    private void resumePlayer() {
        MyLog.w(TAG, "resumePlayer");
        if (mVideoPlayerPresenterEx != null) {
            mVideoPlayerPresenterEx.resume();
        }
    }

    private void showPlayBtn(boolean show) {
        MyLog.w(TAG, "showPlayBtn show=" + show);
        if (mPlayBtn.getVisibility() != View.VISIBLE && show) {
            mPlayBtn.setVisibility(View.VISIBLE);
        } else if (mPlayBtn.getVisibility() == View.VISIBLE && !show) {
            mPlayBtn.setVisibility(View.GONE);
        }
    }

    private void clickFullScreen() {
        MyLog.w(TAG, "clickFullScreen");
        if (mCoverIv.getVisibility() != VISIBLE) {
            mCoverIv.setVisibility(VISIBLE);
        }
        pausePlayer();
    }

    private void onPlayingState() {
        MyLog.w(TAG, "onPlayingState");
        hideLoadingView();
        if (mCoverIv.getVisibility() == VISIBLE) {
            mCoverIv.setVisibility(GONE);
        }
    }

    private long getPlayingTime() {
        MyLog.w(TAG, "getPlayingTime");
        if (mVideoPlayerPresenterEx != null) {
            return mVideoPlayerPresenterEx.getCurrentPosition();
        }
        return 0;
    }

    private void seekVideoPlayer(long playedTime) {
        MyLog.w(TAG, "seekVideoPlayer playedTime=" + playedTime);
        if (mVideoPlayerPresenterEx != null) {
            mVideoPlayerPresenterEx.resume();
            mVideoPlayerPresenterEx.seekTo(playedTime);
        }
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) VideoDetailPlayerView.this;
            }

            @Override
            public void onPlaying() {
                VideoDetailPlayerView.this.onPlayingState();
            }

            @Override
            public void onClickFullScreen() {
                VideoDetailPlayerView.this.clickFullScreen();
            }

            @Override
            public void showPlayBtn(boolean show) {
                VideoDetailPlayerView.this.showPlayBtn(show);
            }

            @Override
            public void onStartPlayer() {
                VideoDetailPlayerView.this.startPlayer();
            }

            @Override
            public void onResumePlayer() {
                VideoDetailPlayerView.this.resumePlayer();
            }

            @Override
            public void onPausePlayer() {
                VideoDetailPlayerView.this.pausePlayer();
            }

            @Override
            public void onStopPlayer() {
                VideoDetailPlayerView.this.stopPlayer();
            }

            @Override
            public long onGetPlayingTime() {
                return VideoDetailPlayerView.this.getPlayingTime();
            }

            @Override
            public void onSeekPlayer(long playedTime) {
                VideoDetailPlayerView.this.seekVideoPlayer(playedTime);
            }

            @Override
            public void onResetPlayer() {
                VideoDetailPlayerView.this.resetPlayer();
            }
        }
        return new ComponentView();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back_iv) {
            mPresenter.onBackPress();
        } else if (i == R.id.play_button) {
            resumePlayer();
        } else if (i == R.id.player_container) {
            mVideoPlayerPresenterEx.onSeekBarContainerClick();
        }
    }

    public interface IPresenter {
        void onBackPress();
    }

    public interface IView extends IViewProxy {
        void onPlaying();

        void showPlayBtn(boolean show);

        void onStartPlayer();

        void onResumePlayer();

        void onPausePlayer();

        void onClickFullScreen();

        void onStopPlayer();

        long onGetPlayingTime();

        void onSeekPlayer(long playedTime);

        void onResetPlayer();
    }
}
