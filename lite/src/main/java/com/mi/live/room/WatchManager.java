package com.mi.live.room;

import com.mi.live.room.presenter.WatchPresenter;
import com.mi.live.room.view.IWatchView;

/**
 * Created by lan on 17/4/20.
 */
public class WatchManager implements IWatchView {
    private static final String TAG = RoomConstant.LOG_PREFIX + WatchManager.class.getSimpleName();

    private WatchPresenter mWatchPresenter;

    private long mPlayerId;
    private String mLiveId;

    private String mDownStreamUrl;

    public WatchManager() {
        mWatchPresenter = new WatchPresenter(this);
    }

    @Override
    public void enterLive(long playerId, String liveId) {
        mPlayerId = playerId;
        mLiveId = liveId;

        mWatchPresenter.enterLive(playerId, liveId);
    }

    @Override
    public void notifyEnterLiveFail(int errCode) {
    }

    @Override
    public void notifyEnterLiveSuccess(String downStreamUrl) {
        mDownStreamUrl = downStreamUrl;
    }
}
