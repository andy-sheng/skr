package com.module.playways.room.msg.manager;

import com.common.log.MyLog;
import com.module.playways.room.msg.filter.PushMsgFilter;
import com.module.playways.room.msg.process.IPushChatRoomMsgProcess;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.RoomMsg;

import java.util.HashSet;

/**
 * 处理所有的RoomMsg
 */
public class ChatRoomMsgManager extends BaseMsgManager<ERoomMsgType, RoomMsg> {

    public final static String TAG = "ChatRoomMsgManager";

    private static class ChatRoomMsgAdapterHolder {
        private static final ChatRoomMsgManager INSTANCE = new ChatRoomMsgManager();
    }

    private ChatRoomMsgManager() {

    }

    public static final ChatRoomMsgManager getInstance() {
        return ChatRoomMsgAdapterHolder.INSTANCE;
    }

    /**
     * 处理消息分发
     *
     * @param msg
     */
    public void processRoomMsg(RoomMsg msg) {
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
