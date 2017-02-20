package com.wali.live.video.widget;

import android.os.Message;

import com.base.log.MyLog;

/**
 * Created by lan on 16-1-5.
 * 视频播放控制回调的包装器
 */
public class VideoPlayerCallBackWrapper implements IPlayerCallBack {
    public final static String TAG = VideoPlayerCallBackWrapper.class.getSimpleName();
    @Override
    public void onLoad() {
        MyLog.w(TAG,"onLoad");
    }

    @Override
    public void onPrepared() {
        MyLog.w(TAG,"onPrepared");
    }

    @Override
    public void onCompletion() {
        MyLog.w(TAG,"onCompletion");
    }

    @Override
    public void onError(int errCode) {
        MyLog.w(TAG,"onError");
    }

    @Override
    public void onBufferingUpdate(int percent) {
        MyLog.w(TAG,"onBufferingUpdate");
    }

    @Override
    public void onInfo(int info) {
        MyLog.w(TAG,"onInfo");
    }

    @Override
    public void onInfo(Message msg) {
        MyLog.w(TAG,"onInfo");
    }

    @Override
    public void onSeekComplete() {
        MyLog.w(TAG,"onSeekComplete");
    }

    @Override
    public void requestOrientation(int playMode) {
        MyLog.w(TAG,"requestOrientation");
    }

    @Override
    public void onReleased() {
        MyLog.w(TAG,"onReleased");
    }
}
