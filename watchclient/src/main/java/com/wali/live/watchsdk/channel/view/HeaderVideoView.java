package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.network.NetworkUtils;
import com.mi.live.engine.player.widget.VideoPlayerPresenter;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.view.VideoPlayerWrapperView;

/**
 * Created by zyh on 2017/8/29.
 */
public class HeaderVideoView extends RelativeLayout implements VideoPlayerWrapperView.IOuterCallBack {
    private final static String TAG = "HeaderVideoView";

    private static final int ROUND_RADIUS = DisplayUtils.dip2px(3.33f);
    private VideoPlayerWrapperView mVideoView;
    private BaseImageView mCoverIv;
    private ImageView mVolumeIv;

    private String mVideoUrl;
    private String mCoverUrl;

    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    private Path mPath;
    private RectF mRectF;

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
        //wifi play. others (no network or 4g,2g,3g) not play. show cover.
        if (NetworkUtils.isWifi(getContext())) {
            postVideoRunnable();
        }
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
        mVideoView.setOuterCallBack(this);
        mCoverIv = $(R.id.player_bg_iv);
        mVolumeIv = $(R.id.volume_iv);
        mVolumeIv.setSelected(false);
        mVolumeIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mVolumeIv.setSelected(!mVolumeIv.isSelected());
                mVideoView.mute(!mVolumeIv.isSelected());
            }
        });
        mPath = new Path();
    }

    public void startVideo() {
        setVideoPath(mVideoUrl);
    }

    private void setVideoPath(String videoUrl) {
        if (TextUtils.isEmpty(videoUrl)) {
            return;
        }
        mVideoView.play(videoUrl);
        mVideoView.mute(true);
    }

    public void stopVideo() {
        MyLog.v(TAG, "stopVideo");
        mVideoView.release();
    }

    public void postVideoRunnable() {
        mUIHandler.removeCallbacks(mVideoRunnable);
        mUIHandler.postDelayed(mVideoRunnable, 1000);
    }

    public void removeVideoRunnable() {
        mUIHandler.removeCallbacks(mVideoRunnable);
        stopVideo();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRectF == null) {
            mRectF = new RectF(0, 0, getWidth(), getHeight());
        }
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        mPath.addRoundRect(mRectF, ROUND_RADIUS, ROUND_RADIUS, Path.Direction.CW);
        canvas.clipPath(mPath);
        super.onDraw(canvas);
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
    public void onPrepared() {
    }

    @Override
    public void onCompletion() {
        postVideoRunnable();
    }

    @Override
    public void onBufferingStart() {

    }

    @Override
    public void onBufferingEnd() {
        mCoverIv.setVisibility(View.GONE);
    }

    @Override
    public void onError(int errCode) {
        MyLog.e(TAG, "onError errCode=" + errCode);
        postVideoRunnable();
    }
}
