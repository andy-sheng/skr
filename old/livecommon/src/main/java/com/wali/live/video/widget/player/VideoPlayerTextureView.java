package com.wali.live.video.widget.player;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;
import com.wali.live.video.player.IVideoView;
import com.wali.live.video.player.presenter.IPlayerPresenter;
import com.wali.live.video.player.presenter.VideoPlayerPresenter;
import com.xiaomi.player.Player;

/**
 * Created by lan on 16-1-4.
 * VideoPlayerTextureView，继承textureView，功能和VideoPlayerView相同
 * </p>
 * 记得同步修改VideoPlayerView，以后统一
 */
public class VideoPlayerTextureView extends TextureView implements SurfaceTextureListener, IVideoView, IPlayerView {
    protected static final String TAG = VideoPlayerTextureView.class.getSimpleName();

    private int mVideoWidth, mVideoHeight;
    private int mOffset_X = 0, mOffset_Y = 0;
    private float mShiftUpRatio;

    public final static int FEEDSCONTENT_TYPE_LIVE = 0;
    public final static int FEEDSCONTENT_TYPE_VIDEO = 2;
    public final static int FEEDSCONTENT_TYPE_REPLAY = 3;

    public static final int TRANS_MODE_CENTER_CROP = 0;
    public static final int TRANS_MODE_CENTER_INSIDE = 1;
    public static final int TRANS_MODE_AUTO_FIT = 2;//根据视频流自适应，一般用户当前的TextureView是全屏播放的情况
    private int mVideoTransMode = TRANS_MODE_CENTER_CROP;

    // -1 直播间; 0 feeds列表的直播;2 feeds列表的回放;3 feeds列表的小视频
    protected long mType = -1;

    protected VideoPlayerPresenter mVideoPlayerPresenter;
    protected int mRotateBtnBottomMargin;

    public VideoPlayerTextureView(Context context) {
        super(context);
        initVideoView(context, null);
    }

    public VideoPlayerTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initVideoView(context, attrs);
    }

    public VideoPlayerTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initVideoView(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        MyLog.v(TAG, " onLayout " + changed + " " + left + " " + top + " " + right + " " + bottom);
        if (changed) {
            mVideoWidth = mVideoPlayerPresenter.getVideoWidth();
            mVideoHeight = mVideoPlayerPresenter.getVideoHeight();
            transformVideo(mVideoWidth, mVideoHeight);
        }
    }

    private void initVideoView(Context context, AttributeSet attrs) {
        setKeepScreenOn(true);
        mVideoWidth = 0;
        mVideoHeight = 0;
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PlayerView);
            boolean realTime = a.getBoolean(R.styleable.PlayerView_real_time, false);
            a.recycle();
            mVideoPlayerPresenter = new VideoPlayerPresenter(mVideoWidth, mVideoHeight, realTime);
        } else {
            mVideoPlayerPresenter = new VideoPlayerPresenter(mVideoWidth, mVideoHeight, false);
        }
        mVideoPlayerPresenter.setView(this);
        // 设置SurfaceTexture listener
        setSurfaceTextureListener(this);

