package com.module.rankingmode.msg.event;

public class CommentMsgEvent {

    public final static int MSG_TYPE_SEND = 0;
    public final static int MSG_TYPE_RECE = 1;

    int type = MSG_TYPE_RECE;
    String text;


    public CommentMsgEvent(int type, String text){
        this.type = type;
        this.text = text;
    }
}
