package com.module.playways.doubleplay.event;

public class UpdateNoLimitDuraionEvent {
    boolean enableNoLimitDuration;

    public boolean isEnableNoLimitDuration() {
        return enableNoLimitDuration;
    }

    public UpdateNoLimitDuraionEvent(boolean enableNoLimitDuration) {
        this.enableNoLimitDuration = enableNoLimitDuration;
    }
}
