package com.wali.live.watchsdk.component.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.view.Surface;
import android.view.TextureView;

import com.base.dialog.MyAlertDialog;
import com.base.event.SdkEventClass;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.network.NetworkUtils;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.event.EventClass;
import com.wali.live.receiver.NetworkReceiver;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.videodetail.data.PullStreamerPresenter;
import com.xiaomi.player.Player;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

import static com.wali.live.component.BaseSdkController.MSG_NEW_VIDEO_URL;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_COMPLETED;
import static com.wali.live.component.BaseSdkController.MSG_VIDEO_SIZE_CHANGED;

/**
 * Created by yangli on 2017/10/09.
 *
 * @module 观看直播-播放控制表现
 */
public class WatchPlayerPresenter extends ComponentPresenter<TextureView>
        implements TextureView.SurfaceTextureListener {
    private static final String TAG = "WatchPlayerPresenter";

    private static final int FLAG_PHONE_STATE = 0x1 << 0;
    private static final int FLAG_SCREEN_STATE = 0x1 << 1;

    private int mForcePauseFlag = 0;
    private boolean mNewVideoDuringForcePause = false; // 强制暂停时有新的播放请求(系统通话 或 熄屏)

    private PullStreamerPresenter mStreamerPresenter;

    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private Surface mSurface;

    private boolean mIsLandscape = false;
    private boolean mHasNetwork = true;
    private boolean mNeedShowTraffic = false;

    private WeakReference<MyAlertDialog> mTrafficDialogRef;
    private WeakReference<MyAlertDialog> mNetworkDialogRef;

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public void setView(TextureView textureView) {
        super.setView(textureView);
        if (mView != null) {
            mView.setSurfaceTextureListener(this);
        }
    }

    public WatchPlayerPresenter(
            @NonNull IEventController controller,
            @NonNull PullStreamerPresenter streamerPresenter) {
        super(controller);
        mStreamerPresenter = streamerPresenter;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_NEW_VIDEO_URL);
        registerAction(MSG_PLAYER_COMPLETED);
        registerAction(MSG_VIDEO_SIZE_CHANGED);
        mHasNetwork = NetworkUtils.hasNetwork(GlobalData.app());
        mNeedShowTraffic = false; // mHasNetwork && !NetworkUtils.isWifi(GlobalData.app());
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    protected final void doStartWatch() {
        if (mStreamerPresenter.isStarted()) {
            mStreamerPresenter.resumeWatch();
        } else {
            mStreamerPresenter.startWatch();
        }
    }

    public final void startWatch() {
        if (!mHasNetwork && mStreamerPresenter.isLocalVideo()) {
            showNetworkDialog();
        }
        if (!mStreamerPresenter.isStarted() && mNeedShowTraffic) {
            showTrafficDialog();
        } else {
            doStartWatch();
        }
    }


    private final void resumeWatch() {
        if (mStreamerPresenter.isStarted() && mStreamerPresenter.isPaused()) {
            mStreamerPresenter.resumeWatch();
        }
    }

    private final void pauseWatch() {
        if (mStreamerPresenter.isStarted() && !mStreamerPresenter.isPaused()) {
            mStreamerPresenter.pauseWatch();
        }
    }

    private final void stopWatch() {
        mNewVideoDuringForcePause = false;
        mStreamerPresenter.stopWatch();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        MyLog.w(TAG, "onSurfaceTextureAvailable");
        if (mSurface == null) {
            mSurface = new Surface(surface);
        }
        onSurfaceTextureSizeChanged(surface, width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        MyLog.w(TAG, "onSurfaceTextureSizeChanged");
        if (mSurfaceWidth != width || mSurfaceHeight != height) {
            MyLog.w(TAG, "onSurfaceTextureSizeChanged width=" + width + ", height=" + width);
            mSurfaceWidth = width;
            mSurfaceHeight = height;
            mStreamerPresenter.setSurface(mSurface);
            mStreamerPresenter.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFit,
                    mSurfaceWidth, mSurfaceHeight);
            updateShiftUp();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        MyLog.w(TAG, "onSurfaceTextureDestroyed");
        if (mSurface != null) {
            mSurfaceWidth = mSurfaceHeight = 0;
            mSurface.release();
            mSurface = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private void updateShiftUp() {
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

    private void onVideoSizeChange(int width, int height) {
        if (mVideoWidth != width || mVideoHeight != height) {
            mVideoWidth = width;
            mVideoHeight = height;
            updateShiftUp();
        }
    }

    private void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        mIsLandscape = isLandscape;
        updateShiftUp();
    }

    private void showTrafficDialog() {
        MyAlertDialog trafficDialog = mTrafficDialogRef != null ? mTrafficDialogRef.get() : null;
        if (trafficDialog == null) {
            trafficDialog = new MyAlertDialog.Builder(mView.getContext())
                    .setMessage(R.string.live_traffic_tip)
                    .setPositiveButton(R.string.live_traffic_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doStartWatch();
                            dialog.dismiss();
                        }
                    }).setNegativeButton(R.string.live_traffic_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((Activity) mView.getContext()).finish();
                            dialog.dismiss();
                        }
                    }).setCancelable(false).create();
            mTrafficDialogRef = new WeakReference<>(trafficDialog);
        }
        if (!trafficDialog.isShowing()) {
            trafficDialog.show();
        }
    }

    private void showNetworkDialog() {
        MyAlertDialog networkDialog = mNetworkDialogRef != null ? mNetworkDialogRef.get() : null;
        if (networkDialog == null) {
            networkDialog = new MyAlertDialog.Builder(mView.getContext())
                    .setMessage(R.string.live_offline_no_network)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Context context = mView.getContext();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.NetWorkChangeEvent event) {
        MyLog.w(TAG, "EventClass.NetWorkChangeEvent");
        if (event == null) {
            return;
        }
        NetworkReceiver.NetState netCode = event.getNetState();
        if (netCode != NetworkReceiver.NetState.NET_NO) { // 优先处理错误情况
            mHasNetwork = true;
            boolean needShowTraffic = netCode == NetworkReceiver.NetState.NET_2G ||
                    netCode == NetworkReceiver.NetState.NET_3G ||
                    netCode == NetworkReceiver.NetState.NET_4G;
            if (mNeedShowTraffic == needShowTraffic) {
                return;
            }
            mNeedShowTraffic = needShowTraffic;
            if (mNeedShowTraffic && mStreamerPresenter.isStarted()) {
                if (!mStreamerPresenter.isPaused()) {
                    showTrafficDialog();
                }
                stopWatch();
            }
        } else {
            mHasNetwork = false;
            mNeedShowTraffic = false;
            if (mStreamerPresenter.isLocalVideo()) {
                showNetworkDialog();
            }
        }
    }

    private void updateForcePauseFlag(int newFlag) {
        if (mForcePauseFlag != 0 && newFlag == 0) {
            if (mNewVideoDuringForcePause) {
                mNewVideoDuringForcePause = false;
                startWatch();
            } else {
                resumeWatch();
            }
        } else if (mForcePauseFlag == 0 && newFlag != 0) {
            pauseWatch();
        }
        mForcePauseFlag = newFlag;
    }

    private void addForcePauseFlag(int flag) {
        updateForcePauseFlag(mForcePauseFlag | flag);
    }

    private void removeForcePauseFlag(int flag) {
        updateForcePauseFlag(mForcePauseFlag & ~flag);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.PhoneStateEvent event) {
        switch (event.type) {
            case EventClass.PhoneStateEvent.TYPE_PHONE_STATE_IDLE:
                addForcePauseFlag(FLAG_PHONE_STATE);
                break;
            case EventClass.PhoneStateEvent.TYPE_PHONE_STATE_RING:
            case EventClass.PhoneStateEvent.TYPE_PHONE_STATE_OFFHOOK: // fall through
                removeForcePauseFlag(FLAG_PHONE_STATE);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SdkEventClass.ScreenStateEvent event) {
        switch (event.screenState) {
            case SdkEventClass.ScreenStateEvent.ACTION_SCREEN_ON:
                addForcePauseFlag(FLAG_SCREEN_STATE);
                break;
            case SdkEventClass.ScreenStateEvent.ACTION_SCREEN_OFF:
                removeForcePauseFlag(FLAG_SCREEN_STATE);
                break;
        }
    }

    private void onNewVideoUrl(String videoUrl) {
        stopWatch();
        mStreamerPresenter.setOriginalStreamUrl(videoUrl);
        if (!mHasNetwork && !mStreamerPresenter.isLocalVideo()) {
            showNetworkDialog();
        }
        if (mForcePauseFlag != 0) {
            mNewVideoDuringForcePause = true;
        } else {
            startWatch();
        }
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                onOrientation(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                onOrientation(true);
                return true;
            case MSG_NEW_VIDEO_URL:
                onNewVideoUrl((String) params.getItem(0));
                return true;
            case MSG_PLAYER_COMPLETED:
                stopWatch();
                return true;
            case MSG_VIDEO_SIZE_CHANGED:
                onVideoSizeChange((int) params.getItem(0), (int) params.getItem(1));
                return true;
            default:
                break;
        }
        return false;
    }
}
