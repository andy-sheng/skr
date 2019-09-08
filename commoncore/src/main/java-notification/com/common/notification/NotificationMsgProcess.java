package com.common.notification;

import com.common.log.MyLog;
import com.module.msg.CustomMsgType;
import com.module.msg.IPushMsgProcess;
import com.zq.live.proto.Notification.NotificationMsg;
import com.zq.live.proto.broadcast.RoomBroadcastMsg;

import java.io.IOException;

public class NotificationMsgProcess implements IPushMsgProcess {

    public final String TAG = "NotificationMsgProcess";


    @Override
    public void process(int messageType, byte[] data) {
        MyLog.d(TAG, "process" + " messageType=" + messageType + " data=" + data);
        switch (messageType) {
            case CustomMsgType.MSG_TYPE_NOTIFICATION:
                processNotificationMsg(data);
                break;
                case CustomMsgType.MSG_TYPE_BROADCAST:
                    processBroadcastMsg(data);
                    break;
        }
    }

    @Override
    public int[] acceptType() {
        return new int[]{
                CustomMsgType.MSG_TYPE_NOTIFICATION,
                CustomMsgType.MSG_TYPE_BROADCAST

        };
    }

    // 处理通知消息
    private void processNotificationMsg(byte[] data) {
        try {
            NotificationMsg msg = NotificationMsg.parseFrom(data);

            if (msg == null){
                MyLog.e(TAG, "processRoomMsg" + " msg == null ");
                return;
            }

            NotificationPushManager.getInstance().processNotificationMsg(msg);
        } catch (IOException e) {
            MyLog.e(e);
        }
    }

    // 处理广播消息
    private void processBroadcastMsg(byte[] data) {
        try {
            RoomBroadcastMsg msg = RoomBroadcastMsg.parseFrom(data);
            if (msg == null){
                MyLog.e(TAG, "processRoomMsg" + " msg == null ");
                return;
            }

            NotificationPushManager.getInstance().processBroadcastMsg(msg);
        } catch (IOException e) {
            MyLog.e(e);
        }
    }
}
