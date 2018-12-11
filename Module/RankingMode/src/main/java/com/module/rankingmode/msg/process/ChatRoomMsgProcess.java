package com.module.rankingmode.msg.process;

import com.common.log.MyLog;
import com.module.msg.CustomMsgType;
import com.module.msg.IPushMsgProcess;
import com.module.rankingmode.msg.manager.ChatRoomMsgManager;
import com.zq.live.proto.Room.RoomMsg;

import java.io.IOException;

public class ChatRoomMsgProcess implements IPushMsgProcess {
    public final static String TAG = "ChatRoomMsgProcess";

    @Override
    public void process(int messageType, byte[] data) {
        MyLog.d(TAG, "process" + " messageType=" + messageType + " data=" + data);
        switch (messageType) {
            case CustomMsgType.MSG_TYPE_ROOM:
                processRoomMsg(data);
                break;
        }

    }

    @Override
    public int[] acceptType() {
        return new int[]{
                CustomMsgType.MSG_TYPE_ROOM
        };
    }

    // 处理房间消息
    private void processRoomMsg(byte[] data) {
        try {
            RoomMsg msg = RoomMsg.parseFrom(data);

            if (msg == null){
                MyLog.e(TAG, "processRoomMsg" + " msg == null ");
                return;
            }

            ChatRoomMsgManager.getInstance().processRoomMsg(msg);
        } catch (IOException e) {
            MyLog.e(e);
        }

    }
}
