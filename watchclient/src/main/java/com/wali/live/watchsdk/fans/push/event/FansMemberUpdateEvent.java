package com.wali.live.watchsdk.fans.push.event;

/**
 * Created by zyh on 2017/11/22.
 */
public class FansMemberUpdateEvent {
    public static final int BE_MANAGER_TYPE = 0;
    public static final int CANCEL_BE_MANAGER_TYPE = 1;
    public static final int BE_MEMBER = 2;
    public static final int CANCEL_BE_MEMBER = 3;

    public long vfansId;
    public int type;

    public FansMemberUpdateEvent(long vfansId, int type) {
        this.vfansId = vfansId;
        this.type = type;
    }
}
