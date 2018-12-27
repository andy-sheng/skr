package com.module.rankingmode.room.view;

import com.module.rankingmode.prepare.model.OnLineInfoModel;
import com.module.rankingmode.room.model.RecordData;
import com.module.rankingmode.song.model.SongModel;

import java.util.List;

public interface IGameRuleView {
    void startSelfCountdown(Runnable countDownOver);

    void startRivalCountdown(int uid);

    void userExit();

    void gameFinish();

    void showVoteView();

    /**
     * 战绩界面
     */
    void showRecordView(RecordData recordData);

    void updateUserState(List<OnLineInfoModel> jsonOnLineInfoList);

    //先显示，然后再播放
    void playLyric(SongModel songModel, boolean play);

    void updateScrollBarProgress(int volume);
}
