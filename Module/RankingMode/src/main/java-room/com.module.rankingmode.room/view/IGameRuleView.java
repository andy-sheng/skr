package com.module.rankingmode.room.view;

import com.module.rankingmode.prepare.model.OnLineInfoModel;

import java.util.List;

public interface IGameRuleView {
    void startSelfCountdown(Runnable countDownOver);

    void startRivalCountdown();

    void userExit();

    void gameFinish();

    void updateUserState(List<OnLineInfoModel> jsonOnLineInfoList);

    void playLyric(int songId);
}
