package com.wali.live.watchsdk.watch.view;

/**
 * Created by zyh on 2017/6/15.
 */

public interface IWatchVideoView {
    void updateVideoUrl(String videoUrl);

    void updateRoomInfo(String roomId, String videoUrl);

    void notifyLiveEnd();
}
