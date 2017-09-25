package com.wali.live.watchsdk.videothird.data;

import android.os.Message;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.engine.media.player.IMediaPlayer;
import com.thornbirds.component.IEventController;
import com.wali.live.dns.IDnsStatusListener;
import com.wali.live.ipselect.WatchIpSelectionHelper;
import com.wali.live.watchsdk.videothird.data.engine.IPlayer;
import com.wali.live.watchsdk.videothird.data.engine.IPlayerCallback;
import com.xiaomi.player.Player;

import static com.wali.live.component.BaseSdkController.MSG_ON_STREAM_RECONNECT;
import static com.wali.live.component.BaseSdkController.MSG_ON_STREAM_SUCCESS;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_COMPLETED;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_PREPARED;
import static com.wali.live.component.BaseSdkController.MSG_SEEK_COMPLETED;

/**
 * Created by yangli on 17-5-3.
 *
 * @module 拉流器数据
 */
public class PullStreamerPresenter extends BaseStreamerPresenter<PullStreamerPresenter.ReconnectHelper,
        WatchIpSelectionHelper, IPlayer> {
    private static final String TAG = "PullStreamerPresenter";

    @NonNull
    private IEventController mController;

    private boolean mIsRealTime = true;
    private boolean mPaused = true;

    private final PlayerCallback<? extends IPlayer> mPlayerCallback = new PlayerCallback<>();

    @Override
    protected String getTAG() {
        return TAG;
    }

    public boolean isStarted() {
        return mStarted;
    }

    public long getCurrentPosition() {
        return mStreamer != null ? mStreamer.getCurrentPosition() : 0;
    }

    public PlayerCallback<? extends IPlayer> getPlayerCallback() {
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

    public void setIsRealTime(boolean isRealTime) {
        mIsRealTime = isRealTime;
    }

    public void setOriginalStreamUrl(String originalStreamUrl) {
        mIpSelectionHelper.setOriginalStreamUrl(originalStreamUrl);
    }

    public void setDisplay(SurfaceHolder holder) {
        if (mStreamer != null) {
            mStreamer.setDisplay(holder);
        }
    }

    public void setGravity(Player.SurfaceGravity gravity, int width, int height) {
        if (mStreamer != null) {
            mStreamer.setGravity(gravity, width, height);
        }
    }

    public void shiftUp(float ratio) {
        if (mStreamer != null) {
            mStreamer.shiftUp(ratio);
        }
    }

    // 拉流开始
    public void startWatch() {
        if (mStreamer == null || mStarted || !mIpSelectionHelper.hasStreamUrl()) {
            return;
        }
        mStarted = true;
        mPaused = false;
        mReconnectHelper.startStream();
    }

    // 恢复播放
    public void resumeWatch() {
        if (mStreamer == null || !mStarted || !mPaused) {
            return;
        }
        mPaused = false;
        mStreamer.start();
    }

    // 结束播放
    public void pauseWatch() {
        if (mStreamer == null || !mStarted || mPaused) {
            return;
        }
        mPaused = true;
        mStreamer.pause();
    }

    // 拉流结束
    public void stopWatch() {
        if (mStreamer == null || !mStarted) {
            return;
        }
        mStarted = false;
        mPaused = false;
        mReconnectHelper.stopStream();
    }

    // 播放器回调
    protected class PlayerCallback<PLAYER extends IPlayer> implements IPlayerCallback<PLAYER> {

        @Override
        public void onPrepared(PLAYER player) {
            mUIHandler.sendEmptyMessage(MSG_PLAYER_PREPARED);
        }

        @Override
        public void onCompletion(PLAYER player) {
            mUIHandler.sendEmptyMessage(MSG_PLAYER_COMPLETED);
        }

        @Override
        public void onSeekComplete(PLAYER player) {
            mUIHandler.sendEmptyMessage(MSG_SEEK_COMPLETED);
        }

        @Override
        public void onVideoSizeChanged(PLAYER player, int width, int height) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onError(PLAYER player, int what, int extra) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
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
                            mUIHandler.removeMessages(MSG_RECONNECT_STREAM);
                            mUIHandler.sendEmptyMessageDelayed(MSG_RECONNECT_STREAM, RECONNECT_TIMEOUT);
                            break;
                        case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                            MyLog.w(TAG, "MEDIA_INFO_BUFFERING_END");
                            if (mIpSelectionHelper.isStuttering()) {
                                mIpSelectionHelper.updateStutterStatus(false);
                                mController.postEvent(MSG_ON_STREAM_SUCCESS);
                            }
                            mUIHandler.removeMessages(MSG_RECONNECT_STREAM);
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
                case MSG_RECONNECT_STREAM:
                    MyLog.w(TAG, "MSG_RECONNECT_STREAM");
                    presenter.mReconnectHelper.startReconnect(0);
                    break;
                case MSG_PLAYER_PREPARED: // fall through
                case MSG_PLAYER_COMPLETED:
                case MSG_SEEK_COMPLETED:
                    presenter.mController.postEvent(msg.what); // 转发事件
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
            if (mStarted && mIpSelectionHelper.isStuttering()) {
                startReconnect(0);
            }
        }

        @Override
        protected void startStream() {
            if (!mStreamStarted) {
                MyLog.w(TAG, "startStream");
                mStreamStarted = true;
                mIpSelectionHelper.ipSelect();
                mStreamer.setVideoPath(mIpSelectionHelper.getStreamUrl(), mIpSelectionHelper.getStreamHost());
                mStreamer.setIpList(mIpSelectionHelper.getSelectedHttpIpList(), mIpSelectionHelper.getSelectedLocalIpList());
                mStreamer.prepare(mIsRealTime);
                mStreamer.start();
            } else {
                MyLog.w(TAG, "startStream is ignored, mStreamStarted=" + mStreamStarted);
            }
        }

        @Override
        protected void stopStream() {
            if (mStreamStarted) {
                MyLog.w(TAG, "stopStream");
                mStreamStarted = false;
                mStreamer.stop();
            } else {
                MyLog.w(TAG, "stopStream is ignored, mStreamStarted=" + mStreamStarted);
            }
        }

        @Override
        protected void startReconnect(int code) {
            if (mStreamer != null && mStreamStarted) {
                MyLog.w(TAG, "startReconnect, code = " + code);
                if (!mIpSelectionHelper.isStuttering()) {
                    mIpSelectionHelper.updateStutterStatus(true);
                    mController.postEvent(MSG_ON_STREAM_RECONNECT);
                }
                mIpSelectionHelper.ipSelect();
                mStreamer.setVideoPath(mIpSelectionHelper.getStreamUrl(), mIpSelectionHelper.getStreamHost());
                mStreamer.setIpList(mIpSelectionHelper.getSelectedHttpIpList(), mIpSelectionHelper.getSelectedLocalIpList());
                mStreamer.reconnect();
            } else {
                MyLog.w(TAG, "startReconnect is ignored, mStreamStarted=" + mStreamStarted);
            }
        }
    }
}
