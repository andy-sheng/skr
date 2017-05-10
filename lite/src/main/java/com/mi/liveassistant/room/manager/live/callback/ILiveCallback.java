package com.mi.liveassistant.room.manager.live.callback;

/**
 * Created by lan on 17/4/24.
 */
public interface ILiveCallback {
    /**
     * 进入/退出推流房间失败
     *
     * @param errCode 服务器返回的错误码
     */
    void notifyFail(int errCode);

    /**
     * 进入/退出推流房间成功
     *
     * @param playerId 主播ID
     * @param liveId   房间ID
     */
    void notifySuccess(long playerId, String liveId);
}
