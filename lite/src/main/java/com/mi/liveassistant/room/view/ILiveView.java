package com.mi.liveassistant.room.view;

import com.mi.liveassistant.data.model.Location;
import com.mi.liveassistant.proto.LiveCommonProto;
import com.mi.liveassistant.room.manager.live.callback.ILiveCallback;

import java.util.List;

/**
 * Created by lan on 17/4/20.
 */
public interface ILiveView extends ILifecycleView {
    void beginLive(Location location, String title, String coverUrl, ILiveCallback callback);

    void notifyBeginLiveFail(int errCode);

    void notifyBeginLiveSuccess(String liveId, List<LiveCommonProto.UpStreamUrl> list, String url);

    void endLive(ILiveCallback callback);

    void notifyEndLiveFail(int errCode);

    void notifyEndLiveSuccess();
}
