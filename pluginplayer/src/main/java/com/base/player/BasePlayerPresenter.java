package com.base.player;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;

import com.base.log.MyLog;
import com.base.player.component.ComponentPresenter;
import com.base.player.data.PullStreamerPresenter;
import com.xiaomi.player.Player;

import java.lang.ref.Reference;

/**
 * Created by yangli on 2017/11/7.
 *
 * @module 播放器表现基类
 */
public abstract class BasePlayerPresenter<VIEW, STREAMER extends PullStreamerPresenter>
        extends ComponentPresenter<VIEW> implements TextureView.SurfaceTextureListener {

    protected static final int FLAG_PHONE_STATE = 0x1 << 0;
    protected static final int FLAG_SCREEN_STATE = 0x1 << 1;

    protected STREAMER mStreamerPresenter;

    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private Surface mSurface;

    private boolean mIsLandscape = false;

    protected static <T> T deRef(Reference<T> reference) {
        return reference != null ? reference.get() : null;
    }

    protected abstract Context getContext();

    protected abstract void doStartPlay();

    public BasePlayerPresenter() {
    }

    @Override
    public final void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        MyLog.w(TAG, "onSurfaceTextureAvailable");
        if (mSurface == null) {
            mSurface = new Surface(surface);
        }
        onSurfaceTextureSizeChanged(surface, width, height);
    }

    @Override
    public final void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        MyLog.w(TAG, "onSurfaceTextureSizeChanged");
        if (mSurfaceWidth != width || mSurfaceHeight != height) {
            MyLog.w(TAG, "onSurfaceTextureSizeChanged width=" + width + ", height=" + height);
            mSurfaceWidth = width;
            mSurfaceHeight = height;
            mStreamerPresenter.setSurface(mSurface);
            mStreamerPresenter.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFit,
                    mSurfaceWidth, mSurfaceHeight);
            updateShiftUp();
        }
    }

    @Override
    public final boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        MyLog.w(TAG, "onSurfaceTextureDestroyed");
        if (mSurface != null) {
            mSurfaceWidth = mSurfaceHeight = 0;
            mSurface.release();
            mSurface = null;
        }
        return true;
    }

    @Override
    public final void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    protected void updateShiftUp() {
        if (mIsLandscape) {
            mStreamerPresenter.shiftUp(0);
            return;
        }
        if (mSurfaceWidth == 0 || mSurfaceHeight == 0) {
            mStreamerPresenter.shiftUp(0);
        } else if (mVideoWidth == 0 || mVideoHeight == 0) {
            mStreamerPresenter.shiftUp(0);
        } else if (mVideoWidth * 16 > mVideoHeight * 9) {
            float ratio = (mSurfaceHeight - mSurfaceWidth * 9 / 16) * 0.25f / mSurfaceHeight;
            mStreamerPresenter.shiftUp(ratio);
        } else {
            mStreamerPresenter.shiftUp(0);
        }
    }

    protected final void onVideoSizeChange(int width, int height) {
        if (mVideoWidth != width || mVideoHeight != height) {
            mVideoWidth = width;
            mVideoHeight = height;
            updateShiftUp();
        }
    }

    protected final void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        mIsLandscape = isLandscape;
        updateShiftUp();
    }
}
