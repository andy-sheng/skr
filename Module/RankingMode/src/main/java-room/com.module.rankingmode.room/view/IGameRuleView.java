package com.module.rankingmode.room.view;

import com.module.rankingmode.prepare.model.JsonOnLineInfo;

import java.util.List;

public interface IGameRuleView {
    void startSelfCountdown();

    void startRivalCountdown();

    void userExit();

    void gameFinish();

    void updateUserState(List<JsonOnLineInfo> jsonOnLineInfoList);
}
