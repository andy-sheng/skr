package com.wali.live.watchsdk.videothird.data;

import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.thornbirds.component.IEventController;
import com.wali.live.dns.IDnsStatusListener;
import com.wali.live.ipselect.WatchIpSelectionHelper;
import com.wali.live.watchsdk.videothird.data.engine.IPlayer;
import com.wali.live.watchsdk.videothird.data.engine.IPlayerCallback;

import static com.wali.live.component.BaseSdkController.MSG_ON_STREAM_RECONNECT;

/**
 * Created by yangli on 17-5-3.
 *
 * @module 拉流器数据
 */
public class PullStreamerPresenter extends BaseStreamerPresenter<PullStreamerPresenter.ReconnectHelper,
        WatchIpSelectionHelper, IPlayer> {
    private static final String TAG = "PullStreamerPresenter";

    @Nullable
    private IEventController mController;

    private boolean mIsRealTime = true;
    private boolean mPaused = true;

    private final PlayerCallback<IPlayer> mPlayerCallback = new PlayerCallback<>();

    @Override
    protected String getTAG() {
        return TAG;
    }

    public boolean isStarted() {
        return mStarted;
    }

    public void setComponentController(@Nullable IEventController controller) {
        mController = controller;
    }

    public PullStreamerPresenter() {
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

    public void setOriginalStreamUrl(String originalStreamUrl) {
        mIpSelectionHelper.setOriginalStreamUrl(originalStreamUrl);
    }

    // 拉流开始
    public void startWatch() {
        if (mStreamer == null || mStarted) {
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
                if (!mIpSelectionHelper.isStuttering() && mController != null) {
                    mController.postEvent(MSG_ON_STREAM_RECONNECT);
                }
                mIpSelectionHelper.updateStutterStatus(true);
                mIpSelectionHelper.ipSelect();
                mStreamer.setVideoPath(mIpSelectionHelper.getStreamUrl(), mIpSelectionHelper.getStreamHost());
                mStreamer.setIpList(mIpSelectionHelper.getSelectedHttpIpList(), mIpSelectionHelper.getSelectedLocalIpList());
                mStreamer.reconnect();
            } else {
                MyLog.w(TAG, "startReconnect is ignored, mStreamStarted=" + mStreamStarted);
            }
        }
    }

    // 播放器回调
    protected class PlayerCallback<PLAYER extends IPlayer> implements IPlayerCallback<PLAYER> {

        @Override
        public void onPrepared(PLAYER player) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onCompletion(PLAYER player) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onSeekComplete(PLAYER player) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                }
            });
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
        public void onInfo(PLAYER player, int what, int extra) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
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
                default:
                    break;
            }
        }
    }
}
