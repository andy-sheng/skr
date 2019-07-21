package com.module.playways.room.msg.manager;

import com.common.log.MyLog;
import com.module.playways.room.msg.filter.PushMsgFilter;
import com.module.playways.room.msg.process.IPushChatRoomMsgProcess;
import com.zq.live.proto.CombineRoom.CombineRoomMsg;
import com.zq.live.proto.CombineRoom.ECombineRoomMsgType;

import java.util.HashSet;

/**
 * 处理所有的RoomMsg
 */
public class DoubleRoomMsgManager extends BaseMsgManager<ECombineRoomMsgType, CombineRoomMsg> {

    public final String TAG = "DoubleRoomMsgManager";

    private static class ChatRoomMsgAdapterHolder {
        private static final DoubleRoomMsgManager INSTANCE = new DoubleRoomMsgManager();
    }

    private DoubleRoomMsgManager() {

    }

    public static final DoubleRoomMsgManager getInstance() {
        return ChatRoomMsgAdapterHolder.INSTANCE;
    }


    /**
     * 处理消息分发
     *
     * @param msg
     */
    public void processRoomMsg(CombineRoomMsg msg) {
        boolean canGo = true;  //是否放行的flag
        for (PushMsgFilter filter : mPushMsgFilterList) {
            canGo = filter.doFilter(msg);
            if (!canGo) {
                MyLog.d(TAG, "processRoomMsg " + msg + "被拦截");
                return;
            }
        }

        HashSet<IPushChatRoomMsgProcess> processors = mProcessorMap.get(msg.getMsgType());
        if (processors != null) {
            for (IPushChatRoomMsgProcess process : processors) {
                process.processRoomMsg(msg.getMsgType(), msg);
            }
        }
    }

}
