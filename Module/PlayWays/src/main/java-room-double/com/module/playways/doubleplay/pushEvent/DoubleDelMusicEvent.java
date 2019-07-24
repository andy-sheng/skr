package com.module.playways.doubleplay.pushEvent;

import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.CombineRoom.DelMusicInfoMsg;

public class DoubleDelMusicEvent {

    public BasePushInfo mBasePushInfo;
    public String uniqTag;  //唯一id

    public DoubleDelMusicEvent(BasePushInfo info, DelMusicInfoMsg delMusicInfoMsg) {
        this.mBasePushInfo = info;
        this.uniqTag = delMusicInfoMsg.getUniqTag();
    }
}
