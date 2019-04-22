package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.ChorusRoundInfoModel;

public class GrabGiveUpInChorusEvent {
    public ChorusRoundInfoModel mChorusRoundInfoModel;

    public GrabGiveUpInChorusEvent(ChorusRoundInfoModel infoModel) {
        this.mChorusRoundInfoModel = infoModel;
    }

    @Override
    public String toString() {
        return "GrabGiveUpInChorusEvent{" +
                "uid=" + mChorusRoundInfoModel +
                '}';
    }
}
