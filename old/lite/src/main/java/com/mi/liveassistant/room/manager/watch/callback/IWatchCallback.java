package com.mi.liveassistant.room.manager.watch.callback;

/**
 * Created by lan on 17/4/24.
 */
public interface IWatchCallback {
    /**
     * 进入拉流房间失败
     *
     * @param errCode 服务器返回的错误码
     */
    void notifyFail(int errCode);

    /**
     * 进入拉流房间成功
     */
    void notifySuccess();
}
