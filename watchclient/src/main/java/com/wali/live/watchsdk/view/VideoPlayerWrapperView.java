package com.wali.live.watchsdk.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.base.log.MyLog;
import com.mi.live.engine.media.player.IMediaPlayer;
import com.mi.live.engine.player.widget.VideoPlayerCallBackWrapper;
import com.mi.live.engine.player.widget.VideoPlayerTextureView;
import com.wali.live.dns.IDnsStatusListener;
import com.wali.live.ipselect.WatchIpSelectionHelper;

import java.lang.ref.WeakReference;

/**
 * Created by lan on 17/3/10.
 */
public class VideoPlayerWrapperView extends VideoPlayerTextureView implements IDnsStatusListener {
    // 域名解析、重连相关
    public static final int MSG_RELOAD_VIDEO = 100;             // onInfo开始buffer时，reload数据的标记。
    public static final int PLAYER_KADUN_RELOAD_TIME = 5000;    // 毫秒

    private WatchIpSelectionHelper mIpSelectionHelper;

    protected Handler mHandler = new MyUIHandler(this);

    protected boolean mIsCompletion = false;

    private VideoPlayerCallBackWrapper mIPlayerCallBack = new VideoPlayerCallBackWrapper() {
        @Override
        public void onPrepared() {
            MyLog.v(TAG, "onPrepared");
            if (mOuterCallBack != null) {
                mOuterCallBack.onPrepared();
            }
        }

        @Override
        public void onCompletion() {
            MyLog.v(TAG, "onCompletion");
            mIsCompletion = true;
        }

        @Override
        public void onError(int errCode) {
            MyLog.v(TAG, "onError code=" + errCode);
            pause();
            if (mOuterCallBack != null) {
                mOuterCallBack.onError(errCode);
            }
        }

        @Override
        public void onInfo(int info) {
            MyLog.v(TAG, "onInfo int=" + info);
        }

        @Override
        public void onInfo(Message msg) {
            MyLog.v(TAG, "onInfo int=" + msg.what + " , msg=" + msg.toString());
            switch (msg.what) {
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    MyLog.w(TAG, "MEDIA_INFO_BUFFERING_START");
                    if (mIpSelectionHelper != null) {
                        mIpSelectionHelper.updateStutterStatus(true);
                        mHandler.removeMessages(MSG_RELOAD_VIDEO);
                        mHandler.sendEmptyMessageDelayed(MSG_RELOAD_VIDEO, PLAYER_KADUN_RELOAD_TIME);
                    }
                    if (mOuterCallBack != null) {
                        mOuterCallBack.onBufferingStart();
                    }
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    MyLog.w(TAG, "MEDIA_INFO_BUFFERING_END");
                    if (mIpSelectionHelper != null) {
                        mIpSelectionHelper.updateStutterStatus(false);
                        mHandler.removeMessages(MSG_RELOAD_VIDEO);
                    }
                    if (mOuterCallBack != null) {
                        mOuterCallBack.onBufferingEnd();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private IOuterCallBack mOuterCallBack;

    protected String getTAG() {
        return getClass().getSimpleName();
    }

    public VideoPlayerWrapperView(Context context) {
        super(context);
    }

    public VideoPlayerWrapperView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoPlayerWrapperView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mIpSelectionHelper = new WatchIpSelectionHelper(context, this, null);
        mVideoPlayerPresenter.setVideoPlayerCallBack(mIPlayerCallBack);
        mVideoPlayerPresenter.setBufferSize(500);
    }

    public void setOuterCallBack(IOuterCallBack callback) {
        mOuterCallBack = callback;
    }

    public void play(String videoUrl) throws LoadLibraryException {
        if (!TextUtils.isEmpty(videoUrl)) {
            mIpSelectionHelper.setOriginalStreamUrl(videoUrl);
            mIpSelectionHelper.ipSelect();
            mVideoPlayerPresenter.setVideoPath(mIpSelectionHelper.getStreamUrl(), mIpSelectionHelper.getStreamHost());
            mVideoPlayerPresenter.setIpList(mIpSelectionHelper.getSelectedHttpIpList(), mIpSelectionHelper.getSelectedLocalIpList());
            MyLog.w(TAG, "ipSelect streamUrl = " + mIpSelectionHelper.getStreamUrl());
            MyLog.w(TAG, "ipSelect http ipList = " + mIpSelectionHelper.getSelectedHttpIpList());
            MyLog.w(TAG, "ipSelect local ipList = " + mIpSelectionHelper.getSelectedLocalIpList());
            mVideoPlayerPresenter.setVideoPlayerCallBack(mIPlayerCallBack);
            resume();
        }
    }

    public void mute(boolean isMute) {
        if (mVideoPlayerPresenter != null) {
            MyLog.w(TAG, "mute=" + isMute);
            if (isMute) {
                mVideoPlayerPresenter.setVolume(0, 0);
            } else {
                mVideoPlayerPresenter.setVolume(1, 1);
            }
        }
    }

    public void notifyOrientation(boolean isLandscape) {
        MyLog.w(TAG, "notifyOrientation isLandscape=" + isLandscape);
        if (mVideoPlayerPresenter != null) {
            mVideoPlayerPresenter.notifyOrientation(isLandscape);
        }
    }

    private void startReconnect() {
        MyLog.w(TAG, "startReconnect");
        mHandler.removeMessages(MSG_RELOAD_VIDEO);
        if (mIpSelectionHelper != null) {
            mIpSelectionHelper.ipSelect();
            mVideoPlayerPresenter.setVideoPath(mIpSelectionHelper.getStreamUrl(), mIpSelectionHelper.getStreamHost());
            mVideoPlayerPresenter.setIpList(mIpSelectionHelper.getSelectedHttpIpList(), mIpSelectionHelper.getSelectedLocalIpList());
        }
        reconnect();
    }

    private void reconnect() {
        mVideoPlayerPresenter.reconnect();
    }

    @Override
    public void onDnsReady() {
        MyLog.w(TAG, "onDnsReady");
        if (mIpSelectionHelper.isStuttering()) {
            startReconnect();
        }
    }

    public void resume() {
        if (mIsCompletion) {
            mIsCompletion = false;
            mVideoPlayerPresenter.seekTo(0);
        }
        mVideoPlayerPresenter.start();
        mVideoPlayerPresenter.enableReconnect(true);
    }

    public void pause() {
        mVideoPlayerPresenter.enableReconnect(false);
        mVideoPlayerPresenter.pause();
    }

    public void release() {
        mVideoPlayerPresenter.release();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mIpSelectionHelper != null) {
            mIpSelectionHelper.destroy();
        }
    }

    public long getCurrentPosition() {
        return mVideoPlayerPresenter.getCurrentPosition();
    }

    public void seekTo(long ts) {
        mVideoPlayerPresenter.seekTo(ts);
    }

    private static class MyUIHandler extends Handler {
        private final WeakReference<VideoPlayerWrapperView> mWrapperViewWeakRef;

        public MyUIHandler(VideoPlayerWrapperView wrapperView) {
            super(Looper.getMainLooper());
            mWrapperViewWeakRef = new WeakReference(wrapperView);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoPlayerWrapperView wrapperView = mWrapperViewWeakRef.get();
            if (wrapperView == null) {
                return;
            }
            switch (msg.what) {
                case MSG_RELOAD_VIDEO:
                    MyLog.w(wrapperView.TAG, "MSG_RELOAD_VIDEO");
                    wrapperView.startReconnect();
                    break;
                default:
                    break;
            }
        }
    }


    public static class LoadLibraryException extends RuntimeException {
        public LoadLibraryException() {
        }

        public LoadLibraryException(String detailMessage) {
            super(detailMessage);
        }
    }

    public interface IOuterCallBack {
        void onPrepared();

        void onCompletion();

        void onBufferingStart();

        void onBufferingEnd();

        void onError(int errCode);
    }
}
