package com.module.msg;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public interface IPushMsgProcess {
    /**
     * 处理消息
     * @param messageType
     * @param jsonObject
     */
    void process(int messageType, JSONObject jsonObject);

    /**
     * 该processor需要监听的消息类型
     * @return
     */
    int[] acceptType();
}
