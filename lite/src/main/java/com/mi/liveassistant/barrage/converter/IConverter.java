package com.mi.liveassistant.barrage.converter;

import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.model.BarrageMsg;

import java.util.List;

/**
 * 弹幕数据转换接口
 *
 * Created by wuxiaoshan on 17-5-3.
 */
public interface IConverter {

    List<Message> barrageConvert(BarrageMsg barrageMsg);

    int[] getAcceptMsgType();

}
