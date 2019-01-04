package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.zq.live.proto.Room.SpecialEmojiMsgType;

public class SpecialEmojiMsgEvent {
    public final static int MSG_TYPE_SEND = 0;
    public final static int MSG_TYPE_RECE = 1;

    int type = -1;
    SpecialEmojiMsgType emojiType = SpecialEmojiMsgType.SP_EMOJI_TYPE_UNKNOWN;
    String action;
    int count;

    BasePushInfo info;

    public SpecialEmojiMsgEvent(BasePushInfo info, int type, SpecialEmojiMsgType emojiType, String action, int count) {
        this.type = type;
        this.info = info;
        this.emojiType = emojiType;
        this.action = action;
        this.count = count;
    }

}
