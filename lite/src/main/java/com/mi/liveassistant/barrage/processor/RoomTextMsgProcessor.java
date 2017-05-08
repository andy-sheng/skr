package com.mi.liveassistant.barrage.processor;

import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.model.BarrageMsg;
import com.mi.liveassistant.barrage.model.BarrageMsgType;

/**
 * Created by wuxiaoshan on 17-5-8.
 */
public class RoomTextMsgProcessor extends MsgProcessor {

    public RoomTextMsgProcessor(IMsgDispenser msgDispenser){
        super(msgDispenser);
    }

    @Override
    public void process(BarrageMsg msg, String roomId) {
        if(msg == null || !roomId.equals(msg.getRoomId())){
            return;
        }
        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_TEXT
                || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ANIM
                || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_SHARE
                || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_KICK_VIEWER_BARRAGE) {

            mIMsgDispenser.addChatMsg(Message.loadFromBarrage(msg));
        }
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_TEXT,
                BarrageMsgType.B_MSG_TYPE_ANIM,
                BarrageMsgType.B_MSG_TYPE_SHARE,
                BarrageMsgType.B_MSG_TYPE_KICK_VIEWER_BARRAGE
        };
    }
}
