package com.mi.liveassistant.barrage.processor;

import com.mi.liveassistant.barrage.data.Message;

import java.util.List;

/**
 * 消息分发器
 *
 * Created by wuxiaoshan on 17-5-8.
 */
public interface IMsgDispenser {

    void addChatMsg(List<Message> messageList);

    void addChatMsg(Message message);

    void addSysMsg(List<Message> messageList);

    void addInternalMsgListener(List<Message> messageList);
}
