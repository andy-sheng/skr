package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.TextureView;
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
import com.thornbirds.component.IEventObserver;
import com.thornbirds.component.IParams;
import com.wali.live.component.BaseSdkController;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.view.presenter.HeaderVideoPresenter;

/**
 * Created by zyh on 2017/8/29.
 */
public class HeaderVideoView extends RelativeLayout {
    private final static String TAG = "HeaderVideoView";

    private Action mAction = new Action();
    private final static int PLAYER_INIT = 0;
    private final static int PLAYER_PLAYING = 1;
    private final static int PLAYER_PAUSE = 2;

    private int mPlayerState = PLAYER_INIT;
    private boolean mIsSilent = true;    // true : 静音  false:有声音

    private static final int ROUND_RADIUS = DisplayUtils.dip2px(3.33f);
    private final PaintFlagsDrawFilter mPaintFlagsDrawFilter =
            new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Path mPath = new Path();
    private RectF mRectF;

    private TextureView mVideoView;
    private BaseImageView mCoverIv;
    private ImageView mVolumeIv;
    private String mVideoUrl;
    private String mCoverUrl;

    private Handler mUIHandler = new Handler(Looper.getMainLooper());
    private HeaderVideoPresenter mPresenter;
    private BaseSdkController mController = new BaseSdkController() {
        @Override
        protected String getTAG() {
            return "HeaderVideoController";
        }
    };

    private Runnable mVideoRunnable = new Runnable() {
        @Override
        public void run() {
            mPresenter.startVideo();
            mPlayerState = PLAYER_PLAYING;
            MyLog.v(TAG, "play mVideoUrl=" + mVideoUrl + "   mPlayerState" + mPlayerState);
        }
    };

    public void setData(String videoUrl, String coverUrl) {
        MyLog.w(TAG, "setData videoUrl=" + videoUrl + " coverUrl=" + coverUrl);
        mCoverUrl = coverUrl;
        mVideoUrl = videoUrl;
        mCoverIv.setVisibility(View.VISIBLE);
        FrescoWorker.loadImage(mCoverIv,
                ImageFactory.newHttpImage(mCoverUrl).setWidth(getWidth()).build());
        mPresenter.setOriginalStreamUrl(mVideoUrl);
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
        initPresenter(context);
    }

    private void initPresenter(Context context) {
        mPresenter = new HeaderVideoPresenter(mController);
        mPresenter.setView(mVideoView);

        if (context instanceof BaseSdkActivity) {
            ((BaseSdkActivity) context).addPresent(new HeaderVideoInnerPresenter());
        }
    }

    private void initView(Context context) {
        inflate(context, R.layout.header_video_view, this);
        //view group's onDraw does not execute.
        setWillNotDraw(false);
        mVideoView = $(R.id.video_player_view);

        mCoverIv = $(R.id.player_bg_iv);
        mVolumeIv = $(R.id.volume_iv);
        mVolumeIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsSilent = !mVolumeIv.isSelected();
                mVolumeIv.setSelected(mIsSilent);
                mPresenter.mute(mIsSilent);
            }
        });
    }

    private void openVideo() {
        MyLog.w(TAG, "openVideo start mVideoUrl=" + mVideoUrl + " mPlayerState=" + mPlayerState);
        //wifi play. others (no network or 4g,2g,3g) not play. show cover.
        if (!NetworkUtils.isWifi(getContext()) || TextUtils.isEmpty(mVideoUrl)) {
            MyLog.w(TAG, "not wifi or mVideoUrl is empty");
            return;
        }
        if (mPlayerState == PLAYER_INIT) {
            startVideo();
        } else if (mPlayerState == PLAYER_PAUSE) {
            mPresenter.resumeVideo();
            mPlayerState = PLAYER_PLAYING;
            mCoverIv.setVisibility(View.GONE);
        }
        mVolumeIv.setSelected(mIsSilent);
        mPresenter.mute(mIsSilent);
    }

    private void startVideo() {
        mUIHandler.removeCallbacks(mVideoRunnable);
        mUIHandler.postDelayed(mVideoRunnable, 200);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRectF == null) {
            mRectF = new RectF(0, 0, getWidth(), getHeight());
            mPath.addRoundRect(mRectF, ROUND_RADIUS, ROUND_RADIUS, Path.Direction.CW);
        }
        canvas.setDrawFilter(mPaintFlagsDrawFilter);
        canvas.clipPath(mPath);
        super.onDraw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MyLog.v(TAG, "onAttachedToWindow mPlayerState=" + mPlayerState);
        mAction.registerAction();
        openVideo();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MyLog.v(TAG, "onDetachedFromWindow mPlayerState=" + mPlayerState);
        mAction.unregisterAction();
        mUIHandler.removeCallbacks(mVideoRunnable);
        if (mPlayerState == PLAYER_PLAYING) {
            mPresenter.pauseVideo();
            mPlayerState = PLAYER_PAUSE;
        }
    }

    public class HeaderVideoInnerPresenter extends RxLifeCyclePresenter {
        private String TAG = "HeaderVideoPresenter";

        @Override
        public void resume() {
            super.resume();
            if (mPlayerState == PLAYER_PLAYING) {
                mPresenter.resumeVideo();
            }
        }

        @Override
        public void pause() {
            super.pause();
            mUIHandler.removeCallbacks(mVideoRunnable);
            if (mPlayerState == PLAYER_PLAYING) {
                mPresenter.pauseVideo();
            }
        }

        @Override
        public void destroy() {
            MyLog.w(TAG, "destroy");
            super.destroy();
            mPresenter.stopVideo();
        }
    }

    private class Action implements IEventObserver {

        private void registerAction() {
            mController.registerObserverForEvent(BaseSdkController.MSG_PLAYER_COMPLETED, this); //completed
            mController.registerObserverForEvent(BaseSdkController.MSG_PLAYER_ERROR, this); //error
            mController.registerObserverForEvent(BaseSdkController.MSG_PLAYER_HIDE_LOADING, this); //buffer_end
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
                    //注：这里是因为首次openVideo之后立马detachWindow，执行pause但是没有pause住流。
                    // 所以在prepare回调里面跟距mPlayerState状态在执行一次pause.
                    if (mPlayerState == PLAYER_PAUSE) {
                        mPresenter.pauseVideo();
                    }
                    break;
                case BaseSdkController.MSG_PLAYER_HIDE_LOADING:
                    mCoverIv.setVisibility(View.GONE);
                    break;
                case BaseSdkController.MSG_PLAYER_ERROR:
                    openVideo();
                    break;
                case BaseSdkController.MSG_PLAYER_COMPLETED:
                    startVideo();
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    protected <T extends View> T $(int resId) {
        return (T) findViewById(resId);
    }
}
