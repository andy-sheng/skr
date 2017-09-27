package com.wali.live.watchsdk.videothird.data;

import android.os.Message;
import android.support.annotation.NonNull;
import android.view.Surface;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.engine.media.player.IMediaPlayer;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.Params;
import com.wali.live.dns.IDnsStatusListener;
import com.wali.live.ipselect.WatchIpSelectionHelper;
import com.wali.live.watchsdk.videothird.data.engine.IPlayer;
import com.wali.live.watchsdk.videothird.data.engine.IPlayerCallback;
import com.xiaomi.player.Player;

import static com.wali.live.component.BaseSdkController.MSG_PLAYER_COMPLETED;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_ERROR;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_HIDE_LOADING;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_READY;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_SHOW_LOADING;
import static com.wali.live.component.BaseSdkController.MSG_SEEK_COMPLETED;
import static com.wali.live.component.BaseSdkController.MSG_UPDATE_PLAY_PROGRESS;
import static com.wali.live.component.BaseSdkController.MSG_VIDEO_SIZE_CHANGED;

/**
 * Created by yangli on 17-5-3.
 *
 * @module 拉流器数据
 */
public class PullStreamerPresenter extends BaseStreamerPresenter<PullStreamerPresenter.ReconnectHelper,
        WatchIpSelectionHelper, IPlayer> {
    private static final String TAG = "PullStreamerPresenter";

    protected static final int _UPDATE_PROGRESS_TIMEOUT = 1000; // 刷新播放进度间隔

    protected static final int _MSG_RECONNECT_STREAM = 1004;  // 推/拉流重连

    protected static final int _MSG_PLAYER_PREPARED = 2000;  // 播放器开始渲染画面
    protected static final int _MSG_PLAYER_COMPLETED = 2001; // 播放完成
    protected static final int _MSG_SEEK_COMPLETED = 2002;   // Seek完成
    protected static final int _MSG_PLAYER_PROGRESS = 2003;  // 进度刷新

    @NonNull
    private IEventController mController;

    protected boolean mStarted = false;
    protected boolean mPaused = true;

    protected boolean mIsRealTime = true;

    private final PlayerCallback<? extends IPlayer> mPlayerCallback = new PlayerCallback<>();

    @Override
    protected String getTAG() {
        return TAG;
    }

    public final boolean isStarted() {
        return mStarted;
    }

    public final long getCurrentPosition() {
        return mStreamer != null ? mStreamer.getCurrentPosition() : 0;
    }

    public final long getDuration() {
        return mStreamer != null ? mStreamer.getDuration() : 0;
    }

    public final PlayerCallback<? extends IPlayer> getPlayerCallback() {
        return mPlayerCallback;
    }

    public PullStreamerPresenter(@NonNull IEventController controller) {
        mController = controller;
        mUIHandler = new MyUIHandler(this);
        mReconnectHelper = new ReconnectHelper();
        mIpSelectionHelper = new WatchIpSelectionHelper(GlobalData.app(), mReconnectHelper, null/*TODO YangLi 加入关键流程打点*/);
    }

    @Override
    public void destroy() {
        if (mStreamer != null) {
            mStreamer.release();
            mStreamer = null;
        }
        mUIHandler.removeCallbacksAndMessages(null);
    }

    public final void setIsRealTime(boolean isRealTime) {
        mIsRealTime = isRealTime;
    }

    public final void setSurface(Surface surface) {
        if (mStreamer != null) {
            mStreamer.setSurface(surface);
        }
    }

    public final void setGravity(Player.SurfaceGravity gravity, int width, int height) {
        if (mStreamer != null) {
            mStreamer.setGravity(gravity, width, height);
        }
    }

    public final void shiftUp(float ratio) {
        if (mStreamer != null) {
            mStreamer.shiftUp(ratio);
        }
    }

    public final void seekTo(long msec) {
        if (mStreamer != null && !mIsRealTime) {
            mIpSelectionHelper.updateStutterStatus(false);
            mStreamer.seekTo(msec);
        }
    }

    public void setOriginalStreamUrl(String originalStreamUrl) {
        mIpSelectionHelper.setOriginalStreamUrl(originalStreamUrl);
    }

    // 拉流开始
    public void startWatch() {
        if (mStreamer == null || mStarted || !mIpSelectionHelper.hasStreamUrl()) {
            return;
        }
        MyLog.w(TAG, "startWatch");
        mStarted = true;
        mPaused = false;
        mReconnectHelper.startStream();
        if (!mIsRealTime) {
            mUIHandler.removeMessages(_MSG_PLAYER_PROGRESS);
            mUIHandler.sendEmptyMessageDelayed(_MSG_PLAYER_PROGRESS, _UPDATE_PROGRESS_TIMEOUT);
        }
    }

    // 恢复播放
    public void resumeWatch() {
        if (mStreamer == null || !mStarted || !mPaused) {
            return;
        }
        MyLog.w(TAG, "resumeWatch");
        mPaused = false;
        mStreamer.start();
        if (!mIsRealTime) {
            mUIHandler.removeMessages(_MSG_PLAYER_PROGRESS);
            mUIHandler.sendEmptyMessageDelayed(_MSG_PLAYER_PROGRESS, _UPDATE_PROGRESS_TIMEOUT);
        }
    }

    // 结束播放
    public void pauseWatch() {
        if (mStreamer == null || !mStarted || mPaused) {
            MyLog.w(TAG, "pauseWatch failed, mStreamer=" + mStreamer);
            return;
        }
        MyLog.w(TAG, "pauseWatch");
        mPaused = true;
        mStreamer.pause();
        if (!mIsRealTime) {
            mUIHandler.removeMessages(_MSG_PLAYER_PROGRESS);
        }
    }

    // 拉流结束
    public void stopWatch() {
        if (mStreamer == null || !mStarted) {
            return;
        }
        MyLog.w(TAG, "stopWatch");
        mStarted = false;
        mPaused = true;
        mReconnectHelper.stopStream();
        mIpSelectionHelper.updateStutterStatus(false);
        if (!mIsRealTime) {
            mUIHandler.removeMessages(_MSG_PLAYER_PROGRESS);
        }
    }

    // 播放器回调
    protected class PlayerCallback<PLAYER extends IPlayer> implements IPlayerCallback<PLAYER> {

        @Override
        public void onPrepared(PLAYER player) {
            mUIHandler.sendEmptyMessage(_MSG_PLAYER_PREPARED);
        }

        @Override
        public void onCompletion(PLAYER player) {
            mUIHandler.sendEmptyMessage(_MSG_PLAYER_COMPLETED);
        }

        @Override
        public void onSeekComplete(PLAYER player) {
            mUIHandler.sendEmptyMessage(_MSG_SEEK_COMPLETED);
        }

        @Override
        public void onVideoSizeChanged(PLAYER player, final int width, final int height) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    MyLog.w(TAG, "onVideoSizeChanged width=" + width + " height=" + height);
                    mController.postEvent(MSG_VIDEO_SIZE_CHANGED, new Params().putItem(width)
                            .putItem(height));
                }
            });
        }

        @Override
        public void onError(PLAYER player, int what, int extra) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    stopWatch();
                    mController.postEvent(MSG_PLAYER_ERROR);
                }
            });
        }

        @Override
        public void onInfo(PLAYER player, final int what, int extra) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    switch (what) {
                        case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                            MyLog.w(TAG, "MEDIA_INFO_BUFFERING_START");
                            mUIHandler.removeMessages(_MSG_RECONNECT_STREAM);
                            mUIHandler.sendEmptyMessageDelayed(_MSG_RECONNECT_STREAM, RECONNECT_TIMEOUT);
                            mController.postEvent(MSG_PLAYER_SHOW_LOADING);
                            break;
                        case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                            MyLog.w(TAG, "MEDIA_INFO_BUFFERING_END");
                            if (mIpSelectionHelper.isStuttering()) {
                                mIpSelectionHelper.updateStutterStatus(false);
                            }
                            mUIHandler.removeMessages(_MSG_RECONNECT_STREAM);
                            mController.postEvent(MSG_PLAYER_HIDE_LOADING);
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }

    protected static class MyUIHandler extends BaseStreamerPresenter.MyUIHandler<PullStreamerPresenter> {

        public MyUIHandler(@NonNull PullStreamerPresenter presenter) {
            super(presenter);
        }

        @Override
        public void handleMessage(Message msg) {
            final PullStreamerPresenter presenter = deRef(mPresenterRef);
            if (presenter == null || !presenter.mStarted) {
                return;
            }
            switch (msg.what) {
                case _MSG_RECONNECT_STREAM:
                    MyLog.w(TAG, "MSG_RECONNECT_STREAM");
                    presenter.mReconnectHelper.startReconnect(0);
                    break;
                case _MSG_PLAYER_PROGRESS: // fall through
                    if (!presenter.mIsRealTime && !presenter.mPaused) {
                        removeMessages(_MSG_PLAYER_PROGRESS);
                        sendEmptyMessageDelayed(_MSG_PLAYER_PROGRESS, _UPDATE_PROGRESS_TIMEOUT);
                        presenter.mController.postEvent(MSG_UPDATE_PLAY_PROGRESS);
                    }
                    break;
                case _MSG_PLAYER_PREPARED:
                    presenter.mController.postEvent(MSG_PLAYER_READY);
                    break;
                case _MSG_PLAYER_COMPLETED:
                    presenter.seekTo(0);
                    presenter.pauseWatch();
                    presenter.mController.postEvent(MSG_PLAYER_COMPLETED);
                    break;
                case _MSG_SEEK_COMPLETED:
                    presenter.mController.postEvent(MSG_SEEK_COMPLETED);
                    break;
                default:
                    break;
            }
        }
    }

    // 域名解析、重连相关
    protected class ReconnectHelper extends BaseStreamerPresenter.ReconnectHelper implements IDnsStatusListener {

        @Override
        public void onDnsReady() {
            MyLog.w(TAG, "onDnsReady");
            if (mIpSelectionHelper.isStuttering()) {
                startReconnect(0);
            }
        }

        @Override
        protected void startStream() {
            MyLog.w(TAG, "startStream");
            mIpSelectionHelper.ipSelect();
            mStreamer.setVideoPath(mIpSelectionHelper.getStreamUrl(), mIpSelectionHelper.getStreamHost());
            mStreamer.setIpList(mIpSelectionHelper.getSelectedHttpIpList(), mIpSelectionHelper.getSelectedLocalIpList());
            mStreamer.prepare(mIsRealTime);
            mStreamer.start();
        }

        @Override
        protected void stopStream() {
            MyLog.w(TAG, "stopStream");
            mStreamer.stop();
        }

        @Override
        protected void startReconnect(int code) {
            if (mStarted && !mPaused) {
                MyLog.w(TAG, "startReconnect, code = " + code);
                if (!mIpSelectionHelper.isStuttering()) {
                    mIpSelectionHelper.updateStutterStatus(true);
                }
                mIpSelectionHelper.ipSelect();
                mStreamer.setVideoPath(mIpSelectionHelper.getStreamUrl(), mIpSelectionHelper.getStreamHost());
                mStreamer.setIpList(mIpSelectionHelper.getSelectedHttpIpList(), mIpSelectionHelper.getSelectedLocalIpList());
                mStreamer.reconnect();
            } else {
                MyLog.w(TAG, "startReconnect is ignored, but is paused or not started");
            }
        }
    }
}
