package com.module.playways.doubleplay.pushEvent;

import com.module.playways.doubleplay.model.DoubleSyncModel;
import com.module.playways.room.msg.BasePushInfo;
import com.component.live.proto.CombineRoom.CombineRoomSyncStatusV2Msg;

public class DoubleSyncStatusV2Event {
    public BasePushInfo mBasePushInfo;

    DoubleSyncModel doubleSyncModel;

    public DoubleSyncModel getDoubleSyncModel() {
        return doubleSyncModel;
    }

    public DoubleSyncStatusV2Event(BasePushInfo basePushInfo, CombineRoomSyncStatusV2Msg combineRoomSyncStatusV2Msg) {
        mBasePushInfo = basePushInfo;
        doubleSyncModel = DoubleSyncModel.parse(combineRoomSyncStatusV2Msg);
    }

    @Override
    public String toString() {
        return "DoubleSyncStatusV2Event{" +
                "mBasePushInfo=" + mBasePushInfo +
                ", doubleSyncModel=" + doubleSyncModel +
                '}';
    }
}
