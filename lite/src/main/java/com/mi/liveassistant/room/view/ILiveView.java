package com.mi.liveassistant.room.view;

import com.mi.liveassistant.data.Location;
import com.mi.liveassistant.proto.LiveCommonProto;
import com.mi.liveassistant.room.callback.ICallback;

import java.util.List;

/**
 * Created by lan on 17/4/20.
 */
public interface ILiveView extends ILifecycleView {
    void beginLive(Location location, String title, String coverUrl, ICallback callback);

    void notifyBeginLiveFail(int errCode);

    void notifyBeginLiveSuccess(String liveId, List<LiveCommonProto.UpStreamUrl> list, String url);

    void endLive(ICallback callback);

    void notifyEndLiveFail(int errCode);

    void notifyEndLiveSuccess();
}
