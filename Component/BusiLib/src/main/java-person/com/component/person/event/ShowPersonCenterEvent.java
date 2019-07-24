package com.component.person.event;

public class ShowPersonCenterEvent {

    private int uid;

    public ShowPersonCenterEvent(int uid) {
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }
}
