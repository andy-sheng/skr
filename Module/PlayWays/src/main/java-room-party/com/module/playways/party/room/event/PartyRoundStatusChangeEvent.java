package com.module.playways.party.room.event;


import com.module.playways.party.room.model.PartyRoundInfoModel;

public class PartyRoundStatusChangeEvent {
    public int oldStatus;
    public PartyRoundInfoModel roundInfo;

    public PartyRoundStatusChangeEvent(PartyRoundInfoModel roundInfo, int oldStatus) {
        this.roundInfo = roundInfo;
        this.oldStatus = oldStatus;
    }

    @Override
    public String toString() {
        return "PartyRoundStatusChangeEvent{" +
                "oldStatus=" + oldStatus +
                ", roundInfo=" + roundInfo +
                '}';
    }
}
