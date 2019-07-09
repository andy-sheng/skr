package com.zq.person.event;

public class ShowPersonCenterEvent {

    private int uid;

    public ShowPersonCenterEvent(int uid) {
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }
}
