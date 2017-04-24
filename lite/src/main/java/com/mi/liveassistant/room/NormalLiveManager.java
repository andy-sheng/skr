package com.mi.liveassistant.room;

import com.mi.liveassistant.data.Location;
import com.mi.liveassistant.room.callback.ICallback;

/**
 * Created by lan on 17/4/20.
 */
class NormalLiveManager extends BaseLiveManager {
    protected NormalLiveManager() {
        super();
        mIsGameLive = false;
    }

    @Override
    public void beginLive(Location location, String title, String coverUrl, ICallback callback) {
        super.beginLive(location, title, coverUrl, callback);
        mLivePresenter.beginNormalLive(location, title, coverUrl);
    }
}
