package com.mi.liveassistant.engine.player.widget;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;

/**
 * Created by lan on 16-1-4.
 * 最简的VideoPlayerView，慢慢完善
 * </p>
 * 记得同步修改VideoPlayerTextureView，以后统一
 */
public class VideoPlayerView extends VideoSurfaceView implements IVideoView, IPlayerView {
    private static final String TAG = VideoPlayerView.class.getSimpleName();

    /**
     * @notice same as FeedsContentModel type
     */
    public final static int FEEDSCONTENT_TYPE_LIVE = 0;
    public final static int FEEDSCONTENT_TYPE_VIDEO = 2;
    public final static int FEEDSCONTENT_TYPE_REPLAY = 3;

    private SurfaceHolder mSurfaceHolder = null;
    private VideoPlayerPresenter mVideoPlayerPresenter;

    // -1 直播间; 0 feeds列表的直播;2 feeds列表的回放;3 feeds列表的小视频
    private int mType = -1;

    private int mVideoWidth;
    private int mVideoHeight;
    private int mRotateBtnBottomMargin;

    public VideoPlayerView(Context context) {
        super(context);
        init(context);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = View.getDefaultSize(mVideoHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public VideoPlayerPresenter getVideoPlayerPresenter() {
        return mVideoPlayerPresenter;
    }

    private void init(Context context) {
        mVideoWidth = 0;
        mVideoHeight = 0;
        mVideoPlayerPresenter = new VideoPlayerPresenter(mVideoWidth, mVideoHeight);
        mVideoPlayerPresenter.setView(this);
        // 设置SurfaceHolder callback
        getHolder().addCallback(mSHCallback);

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

        if (context instanceof Activity) {
            ((Activity) context).setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }
    }


    // 清除背景
//    private void clearCanvas(SurfaceHolder holder) {
//        if (!mIsClearCanvas) {
//            return;
//        }
//        if (holder != null) {
//            Canvas canvas = holder.lockCanvas();
//            if (canvas != null) {
//                MyLog.d(TAG, "clearCanvas");
//                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                holder.unlockCanvasAndPost(canvas);
//            }
//        }
//    }
//
    private SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            MyLog.w(TAG, "surfaceCreated");
            //clearCanvas(holder);

            mSurfaceHolder = holder;
            mVideoPlayerPresenter.onSurfaceAvailable(mSurfaceHolder.getSurface());
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                                   int h) {
            MyLog.d(TAG, "surfaceChanged");

            mSurfaceHolder = holder;
            mVideoPlayerPresenter.onSurfaceChanged(mSurfaceHolder.getSurface());
            invalidate();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            MyLog.d(TAG, "surfaceDestroyed");
            mVideoPlayerPresenter.onSurfaceDestroyed();
        }
    };

    /**
     * 加黑边
     */
    public void setVideoLayout() {
        mVideoHeight = mVideoPlayerPresenter.getVideoHeight();
        mVideoWidth = mVideoPlayerPresenter.getVideoWidth();
        if (mVideoHeight > 0 && mVideoWidth > 0) {
            if (mType == FEEDSCONTENT_TYPE_LIVE || mType == FEEDSCONTENT_TYPE_REPLAY) {
                if (mVideoWidth > mVideoHeight) {
                    //如果修改请注意布局为RelativeLayout
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
                    float videoRatio = ((float) (mVideoWidth)) / mVideoHeight;
                    MyLog.d(TAG, "videoRatio = " + videoRatio);
                    lp.height = (int) (GlobalData.screenHeight / videoRatio);
                    lp.width = GlobalData.screenHeight;
                    lp.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
                    setLayoutParams(lp);
                } else {
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
                    float videoRatio = ((float) (mVideoWidth)) / mVideoHeight;
                    MyLog.d(TAG, "videoRatio = " + videoRatio);
                    lp.height = GlobalData.screenWidth;
                    lp.width = (int) (GlobalData.screenWidth * videoRatio);
                    lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                    setLayoutParams(lp);
                }
            } else if (mType == FEEDSCONTENT_TYPE_VIDEO) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
                lp.height = GlobalData.screenWidth;
                lp.width = GlobalData.screenWidth;
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                setLayoutParams(lp);
            }
        }
    }

    @Override
    public void adjustVideoLayout(boolean isLandscape) {
        MyLog.w(TAG, "adjustVideoLayout isLandscape=" + isLandscape);
        mVideoWidth = mVideoPlayerPresenter.getVideoWidth();
        mVideoHeight = mVideoPlayerPresenter.getVideoHeight();
        float frameRatio = 1f * mVideoWidth / mVideoHeight;
        if (mVideoHeight > 0 && mVideoWidth > 0 && mType == -1 && !isLandscape && mVideoWidth > mVideoHeight) {
            int curMargin = (GlobalData.screenHeight - GlobalData.screenWidth * mVideoHeight / mVideoWidth) / 2;
            if (frameRatio > 1.33f && frameRatio < 1.78f) {
                int targetMargin = DisplayUtils.dip2px(140);
                float distance = curMargin - targetMargin;
                mVideoPlayerPresenter.shiftUp(distance * 2 / GlobalData.screenHeight);
                mRotateBtnBottomMargin = curMargin + (int) distance + DisplayUtils.dip2px(3.33f);
            } else {
                mRotateBtnBottomMargin = curMargin + DisplayUtils.dip2px(3.33f);
            }
        }
    }

    @Override
    public void onSetVideoURICompleted() {
        requestLayout();
        invalidate();
    }

    /**
     * 用来返回视频是否是横屏流
     */
    public boolean isHorizontalScreenFlow() {
        if (mVideoWidth == 0 || mVideoHeight == 0) {
            return false;
        }
        return mVideoWidth >= mVideoHeight;
    }

    @Override
    public IPlayerPresenter getPlayerPresenter() {
        return mVideoPlayerPresenter;
    }

    @Override
    public int getRotateBtnBottomMargin() {
        return mRotateBtnBottomMargin;
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onConfigurationChanged() {

    }

    @Override
    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }
}
