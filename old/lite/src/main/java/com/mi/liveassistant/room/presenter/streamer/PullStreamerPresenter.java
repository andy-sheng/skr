package com.mi.liveassistant.room.presenter.streamer;

import com.mi.liveassistant.common.global.GlobalData;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.dns.IDnsStatusListener;
import com.mi.liveassistant.dns.WatchIpSelectionHelper;
import com.mi.liveassistant.engine.player.widget.IPlayerPresenter;

/**
 * Created by yangli on 17-5-3.
 * <p>
 * * @module 拉流器数据
 */
public class PullStreamerPresenter extends BaseStreamerPresenter<PullStreamerPresenter.ReconnectHelper,
        WatchIpSelectionHelper, IPlayerPresenter> {
    private static final String TAG = "PullStreamerPresenter";

    @Override
    protected String getTAG() {
        return TAG;
    }

    public PullStreamerPresenter() {
        mUIHandler = new MyUIHandler(this);
        mReconnectHelper = new ReconnectHelper();
        mIpSelectionHelper = new WatchIpSelectionHelper(GlobalData.app(), mReconnectHelper);
    }

    @Override
    public void destroy() {
        if (mStreamer != null) {
            mStreamer.destroy();
            mStreamer = null;
        }
        mUIHandler.removeCallbacksAndMessages(null);
    }

    public void setOriginalStreamUrl(String originalStreamUrl) {
        mIpSelectionHelper.setOriginalStreamUrl(originalStreamUrl);
    }

    // 拉流开始
    public void startLive() {
        if (mStreamer == null || mLiveStarted) {
            return;
        }
        mLiveStarted = true;
        mReconnectHelper.startStream();
    }

    // 拉流结束
    public void stopLive() {
        if (mStreamer == null || !mLiveStarted) {
            return;
        }
        mLiveStarted = false;
        mReconnectHelper.stopStream();
    }

    // 域名解析、重连相关
    protected class ReconnectHelper extends BaseStreamerPresenter.ReconnectHelper implements IDnsStatusListener {

        @Override
        public void onDnsReady() {
            MyLog.w(TAG, "onDnsReady");
            if (mLiveStarted && mIpSelectionHelper.isStuttering()) {
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
            } else {
                MyLog.w(TAG, "startStream is ignored, mStreamStarted=" + mStreamStarted);
            }
        }

        @Override
        protected void stopStream() {
            if (mStreamStarted) {
                MyLog.w(TAG, "stopStream");
                mStreamStarted = false;
                mStreamer.reset();
            } else {
                MyLog.w(TAG, "stopStream is ignored, mStreamStarted=" + mStreamStarted);
            }
        }

        @Override
        protected void startReconnect(int code) {
            if (mStreamer != null && mStreamStarted) {
                MyLog.w(TAG, "startReconnect, code = " + code);
                if (!mIpSelectionHelper.isStuttering()) {
                    //TODO event notify MSG_ON_STREAM_RECONNECT
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
}
