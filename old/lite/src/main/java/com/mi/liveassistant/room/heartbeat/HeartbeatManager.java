package com.mi.liveassistant.room.heartbeat;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.mi.liveassistant.common.api.ErrorCode;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.proto.LiveProto;
import com.mi.liveassistant.room.heartbeat.request.HeartbeatRequest;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lan on 17/4/24.
 */
public class HeartbeatManager {
    private static final String TAG = HeartbeatManager.class.getSimpleName();

    private static final int HEARTBEAT_DURATION = 10 * 1000;        // 心跳间隔时间
    private static final int HEARTBEAT_TIMEOUT = 120 * 1000;        // 心跳超时时间

    private static final int MSG_HEARTBEAT = 203;                   // 心跳
    private static final int MSG_HEARTBEAT_TIMEOUT = 204;           // 心跳超时

    private ExecutorService mHeartbeatService;
    private MyUIHandler mUIHandler;

    private String mLiveId;
    private boolean mIsGameLive;
    private boolean mIsPaused;

    private ICallback mOutCallback;

    public HeartbeatManager() {
    }

    public void setParam(String liveId, boolean isGameLive) {
        mLiveId = liveId;
        mIsGameLive = isGameLive;
    }

    public void start(ICallback callback) {
        mOutCallback = callback;

        if (mHeartbeatService == null) {
            mHeartbeatService = Executors.newSingleThreadExecutor();
        }
        if (mUIHandler == null) {
            mUIHandler = new MyUIHandler(Looper.getMainLooper(), this);
        }
        mUIHandler.sendEmptyMessage(MSG_HEARTBEAT);
        mUIHandler.sendEmptyMessageDelayed(MSG_HEARTBEAT_TIMEOUT, HEARTBEAT_TIMEOUT);
    }

    private void sendHeartbeat() {
        if (mHeartbeatService == null) {
            return;
        }
        mHeartbeatService.execute(new Runnable() {
            @Override
            public void run() {
                LiveProto.HeartBeatRsp rsp = new HeartbeatRequest(mLiveId, !mIsPaused ? 0 : 1).syncRsp();
                if (rsp != null) {
                    switch (rsp.getRetCode()) {
                        case ErrorCode.CODE_SUCCESS:
                            mUIHandler.removeMessages(MSG_HEARTBEAT_TIMEOUT);
                            mUIHandler.sendEmptyMessageDelayed(MSG_HEARTBEAT_TIMEOUT, HEARTBEAT_TIMEOUT);
                            break;
                    }
                }
            }
        });
    }

    private void notifyTimeout() {
        if (mOutCallback != null) {
            mOutCallback.notifyTimeout();
        }
    }

    public void stop() {
        if (mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
        }
        if (mHeartbeatService != null) {
            mHeartbeatService.shutdownNow();
            mHeartbeatService = null;
        }
    }

    public void pause() {
        mIsPaused = true;
    }

    public void resume() {
        mIsPaused = false;
    }

    private static class MyUIHandler extends Handler {
        private static final String TAG = "HeartbeatManager";

        private final WeakReference<HeartbeatManager> mManagerRef;

        public MyUIHandler(Looper looper, HeartbeatManager manager) {
            super(looper);
            mManagerRef = new WeakReference<>(manager);
        }

        @Override
        public void handleMessage(Message msg) {
            final HeartbeatManager manager = mManagerRef.get();
            switch (msg.what) {
                case MSG_HEARTBEAT:
                    MyLog.w(TAG, "MSG_HEARTBEAT");
                    manager.sendHeartbeat();
                    removeMessages(MSG_HEARTBEAT);
                    sendEmptyMessageDelayed(MSG_HEARTBEAT, HEARTBEAT_DURATION);
                    break;
                case MSG_HEARTBEAT_TIMEOUT:
                    MyLog.w(TAG, "MSG_HEARTBEAT_TIMEOUT");
                    manager.notifyTimeout();
                    break;
                default:
                    break;
            }
        }
    }

    public interface ICallback {
        void notifyTimeout();
    }
}
