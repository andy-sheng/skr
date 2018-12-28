package com.module.playways.rank.msg.process;

import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.RoomMsg;

public interface IPushChatRoomMsgProcess {
    /**
     * 处理房间内消息
     * @param messageType
     * @param msg
     */
    void processRoomMsg(ERoomMsgType messageType, RoomMsg msg);

    /**
     * 该processor需要监听的消息类型
     *
     * @return
     */
    ERoomMsgType[] acceptType();
}
