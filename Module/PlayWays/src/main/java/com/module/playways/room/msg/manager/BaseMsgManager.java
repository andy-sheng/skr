package com.module.playways.room.msg.manager;

import com.common.log.MyLog;
import com.module.playways.room.msg.filter.PushMsgFilter;
import com.module.playways.room.msg.process.IPushChatRoomMsgProcess;

import java.util.HashMap;
import java.util.HashSet;

public abstract class BaseMsgManager<T, M> {
    public final static String TAG = "BaseMsgManager";

    /**
     * 消息类型-->消息处理器的映射
     */
    protected HashMap<T, HashSet<IPushChatRoomMsgProcess>> mProcessorMap = new HashMap<>();

    protected HashSet<PushMsgFilter> mPushMsgFilterList = new HashSet<>();

    public synchronized void addChatRoomMsgProcessor(IPushChatRoomMsgProcess processor) {
        MyLog.d(TAG, "addChatRoomMsgProcessor" + " processor=" + processor);
        for (Object msgType : processor.acceptType()) {
            HashSet<IPushChatRoomMsgProcess> processorSet = mProcessorMap.get(msgType);
            if (processorSet == null) {
                processorSet = new HashSet<>();
                mProcessorMap.put((T) msgType, processorSet);
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
    public abstract void processRoomMsg(M msg);
}
