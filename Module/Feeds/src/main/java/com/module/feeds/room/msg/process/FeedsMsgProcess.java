package com.module.feeds.room.msg.process;

import com.common.log.MyLog;
import com.module.msg.IPushMsgProcess;

public class FeedsMsgProcess implements IPushMsgProcess {
    public final static String TAG = "ChatRoomMsgProcess";

    @Override
    public void process(int messageType, byte[] data) {
        MyLog.d(TAG, "process" + " messageType=" + messageType + " data=" + data);
        switch (messageType) {
        }

    }

    @Override
    public int[] acceptType() {
        return new int[]{
        };
    }

    // 处理房间消息
    private void processRoomMsg(byte[] data) {

    }
}
