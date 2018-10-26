package com.wali.live.modulewatch.base.assist;

import com.common.mvp.Presenter;
import com.wali.live.modulewatch.barrage.model.barrage.BarrageMsg;
import com.wali.live.modulewatch.watch.model.roominfo.RoomBaseDataModel;

/**
 * Created by chengsimin on 16/7/4.
 */
public interface IPushMsgProcessor extends Presenter {
    void process(BarrageMsg msg, RoomBaseDataModel roomBaseDataModel);

    int[] getAcceptMsgType();
}
