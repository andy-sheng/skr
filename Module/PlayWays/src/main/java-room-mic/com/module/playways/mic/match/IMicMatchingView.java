package com.module.playways.mic.match;


import com.module.playways.mic.match.model.JoinMicRoomRspModel;

public interface IMicMatchingView {
    /**
     * 匹配成功
     */
    void matchRaceSucess(JoinMicRoomRspModel t);
}
