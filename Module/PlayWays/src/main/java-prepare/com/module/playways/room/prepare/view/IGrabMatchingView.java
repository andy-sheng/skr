package com.module.playways.room.prepare.view;


import com.module.playways.battle.match.model.JoinBattleRoomRspModel;
import com.module.playways.room.prepare.model.JoinGrabRoomRspModel;

public interface IGrabMatchingView {
    /**
     * 匹配成功
     */
    void matchGrabSuccess(JoinGrabRoomRspModel t);

    void channelIsOffLine();

    void matchBattleSuccess(JoinBattleRoomRspModel t, String from);
}
