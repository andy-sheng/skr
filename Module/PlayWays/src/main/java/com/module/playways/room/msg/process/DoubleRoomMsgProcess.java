package com.module.playways.room.msg.process;

import com.common.log.MyLog;
import com.module.msg.CustomMsgType;
import com.module.msg.IPushMsgProcess;
import com.module.playways.room.msg.manager.DoubleRoomMsgManager;
import com.zq.live.proto.CombineRoom.CombineRoomMsg;

import java.io.IOException;

public class DoubleRoomMsgProcess implements IPushMsgProcess {
    public final static String TAG = "ChatRoomMsgProcess";

    @Override
    public void process(int messageType, byte[] data) {
        MyLog.d(TAG, "process" + " messageType=" + messageType + " data=" + data);
        switch (messageType) {
            case CustomMsgType.MSG_TYPE_COMBINE_ROOM:
                processRoomMsg(data);
                break;
        }

    }

    @Override
    public int[] acceptType() {
        return new int[]{
                CustomMsgType.MSG_TYPE_COMBINE_ROOM
        };
    }

    // 处理房间消息
    private void processRoomMsg(byte[] data) {
        try {
            CombineRoomMsg msg = CombineRoomMsg.parseFrom(data);

            if (msg == null) {
                MyLog.e(TAG, "processRoomMsg" + " msg == null ");
                return;
            }

            DoubleRoomMsgManager.getInstance().processRoomMsg(msg);
        } catch (IOException e) {
            MyLog.e(e);
        }

    }
}
