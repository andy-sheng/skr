package com.module.playways.grab.room.inter;

import com.module.playways.rank.room.model.RecordData;
import com.module.playways.rank.song.model.SongModel;

public interface IGrabView {
    /**
     * 抢唱阶段
     * 展示要唱的歌儿,《下一首》《东西》
     * @param seq 当前轮次的序号
     * @param songModel 要唱的歌信息
     * @param onFinished 动画执行完毕时，要执行的逻辑
     */
    void showSongInfoCard(int seq,SongModel songModel,Runnable onFinished);

    /**
     * 自己抢到
     * @param countDownOver
     */
    void startSelfCountdown(Runnable countDownOver);

    /**
     * 别人抢到
     */
    void startRivalCountdown(long uid);

    /**
     * 抢了唱歌权的人亮灯
     * @param uid
     */
    void lightVieUser(long uid);

    /**
     * 抢到唱歌权的人亮灯
     * @param uid
     */
    void lightSingUser(long uid);

    /**
     * 没人想唱
     */
    void noOneWantSing();

    /**
     * 挑战成功，一场到底了
     */
    void challengeSuccess();

    /**
     * 中途被灭灯，挑战失败
     */
    void challengeFaild();

    void gameFinish();

    /**
     * 战绩界面
     */
    void showRecordView(RecordData recordData);

    // 主舞台离开（开始主舞台消失动画）
    void hideMainStage();
}
