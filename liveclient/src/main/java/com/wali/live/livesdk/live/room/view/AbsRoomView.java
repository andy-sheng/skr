package com.wali.live.livesdk.live.room.view;

import com.wali.live.proto.LiveCommonProto;

import java.util.List;

/**
 * Created by lan on 17/4/20.
 */
public abstract class AbsRoomView implements IRoomView {
    @Override
    public void processAction(String milinkCommand, int errCode) {
    }

    @Override
    public void processAction(String milinkCommand, int errCode, String liveId, List<LiveCommonProto.UpStreamUrl> list, String url) {
    }
}
