package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.mi.live.engine.player.widget.IPlayerCallBack;
import com.mi.live.engine.player.widget.VideoPlayerPresenter;
import com.mi.live.engine.player.widget.VideoPlayerTextureView;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel.BaseLiveItem;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;

/**
 * Created by lan on 16/9/8.
 *
 * @module 频道
 */
public class BannerVideoView extends RelativeLayout implements IPlayerCallBack {
    private static final String TAG = BannerVideoView.class.getSimpleName();

    BaseImageView mBannerIv;
    VideoPlayerTextureView mVideoView;
    TextView mSingleTv;
    TextView mTypeTv;
    protected VideoPlayerPresenter mVideoPresenter;
    private BaseLiveItem mLiveItem;
    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    public <V extends View> V $(int resId) {
        return (V) findViewById(resId);
    }


    private Runnable mVideoRunnable = new Runnable() {
        @Override
        public void run() {
            startVideo();
        }
    };

    public BannerVideoView(Context context) {
        super(context);
        init();
    }

    public BannerVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BannerVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        inflate(getContext(), R.layout.video_banner_view, this);
        ButterKnife.bind(this);

        mVideoView.setVideoTransMode(VideoPlayerTextureView.TRANS_MODE_CENTER_INSIDE);
    }

    public void initView() {
        mBannerIv = $(R.id.banner_iv);
        mVideoView = $(R.id.video_player_view);
        mSingleTv = $(R.id.single_tv);
        mTypeTv = $(R.id.type_tv);
    }

    public void postVideoRunnable() {
        mUIHandler.removeCallbacks(mVideoRunnable);
        mUIHandler.postDelayed(mVideoRunnable, 2000);
    }

    public void removeVideoRunnable() {
        mUIHandler.removeCallbacks(mVideoRunnable);
        stopVideo();
    }

    private void startVideo() {
        MyLog.v(TAG, "startVideo");
        if (mLiveItem == null || TextUtils.isEmpty(mLiveItem.getVideoUrl())) {
            return;
        }
        if (mVideoPresenter == null) {
            mVideoPresenter = mVideoView.getVideoPlayerPresenter();
            mVideoPresenter.setVideoPlayerCallBack(this);
            mVideoPresenter.setNeedReset(false);
        }
        setVideoPath(mLiveItem.getId(), mLiveItem.getVideoUrl());
    }

    public void bindData(BaseLiveItem item) {
        MyLog.v(TAG, "bindData");
        mLiveItem = item;
        bindTextView(mSingleTv, item.getTitleText());
        bindTextView(mTypeTv, item.getUpRightText());
        FrescoWorker.loadImage(mBannerIv,
                ImageFactory.newHttpImage(item.getImageUrl(AvatarUtils.SIZE_TYPE_AVATAR_LARGE))
                        .build()
        );
        postVideoRunnable();
    }

    private void setVideoPath(String id, String videoUrl) {
        if (TextUtils.isEmpty(videoUrl)) {
            return;
        }
        mVideoPresenter.setVideoPath(videoUrl, Uri.parse(videoUrl).getHost());
        mVideoPresenter.setVolume(0, 0);
    }

    public void stopVideo() {
        MyLog.v(TAG, "stopVideo");
        if (mVideoPresenter != null) {
            mVideoPresenter.release();
        }
    }

    public void enterWatch() {
        stopVideo();

        mSingleTv.setVisibility(View.VISIBLE);
        mTypeTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onPrepared() {
        MyLog.e(TAG, "onPrepared");
        mSingleTv.setVisibility(View.GONE);
        mTypeTv.setVisibility(View.GONE);
    }

    @Override
    public void onCompletion() {
    }

    @Override
    public void onError(int errCode) {
        MyLog.e(TAG, "onError errCode=" + errCode);
    }

    @Override
    public void onBufferingUpdate(int percent) {
    }

    @Override
    public void onInfo(int info) {
    }

    @Override
    public void onInfo(Message msg) {
    }

    @Override
    public void onSeekComplete() {
    }

    @Override
    public void requestOrientation(int playMode) {
    }

    @Override
    public void onReleased() {
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MyLog.e(TAG, "onAttachedToWindow");
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MyLog.e(TAG, "onDetachedFromWindow");
        unregisterEventBus();
        removeVideoRunnable();
    }

    private void unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    protected void bindTextView(TextView tv, String text) {
        if (tv == null) {
            return;
        }
        if (!TextUtils.isEmpty(text)) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
        } else {
            tv.setVisibility(View.GONE);
        }
    }
}
