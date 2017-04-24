package com.mi.liveassistant.room;

import com.mi.liveassistant.data.Location;
import com.mi.liveassistant.room.callback.ICallback;

/**
 * Created by lan on 17/4/20.
 */
class GameLiveManager extends BaseLiveManager {
    protected GameLiveManager() {
        super();
        mIsGameLive = true;
    }

    @Override
    public void beginLive(Location location, String title, String coverUrl, ICallback callback) {
        super.beginLive(location, title, coverUrl, callback);
        mLivePresenter.beginGameLive(location, title, coverUrl);
    }
}
