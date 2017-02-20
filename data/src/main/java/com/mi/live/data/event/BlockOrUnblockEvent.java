package com.mi.live.data.event;

/**
 * Created by yurui on 2016/12/2.
 */

/**
 * 拉黑和取消拉黑的event
 */
public class BlockOrUnblockEvent {
    public int eventType;
    public long uuid;

    public static final int EVENT_TYPE_BLOCK = 1;

    public static final int EVENT_TYPE_UNBLOCK = 2;

    public BlockOrUnblockEvent(int type, long uid) {
        this.eventType = type;
        this.uuid = uid;
    }
}