package com.module.playways.grab.room.event;

import com.component.busilib.model.BLightEffectModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;

public class GrabSomeOneLightBurstEvent {
    public GrabRoundInfoModel roundInfo;
    public int uid;
    public BLightEffectModel bLightEffectModel;

    public GrabSomeOneLightBurstEvent(int uid, GrabRoundInfoModel newRoundInfo, BLightEffectModel bLightEffectModel) {
        this.uid = uid;
        this.roundInfo = newRoundInfo;
        this.bLightEffectModel = bLightEffectModel;
    }

    public GrabRoundInfoModel getRoundInfo() {
        return roundInfo;
    }
}
