package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.zq.live.proto.Room.SpecialEmojiMsgType;

public class SpecialEmojiMsgEvent {
    public final static int MSG_TYPE_SEND = 0;
    public final static int MSG_TYPE_RECE = 1;

    public int type = -1;
    public SpecialEmojiMsgType emojiType = SpecialEmojiMsgType.SP_EMOJI_TYPE_UNKNOWN;
    public String action;
    public int count;

    public BasePushInfo info;

    public SpecialEmojiMsgEvent(BasePushInfo info) {
        this.info = info;
    }

    public SpecialEmojiMsgEvent(BasePushInfo info, int type, SpecialEmojiMsgType emojiType, String action, int count) {
        this.type = type;
        this.info = info;
        this.emojiType = emojiType;
        this.action = action;
        this.count = count;
    }


}
