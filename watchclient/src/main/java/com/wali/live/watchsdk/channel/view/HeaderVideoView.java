package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.base.activity.BaseSdkActivity;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.base.utils.display.DisplayUtils;
import com.base.utils.network.NetworkUtils;
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
    private long mCurTs = 0l;        //标记播放到的时间戳
    private boolean mIsSilent = true;    // true : 静音  false:有声音

    private Handler mUIHandler = new Handler(Looper.getMainLooper());
    private HeaderVideoPresenter mHeaderVideoPresenter;

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
        MyLog.w(TAG, "setData videoUrl=" + videoUrl + " coverUrl=" + coverUrl);
        mCoverUrl = coverUrl;
        mVideoUrl = videoUrl;
        mCoverIv.setVisibility(View.VISIBLE);
        FrescoWorker.loadImage(mCoverIv,
                ImageFactory.newHttpImage(mCoverUrl).setWidth(getWidth()).build());
        //wifi play. others (no network or 4g,2g,3g) not play. show cover.
        if (NetworkUtils.isWifi(getContext()) && !TextUtils.isEmpty(mVideoUrl)) {
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
        initData(context);
    }

    private void initView(Context context) {
        inflate(context, R.layout.header_video_view, this);
        //view group's onDraw does not execute.
        setWillNotDraw(false);
        mVideoView = $(R.id.video_player_view);
        mVideoView.setOuterCallBack(this);
        mCoverIv = $(R.id.player_bg_iv);
        mVolumeIv = $(R.id.volume_iv);
        mVolumeIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsSilent = !mVolumeIv.isSelected();
                mVolumeIv.setSelected(mIsSilent);
                mVideoView.mute(mIsSilent);

            }
        });
    }

    private void initData(Context context) {
        mPath = new Path();
        mCurTs = 0l;
        mIsSilent = true;
        mHeaderVideoPresenter = new HeaderVideoPresenter(mVideoView);
        if (context instanceof BaseSdkActivity) {
            ((BaseSdkActivity) context).addPresent(mHeaderVideoPresenter);
        }
    }

    public void startVideo() {
        setVideoPath(mVideoUrl);
    }

    private void setVideoPath(String videoUrl) {
        if (TextUtils.isEmpty(videoUrl)) {
            return;
        }
        if (mCurTs > 0) {
            resumeVideo();
            mCoverIv.setVisibility(View.GONE);
            mCurTs = 0;
        } else {
            mVideoView.play(videoUrl);
        }
        mVideoView.mute(mIsSilent);
        mVolumeIv.setSelected(mIsSilent);
    }

    public void resumeVideo() {
        mVideoView.resume();
    }

    public void pauseVideo() {
        mVideoView.pause();
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
        pauseVideo();
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
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mCurTs = mVideoView.getCurrentPosition();
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

    public static class HeaderVideoPresenter extends RxLifeCyclePresenter {
        private static String TAG = "HeaderVideoPresenter";
        @NonNull
        private VideoPlayerWrapperView mVideoView;

        public HeaderVideoPresenter(@NonNull VideoPlayerWrapperView videoView) {
            mVideoView = videoView;
        }

        @Override
        public void destroy() {
            super.destroy();
            if (mVideoView != null) {
                MyLog.w(TAG, "destroy");
                mVideoView.release();
            }
        }
    }

}
