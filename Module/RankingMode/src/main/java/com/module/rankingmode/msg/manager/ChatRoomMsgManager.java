package com.module.rankingmode.msg.manager;

import com.common.log.MyLog;
import com.module.rankingmode.msg.process.IPushChatRoomMsgProcess;
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

    /**
     * 处理消息分发
     *
     * @param msgType
     * @param msg
     */
    public void process(ERoomMsgType msgType, RoomMsg msg) {
        HashSet<IPushChatRoomMsgProcess> processors = mProcessorMap.get(msgType);
        if (processors != null) {
            for (IPushChatRoomMsgProcess process : processors) {
                process.processRoomMsg(msgType, msg);
            }
        }
    }
}
