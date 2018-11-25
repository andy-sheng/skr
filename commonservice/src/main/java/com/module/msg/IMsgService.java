package com.module.msg;

import com.alibaba.android.arouter.facade.template.IProvider;
import com.alibaba.fastjson.JSONObject;
import com.module.common.ICallback;

public interface IMsgService extends IProvider {
    void joinChatRoom(String roomId, ICallback callback);
    void leaveChatRoom(String roomId);

    /**
     * 通过融云发送聊天室消息，不经过 AppServer
     * @param roomId
     * @param messageType
     * @param contentJson
     * @param callback
     */
    void sendChatRoomMessage(String roomId, int messageType, JSONObject contentJson, ICallback callback);

    /**
     * 其他module设置自己的push处理模块
     * @param processor
     */
    void addMsgProcessor(IPushMsgProcess processor);
}
