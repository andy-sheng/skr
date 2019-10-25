package com.module.playways.mic.room.event;

import com.module.playways.mic.room.model.ChorusRoundInfoModel;

public class MicChorusUserStatusChangeEvent {
    public ChorusRoundInfoModel mChorusRoundInfoModel;

    public MicChorusUserStatusChangeEvent(ChorusRoundInfoModel infoModel) {
        this.mChorusRoundInfoModel = infoModel;
    }

    @Override
    public String toString() {
        return "GrabGiveUpInChorusEvent{" +
                "uid=" + mChorusRoundInfoModel +
                '}';
    }
}
