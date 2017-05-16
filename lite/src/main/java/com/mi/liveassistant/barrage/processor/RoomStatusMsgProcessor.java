package com.mi.liveassistant.barrage.processor;

import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.model.BarrageMsg;
import com.mi.liveassistant.barrage.model.BarrageMsgType;
import com.mi.liveassistant.common.log.MyLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuxiaoshan on 17-5-8.
 */
public class RoomStatusMsgProcessor extends MsgProcessor {

    private static final String TAG = RoomStatusMsgProcessor.class.getSimpleName();

    public RoomStatusMsgProcessor(IMsgDispenser msgDispenser){
        super(msgDispenser);
    }

    @Override
    public void process(BarrageMsg msg, String roomId) {
        if(msg == null || !roomId.equals(msg.getRoomId())){
            return;
        }
        List<Message> messageList = new ArrayList<>();
        messageList.add(Message.loadFromBarrage(msg));
        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_LIVE_END) {
            // 直播结束
            MyLog.w(TAG, "B_MSG_TYPE_LIVE_END");
            mIMsgDispenser.addInternalMsgListener(messageList);
            mIMsgDispenser.addSysMsg(messageList);
        }  else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ANCHOR_LEAVE) {
            MyLog.w(TAG, "B_MSG_TYPE_ANCHOR_LEAVE");
            mIMsgDispenser.addSysMsg(messageList);
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ANCHOR_JOIN) {
            MyLog.w(TAG, "B_MSG_TYPE_ANCHOR_JOIN");
            mIMsgDispenser.addSysMsg(messageList);
        }
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_LIVE_END,
                BarrageMsgType.B_MSG_TYPE_ANCHOR_LEAVE,
                BarrageMsgType.B_MSG_TYPE_ANCHOR_JOIN
        };
    }
}
