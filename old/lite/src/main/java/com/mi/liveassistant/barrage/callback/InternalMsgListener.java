package com.mi.liveassistant.barrage.callback;

import com.mi.liveassistant.barrage.data.Message;

import java.util.List;

/**
 * Created by wuxiaoshan on 17-5-8.
 */
public interface InternalMsgListener {

    void handleMessage(List<Message> messageList);
}
