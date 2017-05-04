package com.mi.liveassistant.barrage.converter;

import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.model.BarrageMsg;
import com.mi.liveassistant.barrage.model.BarrageMsgType;

import java.util.List;

/**
 * Created by wuxiaoshan on 17-5-3.
 */
public class RoomSystemMsgConverter implements IConverter {
    @Override
    public List<Message> barrageConvert(BarrageMsg barrageMsg) {
        return null;
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_ROOM_SYS_MSG,
                BarrageMsgType.B_MSG_TYPE_GLOBAL_SYS_MSG,
                BarrageMsgType.B_MSG_TYPE_ROOM_FOUCES_ANCHOR,
                BarrageMsgType.B_MSG_TYPE_COMMEN_SYS_MSG,
        };
    }
}
