package com.wali.live.watchsdk.videodetail.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.view.Surface;

import com.base.dialog.MyAlertDialog;
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
import com.wali.live.watchsdk.videodetail.view.DetailPlayerView;
import com.xiaomi.player.Player;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

import static com.wali.live.component.BaseSdkController.MSG_BACKGROUND_CLICK;
import static com.wali.live.component.BaseSdkController.MSG_NEW_FEED_URL;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_COMPLETED;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_ERROR;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_HIDE_LOADING;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_PAUSE;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_READY;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_SHOW_LOADING;
import static com.wali.live.component.BaseSdkController.MSG_SEEK_COMPLETED;
import static com.wali.live.component.BaseSdkController.MSG_SWITCH_TO_REPLAY_MODE;
import static com.wali.live.component.BaseSdkController.MSG_UPDATE_PLAY_PROGRESS;
import static com.wali.live.component.BaseSdkController.MSG_VIDEO_SIZE_CHANGED;

/**
 * Created by yangli on 2017/09/25.
 *
 * @module 详情-播放控制表现
 */
public class DetailPlayerPresenter extends ComponentPresenter<DetailPlayerView.IView>
        implements DetailPlayerView.IPresenter {
    private static final String TAG = "DetailPlayerPresenter";

    private PullStreamerPresenter mStreamerPresenter;

    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private Surface mSurface;

    private boolean mIsLandscape = false;
    private boolean mIsDetailMode = true;
    private boolean mHasNetwork = true;
    private boolean mNeedShowTraffic = false;
    private long mSavedPosition;

    private WeakReference<MyAlertDialog> mTrafficDialogRef;
    private WeakReference<MyAlertDialog> mNetworkDialogRef;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public final void setIsDetailMode(boolean isDetailMode) {
        mIsDetailMode = isDetailMode;
    }

    public DetailPlayerPresenter(
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
        registerAction(MSG_BACKGROUND_CLICK);
        registerAction(MSG_NEW_FEED_URL);
        registerAction(MSG_PLAYER_PAUSE);
        registerAction(MSG_UPDATE_PLAY_PROGRESS);
        registerAction(MSG_PLAYER_SHOW_LOADING);
        registerAction(MSG_PLAYER_HIDE_LOADING);
        registerAction(MSG_PLAYER_READY);
        registerAction(MSG_PLAYER_ERROR);
        registerAction(MSG_SEEK_COMPLETED);
        registerAction(MSG_PLAYER_COMPLETED);
        registerAction(MSG_VIDEO_SIZE_CHANGED);
        mHasNetwork = NetworkUtils.hasNetwork(GlobalData.app());
        mNeedShowTraffic = mHasNetwork && !NetworkUtils.isWifi(GlobalData.app());
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

    protected final void doResumePlay() {
        if (mStreamerPresenter.isStarted()) {
            mStreamerPresenter.resumeWatch();
        } else {
            mStreamerPresenter.startWatch();
        }
        if (mSavedPosition > 0) {
            mStreamerPresenter.seekTo(mSavedPosition);
            mSavedPosition = 0;
        }
        mView.onPlayResumed();
    }

    @Override
    public final void resumePlay() {
        if (!mHasNetwork && mStreamerPresenter.isLocalVideo()) {
            showNetworkDialog();
        }
        if (!mStreamerPresenter.isStarted() && mNeedShowTraffic) {
            showTrafficDialog();
        } else {
            doResumePlay();
        }
    }

    @Override
    public final void pausePlay() {
        mStreamerPresenter.pauseWatch();
        mView.onPlayPaused();
    }

    private void stopPlay() {
        mStreamerPresenter.stopWatch();
        mView.onPlayPaused();
    }

    @Override
    public final void seekTo(float progress) {
        mSavedPosition = (long) (progress * 1000);
        if (!mStreamerPresenter.isStarted() && mNeedShowTraffic) {
            showTrafficDialog();
        } else {
            doResumePlay();
        }
    }

    @Override
    public final void switchToFullScreen() {
        postEvent(MSG_SWITCH_TO_REPLAY_MODE);
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
        if (mIsDetailMode || mIsLandscape) {
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
            trafficDialog = new MyAlertDialog.Builder(mView.getRealView().getContext())
                    .setMessage(R.string.live_traffic_tip)
                    .setPositiveButton(R.string.live_traffic_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doResumePlay();
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

    private void showNetworkDialog() {
        MyAlertDialog networkDialog = mNetworkDialogRef != null ? mNetworkDialogRef.get() : null;
        if (networkDialog == null) {
            networkDialog = new MyAlertDialog.Builder(mView.getRealView().getContext())
                    .setMessage(R.string.live_offline_no_network)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Context context = mView.getRealView().getContext();
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
                mSavedPosition = mStreamerPresenter.getCurrentPosition();
                if (!mStreamerPresenter.isPaused()) {
                    showTrafficDialog();
                }
                stopPlay();
            }
        } else {
            mHasNetwork = false;
            mNeedShowTraffic = false;
            if (mStreamerPresenter.isLocalVideo()) {
                showNetworkDialog();
            }
        }
    }

    private void onNewFeedUrl(String videoUrl) {
        stopPlay();
        mView.reset();
        mSavedPosition = 0;
        mStreamerPresenter.setOriginalStreamUrl(videoUrl);
        if (!mHasNetwork && !mStreamerPresenter.isLocalVideo()) {
            showNetworkDialog();
        }
        resumePlay();
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
            case MSG_BACKGROUND_CLICK:
                mView.onChangeVisibility();
                return true;
            case MSG_NEW_FEED_URL:
                onNewFeedUrl((String) params.getItem(0));
                return true;
            case MSG_PLAYER_PAUSE:
                pausePlay();
                return true;
            case MSG_UPDATE_PLAY_PROGRESS:
                mView.onUpdateProgress((int) (mStreamerPresenter.getCurrentPosition() / 1000));
                return true;
            case MSG_PLAYER_HIDE_LOADING:
            case MSG_SEEK_COMPLETED: // fall through
                mView.showLoading(false);
                return true;
            case MSG_PLAYER_SHOW_LOADING:
                mView.showLoading(true);
                return true;
            case MSG_PLAYER_READY:
                mView.onUpdateDuration((int) (mStreamerPresenter.getDuration() / 1000));
                return true;
            case MSG_PLAYER_ERROR:
            case MSG_PLAYER_COMPLETED: // fall through
                mView.reset();
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
