package com.mi.liveassistant.unity;

import com.mi.liveassistant.room.manager.live.callback.ILiveCallback;
import com.mi.liveassistant.room.manager.live.callback.ILiveListener;

/**
 * Created by yangli on 2017/5/10.
 */
public interface IUnityLiveListener extends ILoginListener, ILiveListener {
    /**
     * 启动推流房间失败
     *
     * @param errCode 服务器返回的错误码
     * @param errMsg  错误描述
     */
    void onBeginLiveFailed(int errCode, String errMsg);

    /**
     * 启动推流房间成功
     *
     * @param playerId 主播ID
     * @param liveId   房间ID
     */
    void onBeginLiveSuccess(long playerId, String liveId);

    /**
     * 结束推流房间失败
     *
     * @param errCode 服务器返回的错误码
     * @param errMsg  错误描述
     */
    void onEndLiveFailed(int errCode, String errMsg);

    /**
     * 结束推流房间成功
     *
     * @param playerId 主播ID
     * @param liveId   房间ID
     */
    void onEndLiveSuccess(long playerId, String liveId);
}
