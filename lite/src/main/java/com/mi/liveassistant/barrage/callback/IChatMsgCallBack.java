package com.mi.liveassistant.barrage.callback;

import com.mi.liveassistant.barrage.data.Message;

import java.util.List;

/**
 * 文本消息
 * <p>
 * Created by wuxiaoshan on 17-5-3.
 */
public interface IChatMsgCallBack {

    void handleMessage(List<Message> messages);
}
