package com.mi.liveassistant.room.view;

import com.mi.liveassistant.data.model.Location;
import com.mi.liveassistant.room.manager.live.callback.ILiveCallback;

/**
 * Created by lan on 17/4/20.
 */
public interface ILiveView extends ILifecycleView {
    void beginLive(Location location, String title, String coverUrl, ILiveCallback callback);

    void endLive(ILiveCallback callback);
}
