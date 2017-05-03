package com.mi.liveassistant.room.view;

import com.mi.liveassistant.common.mvp.IView;
import com.mi.liveassistant.room.manager.watch.callback.IWatchCallback;

/**
 * Created by lan on 17/4/21.
 */
public interface IWatchView extends IView {
    void enterLive(long playerId, String liveId, IWatchCallback callback);

    void notifyEnterLiveFail(int errCode);

    void notifyEnterLiveSuccess(String downStreamUrl);

    void leaveLive();
}
