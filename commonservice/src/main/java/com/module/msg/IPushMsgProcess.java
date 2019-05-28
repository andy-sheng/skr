package com.module.msg;

public interface IPushMsgProcess {
    /**
     * 处理消息(PB形式)
     * @param messageType
     * @param data
     */
    void process(int messageType, byte[] data);

    /**
     * 该processor需要监听的消息类型
     * @return
     */
    int[] acceptType();
}
