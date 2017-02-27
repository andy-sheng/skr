package com.mi.live.engine.player.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.xiaomi.player.Player;

/**
 * Created by lan on 16-1-4.
 * VideoPlayerTextureView，继承textureView，功能和VideoPlayerView相同
 * </p>
 * 记得同步修改VideoPlayerView，以后统一
 */
public class VideoPlayerTextureView extends TextureView implements SurfaceTextureListener, IVideoView, IPlayerTextureView {
    private static final String TAG = VideoPlayerTextureView.class.getSimpleName();
    private int mVideoWidth, mVideoHeight;
    private float mShiftUpRatio;

    public static final int TRANS_MODE_CENTER_CROP = 0;
    public static final int TRANS_MODE_CENTER_INSIDE = 1;
    private int mVideoTransMode = TRANS_MODE_CENTER_CROP;

    private VideoPlayerPresenter mVideoPlayerPresenter;

    public VideoPlayerTextureView(Context context) {
        super(context);
        init(context);
    }

    public VideoPlayerTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoPlayerTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
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

    private void init(Context context) {
        mVideoWidth = 0;
        mVideoHeight = 0;
        mVideoPlayerPresenter = new VideoPlayerPresenter(mVideoWidth, mVideoHeight);
        mVideoPlayerPresenter.setView(this);
        // 设置SurfaceTexture listener
        setSurfaceTextureListener(this);

//        mCurrentState = STATE_IDLE;
        if (context instanceof Activity) {
            ((Activity) context).setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }
    }

    @Override
    public VideoPlayerPresenter getVideoPlayerPresenter() {
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
    public void setVideoLayout() {
    }

    @Override
    public void adjustVideoLayout(boolean isLandscape) {
        int lastWidth = mVideoWidth;
        int lastHeight = mVideoHeight;
        mVideoWidth = mVideoPlayerPresenter.getVideoWidth();
        mVideoHeight = mVideoPlayerPresenter.getVideoHeight();

        MyLog.w(TAG + " adjustVideoLayout lastWidth " + lastWidth + " lastHeight " + lastHeight);
        MyLog.w(TAG + " adjustVideoLayout " + mVideoWidth + " " + mVideoHeight + " " + isLandscape);

        //第一次layout时候 presenter还没有准备好  得到的mVideoWidth mVideoHeight为0 这里加一个保护 需要重新trans一下
        if ((lastWidth != 0 && lastHeight != 0) && (mVideoWidth != 0 && mVideoHeight != 0)) {
            if (lastWidth != mVideoWidth || lastHeight != mVideoHeight) {     //尺寸发生变化
                transformVideo(mVideoWidth, mVideoHeight);
            }
        }
        float frameRatio = 1f * mVideoWidth / mVideoHeight;
        if (mVideoHeight > 0 && mVideoWidth > 0 && !isLandscape && frameRatio > 1.33f && frameRatio < 1.78f) {
            int curMargin = (GlobalData.screenHeight - GlobalData.screenWidth * mVideoHeight / mVideoWidth) / 2;
            int targetMargin = DisplayUtils.dip2px(140);
            float distance = curMargin - targetMargin;
            mShiftUpRatio = distance * 2 / GlobalData.screenHeight;
        } else {
            mShiftUpRatio = 0;
        }
    }

    @Override
    public void onSetVideoURICompleted() {
        requestLayout();
        invalidate();
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
        MyLog.v(TAG + " transformVideo videoWidth=" + videoWidth + " videoHeight=" + videoHeight + " transformVideo layoutW=" + layoutW + " layoutH=" + layoutH + " mVideoTransMode=" + mVideoTransMode);
        if (mVideoTransMode == TRANS_MODE_CENTER_CROP) {
            mVideoPlayerPresenter.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFill, layoutW, layoutH);
        } else if (mVideoTransMode == TRANS_MODE_CENTER_INSIDE) {
            mVideoPlayerPresenter.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFit, layoutW, layoutH);
        }
    }

    public float getShiftUpRatio() {
        return mShiftUpRatio;
    }

    public int getShiftUpMargin() {
        return (int) (mShiftUpRatio * GlobalData.screenHeight / 2);
    }

}
