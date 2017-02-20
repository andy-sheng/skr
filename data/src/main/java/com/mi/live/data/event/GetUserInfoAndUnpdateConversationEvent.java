package com.mi.live.data.event;

/**
 * Created by yurui on 2016/12/2.
 */

public class GetUserInfoAndUnpdateConversationEvent {
    public long uuid;
    public boolean isBlock;
    public int foucusStaus;
    public int certificationType;
    public String nickName;

    public GetUserInfoAndUnpdateConversationEvent(long uuid, boolean isBlock, int focusStatus, int certificationType, String nickName) {
        this.uuid = uuid;
        this.isBlock = isBlock;
        this.foucusStaus = focusStatus;
        this.certificationType = certificationType;
        this.nickName = nickName;
    }
}
