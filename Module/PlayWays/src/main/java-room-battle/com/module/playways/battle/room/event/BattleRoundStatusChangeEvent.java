package com.module.playways.battle.room.event;


import com.module.playways.battle.room.model.BattleRoundInfoModel;

public class BattleRoundStatusChangeEvent {
    public int oldStatus;
    public BattleRoundInfoModel roundInfo;

    public BattleRoundStatusChangeEvent(BattleRoundInfoModel roundInfo, int oldStatus) {
        this.roundInfo = roundInfo;
        this.oldStatus = oldStatus;
    }

    @Override
    public String toString() {
        return "BattleRoundStatusChangeEvent{" +
                "oldStatus=" + oldStatus +
                ", roundInfo=" + roundInfo +
                '}';
    }
}
