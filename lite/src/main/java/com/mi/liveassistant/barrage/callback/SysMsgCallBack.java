package com.mi.liveassistant.barrage.callback;

import com.mi.liveassistant.barrage.data.Message;

import java.util.List;

/**
 * 系统消息，包括：房间系统消息和全局系统消息
 *
 * Created by wuxiaoshan on 17-5-5.
 */
public interface SysMsgCallBack{
    void handleMessage(List<Message> messageList);
}
