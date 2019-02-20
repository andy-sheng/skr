package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.zq.live.proto.Room.PKBLightMsg;
import com.zq.live.proto.Room.PKMLightMsg;

public class PkLightOffMsgEvent {
    BasePushInfo info;
    PKMLightMsg pKMLightMsg;
    public PkLightOffMsgEvent(BasePushInfo basePushInfo, PKMLightMsg pkmLightMsg){
        info = basePushInfo;
        pKMLightMsg = pkmLightMsg;
    }

    public BasePushInfo getInfo() {
        return info;
    }

    public PKMLightMsg getpKMLightMsg() {
        return pKMLightMsg;
    }
}
