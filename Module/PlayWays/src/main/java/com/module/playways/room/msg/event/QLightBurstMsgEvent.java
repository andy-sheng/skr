// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Room.proto
package com.module.playways.room.msg.event;

import com.component.busilib.model.BLightEffectModel;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.GrabRoom.QBLightMsg;

public final class QLightBurstMsgEvent {
    public BasePushInfo info;
    /**
     * 用户id
     */
    public Integer userID;

    /**
     * 轮次顺序
     */
    public Integer roundSeq;

    public BLightEffectModel bLightEffectModel;


    public QLightBurstMsgEvent(BasePushInfo info, QBLightMsg msg) {
        this.info = info;
        this.userID = msg.getUserID();
        this.roundSeq = msg.getRoundSeq();
        this.bLightEffectModel = BLightEffectModel.Companion.parseBLightEffectModelFromPb(msg.getShowInfo());
    }

}
