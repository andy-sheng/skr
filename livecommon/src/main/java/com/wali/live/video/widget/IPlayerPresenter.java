package com.wali.live.video.widget;

import java.util.List;

/**
 * Created by lan on 16-1-5.
 * 视频播放器需要的接口
 */
public interface IPlayerPresenter {

    void start();

    void reset();

    void pause();

    void destroy();

    long getDuration();

    long getCurrentPosition();

    void seekTo(long msec);

    boolean isPlaying();

    boolean isPaused();

    String getDebugStr();

    void notifyOrientation(boolean isLandscape);

    void destroyAndClearResource();

    String getIpAddress();

    void setPlayMode(int playMode);

    int getPlayMode();

    void setVideoPlayerCallBack(IPlayerCallBack playerCallBack);

    boolean isInErrorState();

    void setVideoPath(String path, String host);

    void setVideoPath(String liveId, String path, String host);

    void setVideoPath(String liveId, String path, String host, int interruptMode);

    void reconnect();

    long getMessageStamp();

    long getResumePosition();

    void resumeTo(long msec);

    /**
     * Note: 由于setIpList底层可能需要根据协议添加默认端口，所以，该接口最好在setVideoPath调用之后调用
     */
    void setIpList(List<String> httpIpList, List<String> localIpList);

    void setBufferSize(int size);

    void setVolume(float var1, float var2);

    void release();

    void setIsWatch(boolean isWatch);

    void enableReconnect(boolean isEnable);

    boolean isEnableReconnect();
}
