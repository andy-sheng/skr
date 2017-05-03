package com.mi.liveassistant.room.manager.watch.callback;

/**
 * Created by lan on 17/4/24.
 */
public interface IWatchCallback {
    void notifyFail(int errCode);

    void notifySuccess();
}
