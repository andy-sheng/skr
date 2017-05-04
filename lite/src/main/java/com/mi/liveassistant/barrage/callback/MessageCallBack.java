package com.mi.liveassistant.barrage.callback;

import com.mi.liveassistant.barrage.data.Message;

import java.util.List;

/**
 * Created by wuxiaoshan on 17-5-4.
 */
public interface MessageCallBack {

    void handleMessage(List<Message> message);
}
