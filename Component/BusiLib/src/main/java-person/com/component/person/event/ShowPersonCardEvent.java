package com.component.person.event;

public class ShowPersonCardEvent {
    private int uid;

    public ShowPersonCardEvent(int uid) {
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }
}
