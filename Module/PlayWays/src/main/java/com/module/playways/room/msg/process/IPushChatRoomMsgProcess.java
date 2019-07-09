package com.module.playways.room.msg.process;

public interface IPushChatRoomMsgProcess<T, M> {
    /**
     * 处理房间内消息
     *
     * @param messageType
     * @param msg
     */
    void processRoomMsg(T messageType, M msg);

    /**
     * 该processor需要监听的消息类型
     *
     * @return
     */
    T[] acceptType();
}
