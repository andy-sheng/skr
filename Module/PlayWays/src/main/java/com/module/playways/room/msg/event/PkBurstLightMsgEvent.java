package com.module.playways.room.msg.event;

import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.GrabRoom.PKBLightMsg;

public class PkBurstLightMsgEvent {
    BasePushInfo info;
    PKBLightMsg pKBLightMsg;
    public PkBurstLightMsgEvent(BasePushInfo basePushInfo, PKBLightMsg pkbLightMsg){
        info = basePushInfo;
        pKBLightMsg = pkbLightMsg;
    }

    public BasePushInfo getInfo() {
        return info;
    }

    public PKBLightMsg getpKBLightMsg() {
        return pKBLightMsg;
    }
}
