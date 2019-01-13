package com.module.playways.rank.prepare.view;


import com.module.playways.rank.prepare.model.MatchIconModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;

import java.util.List;

public interface IMatchingView {
    /**
     * 匹配成功
     */
    void matchSucess(int gameId, long gameCreatMs, List<PlayerInfoModel> playerInfoList, String systemAvatar);

    void showUserIconList(List<MatchIconModel> avatarURL);
}
