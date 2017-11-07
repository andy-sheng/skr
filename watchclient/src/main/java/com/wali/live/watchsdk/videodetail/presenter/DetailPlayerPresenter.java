package com.wali.live.watchsdk.videodetail.presenter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.network.NetworkUtils;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.event.EventClass;
import com.wali.live.receiver.NetworkReceiver;
import com.wali.live.watchsdk.component.presenter.BasePlayerPresenter;
import com.wali.live.watchsdk.videodetail.data.PullStreamerPresenter;
import com.wali.live.watchsdk.videodetail.view.DetailPlayerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
public class DetailPlayerPresenter extends BasePlayerPresenter<DetailPlayerView.IView, PullStreamerPresenter>
        implements DetailPlayerView.IPresenter {
    private static final String TAG = "DetailPlayerPresenter";

    private boolean mIsDetailMode = true;
    private boolean mHasNetwork = true;
    private boolean mNeedShowTraffic = false;
    private long mSavedPosition;

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    protected final Context getContext() {
        return mView.getRealView().getContext();
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

    @Override
    protected final void doStartPlay() {
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
            doStartPlay();
        }
    }

    @Override
    public final void pausePlay() {
        mStreamerPresenter.pauseWatch();
        mView.onPlayPaused();
    }

    protected void stopPlay() {
        mStreamerPresenter.stopWatch();
        mView.onPlayPaused();
    }

    @Override
    public final void seekTo(float progress) {
        mSavedPosition = (long) (progress * 1000);
        if (!mStreamerPresenter.isStarted() && mNeedShowTraffic) {
            showTrafficDialog();
        } else {
            doStartPlay();
        }
    }

    @Override
    public final void switchToFullScreen() {
        postEvent(MSG_SWITCH_TO_REPLAY_MODE);
    }

    @Override
    protected void updateShiftUp() {
        if (mIsDetailMode) {
            mStreamerPresenter.shiftUp(0);
        } else {
            super.updateShiftUp();
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
