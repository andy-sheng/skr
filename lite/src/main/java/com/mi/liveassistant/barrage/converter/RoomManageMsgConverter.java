package com.mi.liveassistant.barrage.converter;

import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.model.BarrageMsg;
import com.mi.liveassistant.barrage.model.BarrageMsgType;

import java.util.List;

/**
 * Created by wuxiaoshan on 17-5-3.
 */
public class RoomManageMsgConverter implements IConverter {
    @Override
    public List<Message> barrageConvert(BarrageMsg barrageMsg) {
        return null;
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_FORBIDDEN,
                BarrageMsgType.B_MSG_TYPE_CANCEL_FORBIDDEN,
                BarrageMsgType.B_MSG_TYPE_CANCEL_MANAGER,
                BarrageMsgType.B_MSG_TYPE_SET_MANAGER,
                BarrageMsgType.B_MSG_TYPE_FREQUENCY_CONTROL,
                BarrageMsgType.B_MSG_TYPE_KICK_VIEWER
        };
    }
}
