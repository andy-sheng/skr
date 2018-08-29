package com.wali.live.watchsdk.component.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.base.dialog.MyAlertDialog;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.CommonUtils;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.videodetail.data.PullStreamerPresenter;
import com.xiaomi.player.Player;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

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

    protected int mVideoWidth;
    protected int mVideoHeight;
    protected int mSurfaceWidth;
    protected int mSurfaceHeight;
    private Surface mSurface;

    private boolean mIsLandscape = false;

    private WeakReference<MyAlertDialog> mTrafficDialogRef;
    private WeakReference<MyAlertDialog> mNetworkDialogRef;

    protected static <T> T deRef(Reference<T> reference) {
        return reference != null ? reference.get() : null;
    }

    protected abstract Context getContext();

    protected abstract void doStartPlay();

    public BasePlayerPresenter(IEventController controller) {
        super(controller);
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
//            mStreamerPresenter.setGravity(mView, Player.SurfaceGravity.SurfaceGravityResizeAspectFit,
//                    mSurfaceWidth, mSurfaceHeight);
            updateGravity();
            updateShiftUp();
        }
    }

    private void updateGravity() {
        if (mStreamerPresenter != null && mSurfaceWidth != 0 && mSurfaceHeight != 0) {
            if (CommonUtils.isNeedFill(mSurfaceWidth, mSurfaceHeight, mVideoWidth, mVideoHeight) && canFill()) {
                mStreamerPresenter.setGravity(mView, Player.SurfaceGravity.SurfaceGravityResizeAspectFill, mSurfaceWidth, mSurfaceHeight);
            } else {
                mStreamerPresenter.setGravity(mView, Player.SurfaceGravity.SurfaceGravityResizeAspectFit, mSurfaceWidth, mSurfaceHeight);
            }
        } else {
            if (mStreamerPresenter != null) {
                mStreamerPresenter.setGravity(mView, Player.SurfaceGravity.SurfaceGravityResizeAspectFit,
                        mSurfaceWidth, mSurfaceHeight);
            }
        }
    }

    protected boolean canFill(){
        return true;
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
            if (mView instanceof View) {
                View view = (View) mView;
                View parent = (View) view.getParent();
                if (parent.getHeight() != 0) {
                    float ratio = (parent.getHeight() - parent.getWidth() * 9 / 16) * 0.25f / parent.getHeight();
                    mStreamerPresenter.shiftUp(ratio);
                }
            } else {
                float ratio = (mSurfaceHeight - mSurfaceWidth * 9 / 16) * 0.25f / mSurfaceHeight;
                mStreamerPresenter.shiftUp(ratio);
            }
        } else {
            mStreamerPresenter.shiftUp(0);
        }
    }

    protected void notifyVideoDirection() {
        // nothing to do
    }

    protected final void onVideoSizeChange(int width, int height) {
        if (mVideoWidth != width || mVideoHeight != height) {
            mVideoWidth = width;
            mVideoHeight = height;
            updateGravity();
            updateShiftUp();
            notifyVideoDirection();
        }
    }

    protected final void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        mIsLandscape = isLandscape;
        updateShiftUp();
    }

    protected final void showNetworkDialog() {
        MyAlertDialog networkDialog = deRef(mNetworkDialogRef);
        if (networkDialog == null) {
            final Context context = getContext();
            networkDialog = new MyAlertDialog.Builder(context)
                    .setMessage(R.string.live_offline_no_network)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (android.os.Build.VERSION.SDK_INT > 10) {
                                context.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                            } else {
                                context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setCancelable(false).create();
            mNetworkDialogRef = new WeakReference<>(networkDialog);
        }
        if (!networkDialog.isShowing()) {
            networkDialog.show();
        }
    }

    static final String LAST_AGREE_TRAFFIC_TS = "last_agree_traffic_ts";

    protected final boolean needShowTrafficDialog() {
        if (System.currentTimeMillis() - PreferenceUtils.getSettingLong(LAST_AGREE_TRAFFIC_TS, 0) < 60 * 1000 * 60) {
            return false;
        }
        return true;
    }

    protected final void showTrafficDialog() {

        MyAlertDialog trafficDialog = deRef(mTrafficDialogRef);
        if (trafficDialog == null) {
            final Context context = getContext();
            trafficDialog = new MyAlertDialog.Builder(context)
                    .setMessage(R.string.live_traffic_tip)
                    .setPositiveButton(R.string.live_traffic_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PreferenceUtils.setSettingLong(LAST_AGREE_TRAFFIC_TS, System.currentTimeMillis());
                            doStartPlay();
                            dialog.dismiss();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setCancelable(false).create();
            mTrafficDialogRef = new WeakReference<>(trafficDialog);
        }
        if (!trafficDialog.isShowing()) {
            trafficDialog.show();
        }
    }
}
