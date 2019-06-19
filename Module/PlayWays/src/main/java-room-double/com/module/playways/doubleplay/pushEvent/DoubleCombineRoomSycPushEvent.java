package com.module.playways.doubleplay.pushEvent;

import com.module.playways.doubleplay.model.DoubleSyncModel;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.CombineRoom.CombineRoomSyncStatusMsg;

public class DoubleCombineRoomSycPushEvent {
    BasePushInfo basePushInfo;
    DoubleSyncModel doubleSyncModel;

    public BasePushInfo getBasePushInfo() {
        return basePushInfo;
    }

    public DoubleSyncModel getDoubleSyncModel() {
        return doubleSyncModel;
    }

    public DoubleCombineRoomSycPushEvent(BasePushInfo basePushInfo, CombineRoomSyncStatusMsg combineRoomSyncStatusMsg) {
        this.basePushInfo = basePushInfo;
        this.doubleSyncModel = DoubleSyncModel.parse(combineRoomSyncStatusMsg);
    }
}
