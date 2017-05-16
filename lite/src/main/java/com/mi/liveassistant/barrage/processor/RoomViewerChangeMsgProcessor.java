package com.mi.liveassistant.barrage.processor;

import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.model.BarrageMsg;
import com.mi.liveassistant.barrage.model.BarrageMsgType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuxiaoshan on 17-5-8.
 */
public class RoomViewerChangeMsgProcessor extends MsgProcessor {

    public RoomViewerChangeMsgProcessor(IMsgDispenser msgDispenser) {
        super(msgDispenser);
    }

    @Override
    public void process(BarrageMsg msg, String roomId) {
        if (msg == null || !roomId.equals(msg.getRoomId())) {
            return;
        }
        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_JOIN
                || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_LEAVE
                || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_VIEWER_CHANGE
                || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_TOP_GET
                || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_TOP_LOSE) {
            List<Message> messageList = new ArrayList<>();
            messageList.add(Message.loadFromBarrage(msg));
            mIMsgDispenser.addInternalMsgListener(messageList);
        }
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_JOIN,
                BarrageMsgType.B_MSG_TYPE_LEAVE,
                BarrageMsgType.B_MSG_TYPE_VIEWER_CHANGE,
                BarrageMsgType.B_MSG_TYPE_TOP_GET,
                BarrageMsgType.B_MSG_TYPE_TOP_LOSE,
        };
    }
}
