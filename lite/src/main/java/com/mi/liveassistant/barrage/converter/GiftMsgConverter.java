package com.mi.liveassistant.barrage.converter;


import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.model.BarrageMsg;
import com.mi.liveassistant.barrage.model.BarrageMsgType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuxiaoshan on 17-5-3.
 */
public class GiftMsgConverter implements IConverter {
    @Override
    public List<Message> barrageConvert(BarrageMsg barrageMsg) {
        return null;
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_GIFT,
                BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE,
                BarrageMsgType.B_MSG_TYPE_RED_ENVELOPE,
                BarrageMsgType.B_MSG_TYPE_ROOM_BACKGROUND_GIFT,
                BarrageMsgType.B_MSG_TYPE_LIGHT_UP_GIFT,
                BarrageMsgType.B_MSG_TYPE_GLABAL_MSG
        };
    }
}
