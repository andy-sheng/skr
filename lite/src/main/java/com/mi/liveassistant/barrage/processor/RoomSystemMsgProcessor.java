package com.mi.liveassistant.barrage.processor;

import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.model.BarrageMsg;
import com.mi.liveassistant.barrage.model.BarrageMsgType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuxiaoshan on 17-5-8.
 */
public class RoomSystemMsgProcessor extends MsgProcessor {

    private static final String TAG = RoomStatusMsgProcessor.class.getSimpleName();

    public RoomSystemMsgProcessor(IMsgDispenser msgDispenser){
        super(msgDispenser);
    }

    @Override
    public void process(BarrageMsg msg, String roomId) {
        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_GLOBAL_SYS_MSG || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_COMMEN_SYS_MSG) {
            //处理全局系统消息
            BarrageMsg.GlobalMessageExt globalMessageExt = (BarrageMsg.GlobalMessageExt) msg.getMsgExt();
            if (globalMessageExt != null) {
                List<BarrageMsg> barrageMsgs = globalMessageExt.getSysBarrageMsg(msg);
                List<Message> messageList = new ArrayList<>();
                for(BarrageMsg barrageMsg:barrageMsgs){
                    messageList.add(Message.loadChatMsgFromBarrage(barrageMsg));
                }
                mIMsgDispenser.addChatMsg(messageList);
            }
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ROOM_SYS_MSG) {
            //处理房间消息
            BarrageMsg.RoomMessageExt roomMessageExt = (BarrageMsg.RoomMessageExt) msg.getMsgExt();
            if (roomMessageExt != null) {
                List<BarrageMsg> barrageMsgs = roomMessageExt.getRoomBarrageMsg(msg);
                List<Message> messageList = new ArrayList<>();
                for(BarrageMsg barrageMsg:barrageMsgs){
                    messageList.add(Message.loadChatMsgFromBarrage(barrageMsg));
                }
                mIMsgDispenser.addChatMsg(messageList);
            }
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ROOM_FOUCES_ANCHOR) {
            if(msg == null || !roomId.equals(msg.getRoomId())){
                return;
            }
            mIMsgDispenser.addChatMsg(Message.loadChatMsgFromBarrage(msg));
        }
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_ROOM_SYS_MSG,
                BarrageMsgType.B_MSG_TYPE_GLOBAL_SYS_MSG,
                BarrageMsgType.B_MSG_TYPE_ROOM_FOUCES_ANCHOR,
                BarrageMsgType.B_MSG_TYPE_COMMEN_SYS_MSG,
        };
    }
}
