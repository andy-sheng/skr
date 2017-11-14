package com.wali.live.watchsdk.videodetail.data;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * Created by yangli on 2017/3/15.
 */
public abstract class BaseStreamerPresenter<R extends BaseStreamerPresenter.ReconnectHelper, IP, STREAM> {
    private String TAG = getTAG();

    protected static final int RECONNECT_TIMEOUT = 5 * 1000;    // 卡顿超时换IP时间
    protected static final int START_STREAM_TIMEOUT = 5 * 1000; // 推流超时时间

    protected static final int MSG_START_STREAM = 1001;         // 启动流
    protected static final int MSG_START_STREAM_TIMEOUT = 1002; // 推/拉流超时
    protected static final int MSG_START_STREAM_FAILED = 1003;  // 推/拉流失败

    protected MyUIHandler mUIHandler;
    protected R mReconnectHelper;
    protected IP mIpSelectionHelper;

    protected STREAM mStreamer;

    protected abstract String getTAG();

    public abstract void destroy();

    public final void setStreamer(STREAM streamer) {
        mStreamer = streamer;
    }

    /**
     * 重连
     */
    protected abstract class ReconnectHelper {
        /**
         * 开始推/拉流
         */
        protected abstract void startStream();

        /**
         * 停止推/拉流
         */
        protected abstract void stopStream();

        /**
         * 开始重连
         */
        protected abstract void startReconnect(int code);

        public ReconnectHelper() {
        }
    }

    /**
     * 消息队列
     */
    protected static class MyUIHandler<T extends BaseStreamerPresenter> extends Handler {
        protected final WeakReference<T> mPresenterRef;
        protected final String TAG;

        protected final <T> T deRef(WeakReference<T> reference) {
            return reference != null ? reference.get() : null;
        }

        public MyUIHandler(@NonNull T presenter) {
            mPresenterRef = new WeakReference<>(presenter);
            TAG = presenter.getTAG();
        }
    }
}
