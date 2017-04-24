package com.mi.liveassistant.room.view;

import com.mi.liveassistant.common.mvp.IView;
import com.mi.liveassistant.data.Location;
import com.mi.liveassistant.proto.LiveCommonProto;

import java.util.List;

/**
 * Created by lan on 17/4/20.
 */
public interface ILiveView extends IView {
    void beginNormalLive(Location location, String title, String coverUrl);

    void beginGameLive(Location location, String title, String coverUrl);

    void notifyBeginLiveFail(int errCode);

    void notifyBeginLiveSuccess(String liveId, List<LiveCommonProto.UpStreamUrl> list, String url);

    void endLive();

    void notifyEndLiveFail(int errCode);

    void notifyEndLiveSuccess();
}
