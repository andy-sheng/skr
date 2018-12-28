package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;

public class SpecialEmojiMsgEvent {
    public final static int MSG_TYPE_SEND = 0;
    public final static int MSG_TYPE_RECE = 1;

    int type = MSG_TYPE_RECE;
    int emojiId;

    BasePushInfo info;

    public SpecialEmojiMsgEvent(BasePushInfo info, int type, int emojiId) {
        this.type = type;
        this.emojiId = emojiId;
        this.info = info;
    }

}
