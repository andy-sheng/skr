package com.module.playways.room.prepare.view;


import com.module.playways.room.prepare.model.JoinGrabRoomRspModel;

public interface IGrabMatchingView {
    /**
     * 匹配成功
     */
    void matchGrabSucess(JoinGrabRoomRspModel t);

    void channelIsOffLine();
}
