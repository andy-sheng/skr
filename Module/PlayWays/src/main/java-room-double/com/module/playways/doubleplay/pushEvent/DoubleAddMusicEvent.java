package com.module.playways.doubleplay.pushEvent;

import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomMusic;
import com.module.playways.room.msg.BasePushInfo;
import com.component.live.proto.CombineRoom.AddMusicInfoMsg;

public class DoubleAddMusicEvent {

    public BasePushInfo mBasePushInfo;
    public LocalCombineRoomMusic mCombineRoomMusic;
    public boolean mNeedLoad;

    public DoubleAddMusicEvent(BasePushInfo info, AddMusicInfoMsg addMusicInfoMsg) {
        this.mBasePushInfo = info;
        this.mNeedLoad = addMusicInfoMsg.getNeedLoad();
        mCombineRoomMusic = new LocalCombineRoomMusic(addMusicInfoMsg.getMusic());
    }
}
