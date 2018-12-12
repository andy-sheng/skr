package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;

public class CommentMsgEvent {

    public final static int MSG_TYPE_SEND = 0;
    public final static int MSG_TYPE_RECE = 1;

    int type = MSG_TYPE_RECE;
    public String text;
    public BasePushInfo info;

    public CommentMsgEvent(BasePushInfo info, int type, String text) {
        this.info = info;
        this.type = type;
        this.text = text;
    }
}
