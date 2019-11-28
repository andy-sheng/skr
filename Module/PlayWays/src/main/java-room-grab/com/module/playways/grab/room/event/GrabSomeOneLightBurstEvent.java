package com.module.playways.grab.room.event;

import com.component.busilib.model.EffectModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;

public class GrabSomeOneLightBurstEvent {
    public GrabRoundInfoModel roundInfo;
    public int uid;
    public EffectModel bLightEffectModel;

    public GrabSomeOneLightBurstEvent(int uid, GrabRoundInfoModel newRoundInfo, EffectModel bLightEffectModel) {
        this.uid = uid;
        this.roundInfo = newRoundInfo;
        this.bLightEffectModel = bLightEffectModel;
    }

    public GrabRoundInfoModel getRoundInfo() {
        return roundInfo;
    }
}
