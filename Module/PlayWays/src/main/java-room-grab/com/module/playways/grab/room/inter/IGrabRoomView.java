package com.module.playways.grab.room.inter;

import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.room.prepare.model.OnlineInfoModel;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;
import com.module.playways.room.song.model.SongModel;

import java.util.List;

public interface IGrabRoomView {
    /**
     * 抢唱阶段开始
     * 展示要唱的歌儿,《下一首》《东西》
     *
     * @param seq       当前轮次的序号
     * @param songModel 要唱的歌信息
     */
    void grabBegin(int seq, SongModel songModel);

    /**
     * 自己抢到了
     * 演唱阶段开始
     */
    void singBySelf();

    /**
     * 别人抢到了
     * 演唱阶段开始
     */
    void singByOthers();

    /**
     * 轮次结束
     *
     */
    void roundOver(GrabRoundInfoModel lastRoundInfo,boolean playNextSongInfoCard,GrabRoundInfoModel now);

    void updateUserState(List<OnlineInfoModel> jsonOnLineInfoList);

    /**
     * 中途跑了
     */
    void exitInRound();


    void gameFinish();


    void onGetGameResult(boolean success);

    void onChangeRoomResult(boolean success,String errorMsg);

    void giveUpSuccess(int seq);

    void updateScrollBarProgress(int score, int songLineNum);

    void showKickVoteDialog(int kickUserID, int sourceUserID);

    /**
     * 是否被房主踢出去
     * @param isOwner
     */
    void kickBySomeOne(boolean isOwner);

    /**
     * 把别人踢出去
     */
    void dimissKickDialog();

    void showPracticeFlag(boolean flag);

    void hideInviteTipView();

    void hideManageTipView();
}
