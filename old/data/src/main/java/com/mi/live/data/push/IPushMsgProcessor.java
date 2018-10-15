package com.mi.live.data.push;

import com.base.presenter.Presenter;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.room.model.RoomBaseDataModel;

/**
 * Created by chengsimin on 16/7/4.
 */
public interface IPushMsgProcessor extends Presenter{
    void process(BarrageMsg msg, RoomBaseDataModel roomBaseDataModel);

    int[] getAcceptMsgType();
}
