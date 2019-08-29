package com.module.playways.room.room.view;

import com.module.playways.room.room.model.RankInfoModel;
import com.component.person.model.UserRankModel;

import java.util.List;

public interface ILeaderBoardView {
    void showFirstThreeRankInfo(List<RankInfoModel> rankInfoModel);

    void showRankList(List<RankInfoModel> rankInfoModel, boolean hasMore);

    void showOwnRankInfo(UserRankModel userRankModel);

    void noNetWork();
}
