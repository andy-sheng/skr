package com.module.rankingmode.prepare.view;

import com.module.rankingmode.prepare.model.GameReadyModel;
import com.module.rankingmode.prepare.model.JsonReadyInfo;

import java.util.List;

public interface IMatchSucessView {
    void ready(boolean isPrepareState);

    void allPlayerIsReady(GameReadyModel gameReadyInfo);

    void needReMatch();

    void readyList(List<JsonReadyInfo> readyInfos);
}
