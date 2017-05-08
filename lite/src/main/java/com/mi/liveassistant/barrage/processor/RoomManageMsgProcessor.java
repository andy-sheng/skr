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
public class RoomManageMsgProcessor extends MsgProcessor {

    private static final String TAG = RoomManageMsgProcessor.class.getSimpleName();

    public RoomManageMsgProcessor(IMsgDispenser msgDispenser) {
        super(msgDispenser);
    }

    @Override
    public void process(BarrageMsg msg, String roomId) {
        if (msg == null || !roomId.equals(msg.getRoomId())) {
            return;
        }
        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_FORBIDDEN) {
            BarrageMsg.ForbiddenMsgExt ext = (BarrageMsg.ForbiddenMsgExt) msg.getMsgExt();
            MyLog.d(TAG, "forbiddenUserId:" + ext.forbiddenUserId);
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_CANCEL_FORBIDDEN) {
            BarrageMsg.ForbiddenMsgExt ext = (BarrageMsg.ForbiddenMsgExt) msg.getMsgExt();
            MyLog.d(TAG, "cancelforbiddenUserId:" + ext.forbiddenUserId);
        }
        List<Message> messageList = new ArrayList<>();
        messageList.add(Message.loadFromBarrage(msg));
        mIMsgDispenser.addSysMsg(messageList);
        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_KICK_VIEWER) {
            mIMsgDispenser.addInternalMsgCallBack(messageList);
        }
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_FORBIDDEN,
                BarrageMsgType.B_MSG_TYPE_CANCEL_FORBIDDEN,
                BarrageMsgType.B_MSG_TYPE_CANCEL_MANAGER,
                BarrageMsgType.B_MSG_TYPE_SET_MANAGER,
                BarrageMsgType.B_MSG_TYPE_FREQUENCY_CONTROL,
                BarrageMsgType.B_MSG_TYPE_KICK_VIEWER
        };
    }
}
