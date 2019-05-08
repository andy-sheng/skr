package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.ChorusRoundInfoModel;

public class GrabChorusUserStatusChangeEvent {
    public ChorusRoundInfoModel mChorusRoundInfoModel;

    public GrabChorusUserStatusChangeEvent(ChorusRoundInfoModel infoModel) {
        this.mChorusRoundInfoModel = infoModel;
    }

    @Override
    public String toString() {
        return "GrabGiveUpInChorusEvent{" +
                "uid=" + mChorusRoundInfoModel +
                '}';
    }
}
