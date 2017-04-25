package com.mi.liveassistant.room.manager;

import com.mi.liveassistant.data.Location;
import com.mi.liveassistant.room.callback.ICallback;
import com.mi.liveassistant.room.presenter.NormalLivePresenter;

/**
 * Created by lan on 17/4/20.
 */
public class NormalLiveManager extends BaseLiveManager {
    protected NormalLiveManager() {
        super();
        mIsGameLive = false;
        mLivePresenter = new NormalLivePresenter(this);
    }

    @Override
    public void beginLive(Location location, String title, String coverUrl, ICallback callback) {
        super.beginLive(location, title, coverUrl, callback);
        mLivePresenter.beginLive(location, title, coverUrl);
    }

    @Override
    protected void createStreamer() {
    }
}
