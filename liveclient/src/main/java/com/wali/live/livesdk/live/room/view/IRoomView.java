package com.wali.live.livesdk.live.room.view;

import com.wali.live.livesdk.live.room.mvp.IView;
import com.wali.live.proto.LiveCommonProto;

import java.util.List;

/**
 * Created by lan on 17/4/20.
 */
public interface IRoomView extends IView {
    void processAction(String milinkCommand, int errCode);

    void processAction(String milinkCommand, int errCode,
                       String liveId, List<LiveCommonProto.UpStreamUrl> list, String url);
}
