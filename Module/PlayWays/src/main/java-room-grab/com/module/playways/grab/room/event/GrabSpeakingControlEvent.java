package com.module.playways.grab.room.event;

import com.module.playways.grab.room.model.GrabRoundInfoModel;

public class GrabSpeakingControlEvent {
    public boolean speaking =false;

    public GrabSpeakingControlEvent(boolean speaking) {
        this.speaking = speaking;
    }
}
