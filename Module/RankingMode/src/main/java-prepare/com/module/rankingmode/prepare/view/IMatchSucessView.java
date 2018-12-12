package com.module.rankingmode.prepare.view;

import com.module.rankingmode.prepare.model.JsonGameReadyInfo;

public interface IMatchSucessView {
    void ready(boolean isPrepareState);

    void allPlayerIsReady(JsonGameReadyInfo gameReadyInfo);

    void needReMatch();
}
