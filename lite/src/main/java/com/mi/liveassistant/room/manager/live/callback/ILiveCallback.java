package com.mi.liveassistant.room.manager.live.callback;

/**
 * Created by lan on 17/4/24.
 */
public interface ILiveCallback {
    void notifyFail(int errCode);

    void notifySuccess(long playerId, String liveId);
}
