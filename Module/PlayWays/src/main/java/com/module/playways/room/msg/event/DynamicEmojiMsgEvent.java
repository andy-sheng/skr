package com.module.playways.room.msg.event;

import com.module.playways.grab.room.dynamicmsg.DynamicModel;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.Room.DynamicEmojiMsg;

public class DynamicEmojiMsgEvent {
    public final static int MSG_TYPE_SEND = 0;
    public final static int MSG_TYPE_RECE = 1;

    public int type = MSG_TYPE_RECE;
    public DynamicModel mDynamicModel;
    public BasePushInfo info;

    public DynamicEmojiMsgEvent(BasePushInfo info, int type, DynamicEmojiMsg dynamicEmojiMsg) {
        this.info = info;
        this.type = type;
        this.mDynamicModel = DynamicModel.parse(dynamicEmojiMsg);
    }

    public DynamicEmojiMsgEvent(BasePushInfo info, int type, DynamicModel mDynamicModel) {
        this.info = info;
        this.type = type;
        this.mDynamicModel = mDynamicModel;
    }

    @Override
    public String toString() {
        return "DynamicEmojiMsgEvent{" +
                "type=" + type +
                ", mDynamicModel=" + mDynamicModel +
                ", info=" + info +
                '}';
    }
}
