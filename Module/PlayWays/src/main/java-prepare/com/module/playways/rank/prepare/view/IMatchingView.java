package com.module.playways.rank.prepare.view;


import com.module.playways.rank.prepare.model.PlayerInfo;

import java.util.List;

public interface IMatchingView {
    /**
     * 匹配成功
     */
    void matchSucess(int gameId, long gameCreatMs, List<PlayerInfo> playerInfoList, String systemAvatar);

    void showUserIconList(List<String> avatarURL);
}
