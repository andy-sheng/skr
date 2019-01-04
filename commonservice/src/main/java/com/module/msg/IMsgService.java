package com.module.msg;

import android.app.Application;
import android.content.Context;

import com.alibaba.android.arouter.facade.template.IProvider;
import com.alibaba.fastjson.JSONObject;
import com.module.common.ICallback;

public interface IMsgService extends IProvider {

    /**
     * 初始化融云
     */
    void initRongIM(Application application);

    /**
     * 与融云服务器建立连接
     *
     * @param token
     * @param callback
     */
    void connectRongIM(String token, ICallback callback);

    void disconnect();

    void logout();

    /**
     * 加入融云聊天室
     *
     * @param roomId
     * @param callback
     */
    void joinChatRoom(String roomId, ICallback callback);

    void leaveChatRoom(String roomId);

    /**
     * 通过融云发送聊天室消息
     *
     * @param roomId
     * @param messageType
     * @param contentJson
     * @param callback
     */
    void sendChatRoomMessage(String roomId, int messageType, JSONObject contentJson, ICallback callback);

    /**
     * 其他module设置自己的push处理模块
     *
     * @param processor
     */
    void addMsgProcessor(IPushMsgProcess processor);

    IMessageFragment getMessageFragment();

    /**
     * 打开私聊页面
     * @param context
     * @param targetId
     * @param title
     */
    void startPrivateChat(Context context, String targetId, String title);
}
