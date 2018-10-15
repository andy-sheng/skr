package com.mi.liveassistant.unity;

import com.mi.liveassistant.barrage.data.Message;

/**
 * Created by zyh on 2017/5/18.
 */
public interface IBarrageListener {

    /**
     * 文本消息回调
     *
     * @param msg 文本弹幕消息
     */
    void onChatMsg(Message msg);

    /**
     * 系统消息回调
     *
     * @param msg 系统控制弹幕消息
     */
    void onSysMsg(Message msg);
}
