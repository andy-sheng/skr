package com.module.rankingmode.prepare.view;

import com.module.rankingmode.prepare.model.GameReadyModel;

public interface IMatchSucessView {
    void ready(boolean isPrepareState);

    void allPlayerIsReady(GameReadyModel gameReadyInfo);

    void needReMatch();
}
