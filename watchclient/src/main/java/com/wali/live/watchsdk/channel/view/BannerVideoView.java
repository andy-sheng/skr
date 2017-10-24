package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.thornbirds.component.IEventObserver;
import com.thornbirds.component.IParams;
import com.wali.live.component.BaseSdkController;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.view.presenter.HeaderVideoPresenter;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel.BaseLiveItem;

/**
 * Created by lan on 16/9/8.
 *
 * @module 频道--realTime true
 */
public class BannerVideoView extends RelativeLayout implements IEventObserver {
    private static final String TAG = BannerVideoView.class.getSimpleName();

    private BaseImageView mBannerIv;
    private TextureView mVideoView;
    private TextView mSingleTv;
    private TextView mTypeTv;
    private HeaderVideoPresenter mPresenter;
    private BaseLiveItem mLiveItem;
    private Handler mUIHandler = new Handler(Looper.getMainLooper());
    private BaseSdkController mController = new BaseSdkController() {
        @Override
        protected String getTAG() {
            return "BannerVideoController";
        }
    };

    private final <V extends View> V $(int resId) {
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

    private void init() {
        inflate(getContext(), R.layout.video_banner_view, this);
        initView();
        initPresenter();
    }

    private void initView() {
        mBannerIv = $(R.id.banner_iv);
        mVideoView = $(R.id.video_player_view);
        mSingleTv = $(R.id.single_tv);
        mTypeTv = $(R.id.type_tv);
    }

    private void initPresenter() {
        mPresenter = new HeaderVideoPresenter(mController, true);
        mPresenter.setView(mVideoView);
    }

    private void postVideoRunnable() {
        mUIHandler.removeCallbacks(mVideoRunnable);
        mUIHandler.postDelayed(mVideoRunnable, 2000);
    }

    public void removeVideoRunnable() {
        mUIHandler.removeCallbacks(mVideoRunnable);
        stopVideo();
    }

    public void bindData(BaseLiveItem item) {
        MyLog.v(TAG, "bindData");
        if (item == null || TextUtils.isEmpty(item.getVideoUrl())) {
            return;
        }
        mLiveItem = item;
        bindTextView(mSingleTv, item.getTitleText());
        bindTextView(mTypeTv, item.getUpRightText());
        FrescoWorker.loadImage(mBannerIv,
                ImageFactory.newHttpImage(item.getImageUrl(AvatarUtils.SIZE_TYPE_AVATAR_LARGE))
                        .build());
        setVideoPath(mLiveItem.getVideoUrl());
        postVideoRunnable();
    }

    private void setVideoPath(String videoUrl) {
        if (TextUtils.isEmpty(videoUrl)) {
            return;
        }
        mPresenter.setOriginalStreamUrl(videoUrl);
        mPresenter.mute(true);
    }

    private void startVideo() {
        MyLog.v(TAG, "startVideo");
        if (mPresenter != null) {
            mPresenter.startVideo();
        }

    }

    private void stopVideo() {
        MyLog.v(TAG, "stopVideo");
        if (mPresenter != null) {
            mPresenter.destroyVideo();
        }
    }

    public void enterWatch() {
        stopVideo();

        mSingleTv.setVisibility(View.VISIBLE);
        mTypeTv.setVisibility(View.VISIBLE);
    }

    private void onPlayerReady() {
        MyLog.e(TAG, "onPrepared");
        mSingleTv.setVisibility(View.GONE);
        mTypeTv.setVisibility(View.GONE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerAction();
        MyLog.e(TAG, "onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MyLog.e(TAG, "onDetachedFromWindow");
        removeVideoRunnable();
        unregisterAction();
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

    private void registerAction() {
        mController.registerObserverForEvent(BaseSdkController.MSG_PLAYER_READY, this); //prepared
    }

    private void unregisterAction() {
        if (mController != null) {
            mController.unregisterObserver(this);
        }
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        switch (event) {
            case BaseSdkController.MSG_PLAYER_READY:
                onPlayerReady();
                break;
            case BaseSdkController.MSG_PLAYER_ERROR:
            case BaseSdkController.MSG_PLAYER_COMPLETED:
                startVideo();
                break;
            default:
                break;
        }
        return false;
    }
}
