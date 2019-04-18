package com.module.playways.room.msg.event;

import com.module.playways.room.msg.BasePushInfo;
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

    public PKMLightMsg getPKMLightMsg() {
        return pKMLightMsg;
    }
}
