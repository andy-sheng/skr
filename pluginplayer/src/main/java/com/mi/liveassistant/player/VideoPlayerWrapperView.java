package com.mi.liveassistant.player;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.base.ipselect.IDnsStatusListener;
import com.base.ipselect.WatchIpSelectionHelper;
import com.base.log.MyLog;
import com.mi.live.engine.media.player.IMediaPlayer;
import com.mi.live.engine.player.widget.VideoPlayerCallBackWrapper;
import com.mi.live.engine.player.widget.VideoPlayerTextureView;

import java.lang.ref.WeakReference;

/**
 * Created by lan on 17/3/10.
 */
public class VideoPlayerWrapperView extends VideoPlayerTextureView implements IDnsStatusListener {
    // 域名解析、重连相关
    public static final int MSG_RELOAD_VIDEO = 100;             // onInfo开始buffer时，reload数据的标记。
    public static final int PLAYER_KADUN_RELOAD_TIME = 5000;    //毫秒

    private WatchIpSelectionHelper mIpSelectionHelper;

    protected Handler mHandler = new MyUIHandler(this);

    protected boolean mIsCompletion = false;

    private VideoPlayerCallBackWrapper mIPlayerCallBack = new VideoPlayerCallBackWrapper() {
        @Override
        public void onPrepared() {
            MyLog.v(TAG, " onPrepared");
            if (mVideoPlayerPresenter != null && mVideoPlayerPresenter.isEnableReconnect()) {
                //TODO
            }
        }

        @Override
        public void onCompletion() {
            MyLog.v(TAG, "onCompletion");
            mIsCompletion = true;
        }

        @Override
        public void onError(int errCode) {
            MyLog.v(TAG, "onError " + errCode);
            pause();
        }

        @Override
        public void onInfo(int info) {
            MyLog.v(TAG, "onInfo int " + info);
        }

        @Override
        public void onInfo(Message msg) {
            onInfo(msg.what);
            switch (msg.what) {
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    MyLog.w(TAG, "MEDIA_INFO_BUFFERING_START");
                    if (mIpSelectionHelper != null) {
                        if (!mIpSelectionHelper.isStuttering()) {
                            //TODO
                        }
                        mIpSelectionHelper.updateStutterStatus(true);

                        mHandler.removeMessages(MSG_RELOAD_VIDEO);
                        mHandler.sendEmptyMessageDelayed(MSG_RELOAD_VIDEO, PLAYER_KADUN_RELOAD_TIME);
                    }
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    MyLog.w(TAG, "MEDIA_INFO_BUFFERING_END");
                    if (mIpSelectionHelper != null) {
                        if (mIpSelectionHelper.isStuttering()) {
                            //TODO
                        }
                        mIpSelectionHelper.updateStutterStatus(false);

                        mHandler.removeMessages(MSG_RELOAD_VIDEO);
                        mHandler.sendEmptyMessageDelayed(MSG_RELOAD_VIDEO, PLAYER_KADUN_RELOAD_TIME);
                    }
                    break;
                default:
                    break;
            }
            MyLog.v(TAG, "onInfo " + msg.toString());
        }
    };

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

        mIpSelectionHelper = new WatchIpSelectionHelper(context, this);

        mVideoPlayerPresenter.setVideoPlayerCallBack(mIPlayerCallBack);
        mVideoPlayerPresenter.setBufferSize(500);
    }

    public void play(String videoUrl) {
        if (!TextUtils.isEmpty(videoUrl)) {
            //ip优选
//            mIpSelectionHelper.setOriginalStreamUrl(videoUrl);
//            mIpSelectionHelper.ipSelect();
//
//            mVideoPlayerPresenter.setVideoPath(mIpSelectionHelper.getStreamUrl(), mIpSelectionHelper.getStreamHost());
//            mVideoPlayerPresenter.setIpList(mIpSelectionHelper.getSelectedHttpIpList(), mIpSelectionHelper.getSelectedLocalIpList());

            mVideoPlayerPresenter.setVideoPath(videoUrl, "");

            MyLog.w(TAG, "mIpSelectionHelper.getStreamUrl() = " + mIpSelectionHelper.getStreamUrl());
            MyLog.w(TAG, "mIpSelectionHelper.getSelectedHttpIpList() = " + mIpSelectionHelper.getSelectedHttpIpList());
            MyLog.w(TAG, "mIpSelectionHelper.getSelectedLocalIpList() = " + mIpSelectionHelper.getSelectedLocalIpList());

            mVideoPlayerPresenter.setVideoPlayerCallBack(mIPlayerCallBack);
            resume();
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
        long currentPosition = mVideoPlayerPresenter.getCurrentPosition();
        MyLog.w(TAG, "reconnect, currentPosition= " + currentPosition);
        mVideoPlayerPresenter.seekTo(currentPosition);
    }

    public void enableReconnect(boolean isEnable) {
        MyLog.w(TAG, "enableReconnect, isEnable= " + isEnable);
        mVideoPlayerPresenter.enableReconnect(isEnable);
    }

    @Override
    public void onDnsReady() {
        MyLog.w(TAG, "onDnsReady");
        if (mIpSelectionHelper.isStuttering()) {
            startReconnect();
        }
    }

    // 继续播放
    public void resume() {
        if (mIsCompletion) {
            mIsCompletion = false;
            mVideoPlayerPresenter.seekTo(0);
        }
        mVideoPlayerPresenter.start();
    }

    // 暂停播放
    public void pause() {
        mVideoPlayerPresenter.pause();
    }

    // 停止播放
    public void stop() {
        mVideoPlayerPresenter.release();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

        if (mIpSelectionHelper != null) {
            mIpSelectionHelper.destroy();
        }
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
}
