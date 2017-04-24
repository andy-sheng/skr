package com.mi.liveassistant.room.callback;

/**
 * Created by lan on 17/4/24.
 */
public interface ICallback {
    void notifyFail(int errCode);

    void notifySuccess();
}