//        mCurrentState = STATE_IDLE;
        if (context instanceof Activity) {
            ((Activity) context).setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }
    }

    @Override
    public IPlayerPresenter getPlayerPresenter() {
        return mVideoPlayerPresenter;
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
                                          int height) {
        MyLog.d(TAG, "Meg1234 surfaceCreated");
        mVideoPlayerPresenter.onSurfaceTextureAvailable(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        MyLog.d(TAG, "onSurfaceTextureSizeChanged");
        mVideoPlayerPresenter.onSurfaceTextureChanged(surface);
        invalidate();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        MyLog.d(TAG, "surfaceDestroyed");

        mVideoPlayerPresenter.onSurfaceDestroyed();
        mVideoWidth = 0;
        mVideoHeight = 0;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void setVideoLayout(boolean isLandscape) {
        mVideoHeight = mVideoPlayerPresenter.getVideoHeight();
        mVideoWidth = mVideoPlayerPresenter.getVideoWidth();
        MyLog.w(TAG, "setVideoLayout mVideoWidth:" + mVideoWidth + " mVideoHeight:" + mVideoHeight + " mType:" + mType);
        if (mVideoHeight > 0 && mVideoWidth > 0) {
            if (mType == FEEDSCONTENT_TYPE_LIVE || mType == FEEDSCONTENT_TYPE_REPLAY) {
                if (mVideoWidth > mVideoHeight) {
                    //如果修改请注意布局为RelativeLayout
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
                    float videoRatio = ((float) (mVideoWidth)) / mVideoHeight;
                    MyLog.d(TAG, "videoRatio = " + videoRatio);
                    lp.height = (int) (DisplayUtils.getPhoneHeight() / videoRatio);
                    lp.width = DisplayUtils.getPhoneHeight();
                    lp.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
                    setLayoutParams(lp);
                } else {
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
                    float videoRatio = ((float) (mVideoWidth)) / mVideoHeight;
                    MyLog.d(TAG, "videoRatio = " + videoRatio);
                    lp.height = DisplayUtils.getPhoneWidth();
                    lp.width = (int) (DisplayUtils.getPhoneWidth() * videoRatio);
                    lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                    setLayoutParams(lp);
                }
            } else if (mType == FEEDSCONTENT_TYPE_VIDEO) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
                lp.height = DisplayUtils.getPhoneWidth();
                lp.width = DisplayUtils.getPhoneWidth();
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                setLayoutParams(lp);
            } else if (mVideoPlayerPresenter.getPlayerType() == IPlayerPresenter.TYPE_EXO_PLAYER) {
                if (mVideoTransMode == TRANS_MODE_CENTER_CROP) {
                    updateTextureViewSize();
                }
//                if (mVideoWidth > mVideoHeight) {
//                    //如果修改请注意布局为RelativeLayout
//                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
//                    float videoRatio = ((float) (mVideoWidth)) / mVideoHeight;
//                    MyLog.d(TAG, "videoRatio = " + videoRatio);
//                    lp.height = (int) (DisplayUtils.getPhoneWidth() / videoRatio);
//                    lp.width = DisplayUtils.getPhoneWidth();
//                    lp.topMargin = DisplayUtils.getPhoneHeight() / 4;
//                    setLayoutParams(lp);
//                } else {
//                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
//                    lp.topMargin = 0;
//                    float videoRatio = ((float) (mVideoHeight)) / mVideoWidth;
//                    // 高比宽大，要看大多少
//                    if (videoRatio > 1.78f) {
//                        // 大很多，还是以高为基准
//                        lp.height = DisplayUtils.getPhoneHeight();
//                        lp.width = (int) (DisplayUtils.getPhoneHeight() / videoRatio);
//                    } else {
//                        lp.width = DisplayUtils.getPhoneWidth();
//                        lp.height = (int) (DisplayUtils.getPhoneWidth() * videoRatio);
//                    }
//                    MyLog.d(TAG, "videoRatio = " + videoRatio);
//                    lp.addRule(RelativeLayout.CENTER_IN_PARENT);
//                    setLayoutParams(lp);
//                }
            }
        }
    }

    @Override
    public void adjustVideoLayout(boolean isLandscape) {
        int lastWidth = mVideoWidth;
        int lastHeight = mVideoHeight;
        mVideoWidth = mVideoPlayerPresenter.getVideoWidth();
        mVideoHeight = mVideoPlayerPresenter.getVideoHeight();

        MyLog.w(TAG + " adjustVideoLayout lastWidth " + lastWidth + " lastHeight " + lastHeight);
        MyLog.w(TAG + " adjustVideoLayout " + mVideoWidth + " " + mVideoHeight + " " + isLandscape);
        MyLog.d(TAG, "adjustVideoLayout" + " isLandscape=" + isLandscape);

        //第一次layout时候 presenter还没有准备好  得到的mVideoWidth mVideoHeight为0 这里加一个保护 需要重新trans一下
        if ((lastWidth != 0 && lastHeight != 0) && (mVideoWidth != 0 && mVideoHeight != 0)) {
            if (lastWidth != mVideoWidth || lastHeight != mVideoHeight) {     //尺寸发生变化
                transformVideo(mVideoWidth, mVideoHeight);
            }
        }
        float frameRatio = 1f * mVideoWidth / mVideoHeight;
        if (mVideoHeight > 0 && mVideoWidth > 0 && mType == -1 && mVideoWidth > mVideoHeight) {
            int curMargin = (DisplayUtils.getPhoneHeight() - DisplayUtils.getPhoneWidth() * mVideoHeight / mVideoWidth) / 2;
            if (frameRatio > 1.33f && frameRatio < 2.2f) {
                int targetMargin = DisplayUtils.dip2px(140);
                float distance = curMargin - targetMargin;
                if (!isLandscape) {
                    mVideoPlayerPresenter.shiftUp(distance * 2 / DisplayUtils.getPhoneHeight());
                    mShiftUpRatio = distance * 2 / DisplayUtils.getPhoneHeight();
                }
                mRotateBtnBottomMargin = curMargin + (int) distance + DisplayUtils.dip2px(3.33f);
            } else {
                if (!isLandscape) {
                    mShiftUpRatio = 0;
                }
                mRotateBtnBottomMargin = curMargin + DisplayUtils.dip2px(3.33f);
            }
        }
    }

    @Override
    public void onSetVideoURICompleted() {
        requestLayout();
        invalidate();
    }

    @Override
    public int getRotateBtnBottomMargin() {
        return mRotateBtnBottomMargin;
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onConfigurationChanged() {

    }

    @Override
    public SurfaceHolder getSurfaceHolder() {
        return null;
    }

    @Override
    public void setVideoTransMode(int mode) {
        mVideoTransMode = mode;
        transformVideo(mVideoPlayerPresenter.getVideoWidth(), mVideoPlayerPresenter.getVideoHeight());
    }

    private void transformVideo(int videoWidth, int videoHeight) {
        final int layoutW = getWidth();
        final int layoutH = getHeight();
        if (layoutW == 0 || layoutH == 0) {
            return;
        }
        MyLog.w(TAG + " transformVideo videoWidth=" + videoWidth + " videoHeight=" + videoHeight + " transformVideo layoutW=" + layoutW + " layoutH=" + layoutH + " mVideoTransMode=" + mVideoTransMode);
        MyLog.w(TAG + " transformVideo offsetX = " + mOffset_X + " offsetY = " + mOffset_Y);

        if (mType == FEEDSCONTENT_TYPE_REPLAY) {
            updateReplayGravity(layoutW, layoutH);
        } else {
            if (mVideoTransMode == TRANS_MODE_CENTER_CROP) {
                mVideoPlayerPresenter.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFill, layoutW, layoutH);
            } else if (mVideoTransMode == TRANS_MODE_CENTER_INSIDE) {
                mVideoPlayerPresenter.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFit, layoutW, layoutH);
            }
        }
    }

    /*
    * 针对全面屏回放有黑边的问题
    * */
    private void updateReplayGravity(int layoutWidth, int layoutHeight) {
        if (CommonUtils.isNeedFill(layoutWidth, layoutHeight, mVideoWidth, mVideoHeight)) {
            mVideoPlayerPresenter.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFill, layoutWidth, layoutHeight);
        } else {
            mVideoPlayerPresenter.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFit, layoutWidth, layoutHeight);
        }
    }

    public float getShiftUpRatio() {
        return mShiftUpRatio;
    }

    public int getShiftUpMargin() {
        return (int) (mShiftUpRatio * DisplayUtils.getPhoneHeight() / 2);
    }

    public void rotateVideo(int angle) {
        mVideoPlayerPresenter.setRotateDegree(angle);
    }

    public void setYOffset(int offset) {
        mOffset_Y = offset;
    }

    public VideoPlayerPresenter getVideoPlayerPresenter() {
        return mVideoPlayerPresenter;
    }

    public void setType(long type) {
        mType = type;
    }


    /**
     * EXOPLAY模式下支持CENTER_CROP
     */
    private void updateTextureViewSize() {
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            float sx = (float) getWidth() / mVideoWidth;
            float sy = (float) getHeight() / mVideoHeight;

            Matrix matrix = new Matrix();
            float maxScale = Math.max(sx, sy);

            //第1步:把视频区移动到View区,使两者中心点重合.
            matrix.preTranslate((getWidth() - mVideoWidth) / 2, (getHeight() - mVideoHeight) / 2);

            //第2步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
            matrix.preScale(mVideoWidth / (float) getWidth(), mVideoHeight / (float) getHeight());

            //第3步,等比例放大或缩小,直到视频区的一边超过View一边, 另一边与View的另一边相等. 因为超过的部分超出了View的范围,所以是不会显示的,相当于裁剪了.
            matrix.postScale(maxScale, maxScale, getWidth() / 2, getHeight() / 2);//后两个参数坐标是以整个View的坐标系以参考的

            setTransform(matrix);
            MyLog.w(TAG, "updateTextureViewSize sx=" + sx + ",sy=" + sy);
        }
    }

}
