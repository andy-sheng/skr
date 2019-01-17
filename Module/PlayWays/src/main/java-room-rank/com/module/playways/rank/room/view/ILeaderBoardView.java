package com.module.playways.rank.room.view;

import com.common.core.userinfo.model.RankInfoModel;
import com.common.core.userinfo.model.UserRankModel;

import java.util.List;

public interface ILeaderBoardView {
    void showFirstThreeRankInfo(List<RankInfoModel> rankInfoModel);

    void showRankList(List<RankInfoModel> rankInfoModel, boolean hasMore);

    void showOwnRankInfo(UserRankModel userRankModel);

    void noNetWork();
}
