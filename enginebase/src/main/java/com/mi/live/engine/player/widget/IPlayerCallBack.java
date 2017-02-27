package com.mi.live.engine.player.widget;

import android.os.Message;

/**
 * Created by lan on 16-1-5.
 * 视频播放控制器的回调
 */
public interface IPlayerCallBack {

    // 加载完成
    void onLoad();

    // 视频准备
    void onPrepared();

    // 视频完成
    void onCompletion();

    // 视频出错
    void onError(int errCode);

    // 视频缓冲
    void onBufferingUpdate(int percent);

    // 视频消息
    void onInfo(int info);

    //转发引擎的各种参数数据
    void onInfo(Message msg);

    // 视频进度调整完成
    void onSeekComplete();

    // 横竖屏切换
    void requestOrientation(int playMode);

    // 播放器释放
    void onReleased();
}
