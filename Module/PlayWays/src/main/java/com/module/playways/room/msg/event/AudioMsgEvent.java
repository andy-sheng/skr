package com.module.playways.room.msg.event;

import com.module.playways.room.msg.BasePushInfo;
import com.component.live.proto.Room.AudioMsg;

public class AudioMsgEvent {

    public final static int MSG_TYPE_SEND = 0;
    public final static int MSG_TYPE_RECE = 1;

    public int type = MSG_TYPE_RECE;
    public BasePushInfo mInfo;
    public String localPath;
    public long duration;
    public String msgUrl;

    public AudioMsgEvent(BasePushInfo info, int type, AudioMsg audioMsg) {
        this.type = type;
        this.mInfo = info;
        localPath = "";
        msgUrl = audioMsg.getMsgUrl();
        duration = audioMsg.getDuration();
    }

    public AudioMsgEvent(BasePushInfo info, int type, String localPath, long duration, String msgUrl) {
        this.type = type;
        this.mInfo = info;
        this.localPath = localPath;
        this.duration = duration;
        this.msgUrl = msgUrl;
    }

}
