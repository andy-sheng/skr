package com.mi.liveassistant.room.manager.watch.callback;

/**
 * Created by lan on 17/4/24.
 */
public interface IWatchCallback {
    /**
     * 进入房间观看失败
     *
     * @param errCode 服务器返回的错误码
     */
    void notifyFail(int errCode);

    /**
     * 进入房间观看成功
     */
    void notifySuccess();
}
