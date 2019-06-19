package com.module.playways.doubleplay.pushEvent;

import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomMusic;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.CombineRoom.LoadMusicInfoMsg;

public class DoubleLoadMusicInfoPushEvent {
    BasePushInfo basePushInfo;
    LocalCombineRoomMusic currentMusic;
    String nextMusicDesc;

    public BasePushInfo getBasePushInfo() {
        return basePushInfo;
    }

    public LocalCombineRoomMusic getCurrentMusic() {
        return currentMusic;
    }

    public String getNextMusicDesc() {
        return nextMusicDesc;
    }

    public DoubleLoadMusicInfoPushEvent(BasePushInfo basePushInfo, LoadMusicInfoMsg loadMusicInfoMsg) {
        this.basePushInfo = basePushInfo;
        currentMusic = new LocalCombineRoomMusic(loadMusicInfoMsg.getCurrentMusic());
        nextMusicDesc = loadMusicInfoMsg.getNextMusicDesc();
    }
}
