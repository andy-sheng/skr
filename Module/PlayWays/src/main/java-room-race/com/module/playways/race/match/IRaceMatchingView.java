package com.module.playways.race.match;


import com.module.playways.race.match.model.JoinRaceRoomRspModel;
import com.module.playways.room.prepare.model.JoinGrabRoomRspModel;

public interface IRaceMatchingView {
    /**
     * 匹配成功
     */
    void matchRaceSucess(JoinRaceRoomRspModel t);
}
