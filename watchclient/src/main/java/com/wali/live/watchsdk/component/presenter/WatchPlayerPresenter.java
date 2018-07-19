package com.wali.live.watchsdk.component.presenter;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.view.TextureView;

import com.base.event.SdkEventClass;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.SelfUpdateManager;
import com.base.utils.network.NetworkUtils;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.event.EventClass;
import com.wali.live.receiver.NetworkReceiver;
import com.wali.live.watchsdk.videodetail.data.PullStreamerPresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.wali.live.component.BaseSdkController.MSG_NEW_VIDEO_URL;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_COMPLETED;
import static com.wali.live.component.BaseSdkController.MSG_VIDEO_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_VIDEO_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_VIDEO_SIZE_CHANGED;

/**
 * Created by yangli on 2017/10/09.
 *
 * @module 观看直播-播放控制表现
 */
public class WatchPlayerPresenter extends BasePlayerPresenter<TextureView, PullStreamerPresenter> {
    private static final String TAG = "WatchPlayerPresenter";

    private int mForcePauseFlag = 0;
    private boolean mNewVideoDuringForcePause = false; // 强制暂停时有新的播放请求(系统通话 或 熄屏)

    private boolean mHasNetwork = true;
    private boolean mNeedShowTraffic = false;

    @Override
    protected final String getTAG() {
        return TAG;
    }

    @Override
    protected final Context getContext() {
        return mView.getContext();
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
        abandonAudioFocus();
    }

    @Override
    protected final void doStartPlay() {
        requestAudioFocus();
        if (mStreamerPresenter.isStarted()) {
            mStreamerPresenter.resumeWatch();
        } else {
            mStreamerPresenter.startWatch();
        }

        // 既然已经允许播放了，说明用户不在乎流量了，开始尝试下载apk包
        SelfUpdateManager.tryDownloadNewestApk();
    }

    private boolean requestAudioFocus() {
        // 关闭其他音乐
        AudioManager mAudioManager = (AudioManager) GlobalData.app().getSystemService(Context.AUDIO_SERVICE);
        MyLog.w(TAG, "stopOtherMusic");
        int result = mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            MyLog.w(TAG, "AudioManager result = " + result);
            return false;
        }
        MyLog.w(TAG, "stopOtherMusic over");
        return true;
    }

    private boolean abandonAudioFocus() {
        // 关闭其他音乐
        AudioManager mAudioManager = (AudioManager) GlobalData.app().getSystemService(Context.AUDIO_SERVICE);
        MyLog.w(TAG, "stopOtherMusic");
        int result = mAudioManager.abandonAudioFocus(null);
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            MyLog.w(TAG, "AudioManager result = " + result);
            return false;
        }
        MyLog.w(TAG, "stopOtherMusic over");
        return true;
    }

    private void startPlay() {
        if (!mHasNetwork && mStreamerPresenter.isLocalVideo()) {
            showNetworkDialog();
        }
        if (!mStreamerPresenter.isStarted() && mNeedShowTraffic) {
            showTrafficDialog();
        } else {
            doStartPlay();
        }
    }

    private void resumePlay() {
        if (mStreamerPresenter.isStarted() && mStreamerPresenter.isPaused()) {
            mStreamerPresenter.resumeWatch();
        }
    }

    private void pausePlay() {
        if (mStreamerPresenter.isStarted() && !mStreamerPresenter.isPaused()) {
            mStreamerPresenter.pauseWatch();
        }
    }

    private void stopPlay() {
        mNewVideoDuringForcePause = false;
        mStreamerPresenter.stopWatch();
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

    private void updateForcePauseFlag(int newFlag) {
        if (mForcePauseFlag != 0 && newFlag == 0) {
            if (mNewVideoDuringForcePause) {
                mNewVideoDuringForcePause = false;
                startPlay();
            } else {
                resumePlay();
            }
        } else if (mForcePauseFlag == 0 && newFlag != 0) {
            pausePlay();
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
                removeForcePauseFlag(FLAG_PHONE_STATE);
                break;
            case EventClass.PhoneStateEvent.TYPE_PHONE_STATE_RING:
            case EventClass.PhoneStateEvent.TYPE_PHONE_STATE_OFFHOOK: // fall through
                addForcePauseFlag(FLAG_PHONE_STATE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void notifyVideoDirection() {
        if (mVideoWidth == 0 || mVideoHeight == 0) {
            return;
        }
        if (mVideoWidth > mVideoHeight) {
            mController.postEvent(MSG_VIDEO_LANDSCAPE);
        } else {
            mController.postEvent(MSG_VIDEO_PORTRAIT);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SdkEventClass.ScreenStateEvent event) {
        switch (event.screenState) {
            case SdkEventClass.ScreenStateEvent.ACTION_SCREEN_ON:
                removeForcePauseFlag(FLAG_SCREEN_STATE);
                break;
            case SdkEventClass.ScreenStateEvent.ACTION_SCREEN_OFF:
                addForcePauseFlag(FLAG_SCREEN_STATE);
                break;
            default:
                break;
        }
    }

    private void onNewVideoUrl(String videoUrl) {
        stopPlay();
        mStreamerPresenter.setOriginalStreamUrl(videoUrl);
        if (!mHasNetwork && !mStreamerPresenter.isLocalVideo()) {
            showNetworkDialog();
        }
        if (mForcePauseFlag != 0) {
            mNewVideoDuringForcePause = true;
        } else {
            startPlay();
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
                stopPlay();
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
