package com.wali.live.watchsdk.component.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.base.dialog.MyAlertDialog;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.network.NetworkUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.engine.player.widget.VideoPlayerTextureView;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.utils.AppNetworkUtils;
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
        implements IComponentView<VideoDetailPlayerView.IPresenter, VideoDetailPlayerView.IView>,
        View.OnClickListener {
    private static final String TAG = "VideoDetailPlayerView";

    private final static int PLAYER_START = 0;
    private final static int PLAYER_SEEK = 1;
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
    private boolean mIsNeedStartPlayer = false;

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
            AvatarUtils.loadAvatarByUidTs(mCoverIv, mMyRoomData.getUid(),
                    mMyRoomData.getAvatarTs(), AvatarUtils.SIZE_TYPE_AVATAR_LARGE, false);
            if (!TextUtils.isEmpty(mMyRoomData.getVideoUrl())
                    && NetworkUtils.hasNetwork(this.getContext())
                    && !check4GNet(PLAYER_START, 0)) {
                startPlayer();
            } else {
                mIsNeedStartPlayer = true;
                showPlayBtn(true);
            }
        }
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.float_video_view, this);
        {
            mLoadingProgressBar = $(R.id.loading_bar);
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
            mVideoPlayerPresenterEx = new VideoPlayerPresenterEx(this.getContext(),
                    mVideoPlayerView, mDetailSeekBar, null, false);
            mVideoPlayerPresenterEx.setSeekBarHideDelay(4000);
            mVideoPlayerPresenterEx.setSeekBarFullScreenBtnVisible(true);
        }
    }

    private void showLoadingView() {
        mLoadingProgressBar.setVisibility(VISIBLE);
        mPlayBtn.setVisibility(GONE);
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
        MyLog.w(TAG, "startPlayer url=" + mMyRoomData.getVideoUrl());
        mCoverIv.setVisibility(VISIBLE);
        showLoadingView();
        if (mVideoPlayerPresenterEx != null) {
//            if (!mVideoPlayerPresenterEx.isActivate()) {
            mVideoPlayerPresenterEx.play(mMyRoomData.getVideoUrl());
            mVideoPlayerPresenterEx.setTransMode(VideoPlayerTextureView.TRANS_MODE_CENTER_INSIDE);
//            }
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

    private void onPlayingState() {
        MyLog.w(TAG, "onPlayingState");
        hideLoadingView();
        if (mCoverIv.getVisibility() == VISIBLE) {
            mCoverIv.setVisibility(GONE);
        }
        showPlayBtn(false);
    }

    private long getPlayingTime() {
        MyLog.w(TAG, "getPlayingTime");
        if (mVideoPlayerPresenterEx != null) {
            return mVideoPlayerPresenterEx.getCurrentPosition();
        }
        return 0;
    }

    private void onCompleteState() {
        MyLog.w(TAG, "onCompleteState");
        if (mCoverIv.getVisibility() != VISIBLE) {
            mCoverIv.setVisibility(VISIBLE);
        }
        showPlayBtn(true);
        mIsNeedStartPlayer = true;
    }

    public void destroy() {
        MyLog.w(TAG, "destroy");
        if (mVideoPlayerPresenterEx != null) {
            mVideoPlayerPresenterEx.destroy();
        }
    }

    public void switchToFullScreen(boolean fullScreen) {
        if (mDetailSeekBar != null) {
            mDetailSeekBar.setFullscreen(fullScreen);
        }
    }

    public void showOrHideFullScreenBtn(boolean isShow) {
        if (mDetailSeekBar != null) {
            mDetailSeekBar.showOrHideFullScreenBtn(isShow);
        }
    }

    public void onSeekBarContainerClick() {
        mVideoPlayerPresenterEx.onSeekBarContainerClick();
    }

    private boolean check4GNet(final int from, final long playingTime) {
        if (AppNetworkUtils.is4g()) {
            showPlayBtn(true);
            MyAlertDialog alertDialog = new MyAlertDialog.Builder(this.getContext()).create();
            alertDialog.setMessage(GlobalData.app().getString(R.string.live_traffic_tip));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, GlobalData.app().getString(R.string.live_traffic_positive), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (from) {
                        case PLAYER_START:
                            startPlayer();
                            break;
                        case PLAYER_SEEK:
                            showLoadingView();
                            mVideoPlayerPresenterEx.seekTo(playingTime);
                            break;
                        default:
                            break;

                    }
                    dialog.dismiss();
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, GlobalData.app().getString(R.string.live_traffic_negative), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mPresenter.onBackPress();
                    dialog.dismiss();
                }
            });
            alertDialog.setCancelable(false);
            alertDialog.show();
            return true;
        }
        return false;
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
            public void showPlayBtn(boolean show) {
                VideoDetailPlayerView.this.showPlayBtn(show);
            }

            @Override
            public void onStartPlayer() {
                VideoDetailPlayerView.this.startPlayer();
            }

            @Override
            public void onPausePlayer() {
                VideoDetailPlayerView.this.pausePlayer();
            }

            @Override
            public long onGetPlayingTime() {
                return VideoDetailPlayerView.this.getPlayingTime();
            }


            @Override
            public void onResetPlayer() {
                VideoDetailPlayerView.this.resetPlayer();
            }

            @Override
            public void onCompleteState() {
                VideoDetailPlayerView.this.onCompleteState();
            }

            @Override
            public void onDestroy() {
                VideoDetailPlayerView.this.destroy();
            }
        }
        return new ComponentView();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.play_button) {
            if (!NetworkUtils.hasNetwork(getContext())) {
                ToastUtils.showToast(getContext(), R.string.network_disable);
                return;
            }
            if (mIsNeedStartPlayer) {
                mIsNeedStartPlayer = false;
                resetPlayer();
                startPlayer();
            } else {
                resumePlayer();
            }
        } else if (i == R.id.player_container) {
            mVideoPlayerPresenterEx.onSeekBarContainerClick();
        }
    }

    public interface IPresenter {
        /**
         * 结束activity接口
         */
        void onBackPress();
    }

    public interface IView extends IViewProxy {
        /**
         * 播放器打开
         */
        void onStartPlayer();

        /**
         * 播放器暂停
         */
        void onPausePlayer();

        /**
         * 播放器的重置接口
         */
        void onResetPlayer();

        /**
         * 播放器获取当前播放时间
         *
         * @return
         */
        long onGetPlayingTime();

        /**
         * 播放器正在播放更新ui接口
         */
        void onPlaying();

        /**
         * 暂停按钮的展示与隐藏
         *
         * @param show
         */
        void showPlayBtn(boolean show);

        /**
         * 播放完成ui更新接口
         */
        void onCompleteState();

        /**
         * 释放资源
         */
        void onDestroy();
    }
}
