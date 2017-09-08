package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.HttpImage;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.mi.live.engine.player.widget.IPlayerCallBack;
import com.mi.live.engine.player.widget.VideoPlayerPresenter;
import com.mi.live.engine.player.widget.VideoPlayerTextureView;
import com.wali.live.watchsdk.R;

/**
 * Created by zyh on 2017/8/29.
 */

public class HeaderVideoView extends RelativeLayout implements IPlayerCallBack {
    private final static String TAG = "HeaderVideoView";

    private static final int ROUND_RADIUS = DisplayUtils.dip2px(6.67f);
    VideoPlayerTextureView mVideoView;
    BaseImageView mCoverIv;
    protected VideoPlayerPresenter mVideoPresenter;

    private String mVideoUrl;
    private String mCoverUrl;

    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    protected <T extends View> T $(int resId) {
        return (T) findViewById(resId);
    }


    private Runnable mVideoRunnable = new Runnable() {
        @Override
        public void run() {
            startVideo();
        }
    };

    public void setData(String videoUrl, String coverUrl) {
        mCoverUrl = coverUrl;
        mVideoUrl = videoUrl;
        if (TextUtils.isEmpty(mVideoUrl) || TextUtils.isEmpty(mCoverUrl)) {
            return;
        }
        FrescoWorker.loadImage(mCoverIv,
                ImageFactory.newHttpImage(mCoverUrl).setWidth(getWidth()).build());
        postVideoRunnable();
    }

    public HeaderVideoView(Context context) {
        this(context, null);
    }

    public HeaderVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        inflate(context, R.layout.header_video_view, this);
        //view group's onDraw does not execute.
        setWillNotDraw(false);
        mVideoView = $(R.id.video_player_view);
        mCoverIv = $(R.id.player_bg_iv);
    }

    public void startVideo() {
        if (mVideoPresenter == null) {
            mVideoPresenter = mVideoView.getVideoPlayerPresenter();
            mVideoPresenter.setVideoPlayerCallBack(this);
            mVideoPresenter.setNeedReset(false);
        }
        setVideoPath(mVideoUrl);
    }

    private void setVideoPath(String videoUrl) {
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

    public void postVideoRunnable() {
        mUIHandler.removeCallbacks(mVideoRunnable);
        mUIHandler.postDelayed(mVideoRunnable, 2000);
    }

    public void removeVideoRunnable() {
        mUIHandler.removeCallbacks(mVideoRunnable);
        stopVideo();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Path path = new Path();
        RectF rectF = new RectF(0, 0, getWidth(), getHeight());
        path.addRoundRect(rectF, ROUND_RADIUS, ROUND_RADIUS, Path.Direction.CW);
        canvas.clipPath(path);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MyLog.e(TAG, "onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MyLog.e(TAG, "onDetachedFromWindow");
        removeVideoRunnable();
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onPrepared() {
        mCoverIv.setVisibility(View.GONE);
    }

    @Override
    public void onCompletion() {
        postVideoRunnable();
    }

    @Override
    public void onError(int errCode) {
        MyLog.e(TAG, "onError errCode=" + errCode);
        postVideoRunnable();
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
}
