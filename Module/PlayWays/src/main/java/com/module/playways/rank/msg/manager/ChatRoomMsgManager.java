package com.module.playways.rank.msg.manager;

import com.common.log.MyLog;
import com.module.playways.rank.msg.filter.PushMsgFilter;
import com.module.playways.rank.msg.process.IPushChatRoomMsgProcess;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.RoomMsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * 处理所有的RoomMsg
 */
public class ChatRoomMsgManager {

    public final static String TAG = "ChatRoomMsgManager";

    /**
     * 消息类型-->消息处理器的映射
     */
    private HashMap<ERoomMsgType, HashSet<IPushChatRoomMsgProcess>> mProcessorMap = new HashMap<>();

    private List<PushMsgFilter> pushMsgFilterList = new ArrayList<>();

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
     * @param msg
     */
    public void processRoomMsg(RoomMsg msg) {
        boolean flag = true;  //是否放行的flag
        for (PushMsgFilter filter : pushMsgFilterList) {
            if (filter.processType() != null && filter.processType().contains(msg.getMsgType())) {
                flag = filter.doFilter(msg);
                if (!flag) {
                    break;
                }
            }
        }

        if (flag) {
            HashSet<IPushChatRoomMsgProcess> processors = mProcessorMap.get(msg.getMsgType());
            if (processors != null) {
                for (IPushChatRoomMsgProcess process : processors) {
                    process.processRoomMsg(msg.getMsgType(), msg);
                }
            }
        }
    }

}
