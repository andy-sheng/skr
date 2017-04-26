package com.mi.liveassistant.room.view;

import com.mi.liveassistant.common.mvp.IView;

/**
 * Created by lan on 17/4/21.
 */
public interface IWatchView extends IView {
    void enterLive(long playerId, String liveId);

    void notifyEnterLiveFail(int errCode);

    void notifyEnterLiveSuccess(String downStreamUrl);

    void leaveLive();
}
