package com.module.rankingmode.prepare.view;


import com.module.rankingmode.prepare.model.PlayerInfo;

import java.util.List;

public interface IMatchingView {
    /**
     * 匹配成功
     */
    void matchSucess(int gameId, long gameCreatMs, List<PlayerInfo> playerInfoList);

    void showUserIconList();
}
