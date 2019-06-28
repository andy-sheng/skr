package com.module.playways.grab.room.songmanager.model;

import com.component.busilib.friends.SpecialModel;

public class ChangeTagSuccessEvent {
    SpecialModel specialModel;

    public SpecialModel getSpecialModel() {
        return specialModel;
    }

    public ChangeTagSuccessEvent(SpecialModel specialModel) {
        this.specialModel = specialModel;
    }
}
