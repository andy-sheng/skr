package com.module.rankingmode.prepare.view;

import com.module.rankingmode.prepare.model.GameReadyModel;
import com.module.rankingmode.prepare.model.JsonReadyInfo;

import java.util.List;

public interface IMatchSucessView {
    void ready(boolean isPrepareState);

    void allPlayerIsReady(GameReadyModel gameReadyInfo);

    boolean isReady();

    /**
     * otherEr是否是别人的原因导致无法准备
     * @param otherEr
     */
    void needReMatch(boolean otherEr);

    void readyList(List<JsonReadyInfo> readyInfos);
}
