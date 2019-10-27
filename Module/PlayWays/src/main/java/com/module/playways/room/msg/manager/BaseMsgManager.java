package com.module.playways.room.msg.manager;

import com.module.playways.room.msg.filter.PushMsgFilter;

import java.util.HashSet;

public abstract class BaseMsgManager<T, M> {
    public final String TAG = "BaseMsgManager";

    protected HashSet<PushMsgFilter> mPushMsgFilterList = new HashSet<>();

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
