package com.mi.liveassistant.unity;

import com.mi.liveassistant.room.manager.watch.callback.IWatchListener;

/**
 * Created by yangli on 2017/5/10.
 */
public interface IUnityWatchListener extends ILoginListener, IWatchListener {
    /**
     * 进入观看房间失败
     *
     * @param errCode 服务器返回的错误码
     * @param errMsg  错误描述
     */
    void onEnterLiveFailed(int errCode, String errMsg);

    /**
     * 进入观看房间成功
     */
    void onEnterLiveSuccess();
}
