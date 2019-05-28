package com.module.playways.room.msg.manager;

import com.common.log.MyLog;
import com.module.playways.room.msg.filter.PushMsgFilter;
import com.module.playways.room.msg.process.IPushChatRoomMsgProcess;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.RoomMsg;

import java.util.HashMap;
import java.util.HashSet;

/**
 * 处理所有的RoomMsg
 */
public class ChatRoomMsgManager {

    public final static String TAG = "ChatRoomMsgManager";

    /**
     * 消息类型-->消息处理器的映射
     */
    private HashMap<ERoomMsgType, HashSet<IPushChatRoomMsgProcess>> mProcessorMap = new HashMap<>();

    private HashSet<PushMsgFilter> mPushMsgFilterList = new HashSet<>();

    private static class ChatRoomMsgAdapterHolder {
        private static final ChatRoomMsgManager INSTANCE = new ChatRoomMsgManager();
    }

    private ChatRoomMsgManager() {

    }

    public static final ChatRoomMsgManager getInstance() {
        return ChatRoomMsgAdapterHolder.INSTANCE;
    }

    public synchronized void addChatRoomMsgProcessor(IPushChatRoomMsgProcess processor) {
        MyLog.d(TAG, "addChatRoomMsgProcessor" + " processor=" + processor);
        for (ERoomMsgType msgType : processor.acceptType()) {
            HashSet<IPushChatRoomMsgProcess> processorSet = mProcessorMap.get(msgType);
            if (processorSet == null) {
                processorSet = new HashSet<>();
                mProcessorMap.put(msgType, processorSet);
            }
            processorSet.add(processor);
        }
    }

    public void addFilter(PushMsgFilter pushMsgFilter) {
        mPushMsgFilterList.add(pushMsgFilter);
    }

    public void removeFilter(PushMsgFilter pushMsgFilter) {
        mPushMsgFilterList.remove(pushMsgFilter);
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
