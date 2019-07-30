package com.module.feeds.detail.event;

public class AddCommentEvent {
    int commendID;

    public int getCommendID() {
        return commendID;
    }

    public AddCommentEvent(int commendID) {
        this.commendID = commendID;
    }
}
