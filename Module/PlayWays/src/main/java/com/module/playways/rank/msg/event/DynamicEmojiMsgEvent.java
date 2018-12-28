package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;

public class DynamicEmojiMsgEvent {
    public final static int MSG_TYPE_SEND = 0;
    public final static int MSG_TYPE_RECE = 1;

    int type = MSG_TYPE_RECE;
    int emojiId;
    BasePushInfo info;

    public DynamicEmojiMsgEvent(BasePushInfo info, int type, int emojiId){
        this.info = info;
        this.type = type;
        this.emojiId = emojiId;
    }
}
