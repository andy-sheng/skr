package com.module.playways.rank.room.view;

import com.module.playways.rank.prepare.model.OnlineInfoModel;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.rank.song.model.SongModel;

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

    void updateUserState(List<OnlineInfoModel> jsonOnLineInfoList);

    //先显示，然后再播放
    void playLyric(SongModel songModel, boolean play);

    void updateScrollBarProgress(int volume);

    // 显示演唱剩余时间倒计时
    void showLeftTime(long wholeTile);

    // 主舞台离开（开始主舞台消失动画）
    void hideMainStage();
}
