package com.module.rankingmode.msg.event;

public class DynamicEmojiMsgEvent {
    public final static int MSG_TYPE_SEND = 0;
    public final static int MSG_TYPE_RECE = 1;

    int type = MSG_TYPE_RECE;
    int emojiId;

    public DynamicEmojiMsgEvent(int type, int emojiId){
        this.type = type;
        this.emojiId = emojiId;
    }
}
