package com.mi.liveassistant.barrage.processor;


import com.mi.liveassistant.barrage.model.BarrageMsg;

/**
 * Created by wuxiaoshan on 17-5-8.
 */
public abstract class MsgProcessor {

    protected IMsgDispenser mIMsgDispenser;

    public MsgProcessor(IMsgDispenser msgDispenser){
        mIMsgDispenser = msgDispenser;
    }

    public abstract void process(BarrageMsg msg, String roomId);

    public abstract int[] getAcceptMsgType();
}
