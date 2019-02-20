package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.zq.live.proto.Room.PKBLightMsg;

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
