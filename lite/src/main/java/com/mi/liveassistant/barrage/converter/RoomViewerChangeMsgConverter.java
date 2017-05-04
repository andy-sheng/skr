package com.mi.liveassistant.barrage.converter;

import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.model.BarrageMsg;
import com.mi.liveassistant.barrage.model.BarrageMsgType;

import java.util.List;

/**
 * Created by wuxiaoshan on 17-5-3.
 */
public class RoomViewerChangeMsgConverter implements IConverter {
    @Override
    public List<Message> barrageConvert(BarrageMsg barrageMsg) {
        return null;
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_JOIN,
                BarrageMsgType.B_MSG_TYPE_LEAVE,
                BarrageMsgType.B_MSG_TYPE_VIEWER_CHANGE,
                BarrageMsgType.B_MSG_TYPE_TOP_GET,
                BarrageMsgType.B_MSG_TYPE_TOP_LOSE,
        };
    }
}
