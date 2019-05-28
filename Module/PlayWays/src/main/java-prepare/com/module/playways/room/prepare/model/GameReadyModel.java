package com.module.playways.room.prepare.model;

import com.common.log.MyLog;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.room.room.model.RankRoundInfoModel;
import com.zq.live.proto.Room.QRoundInfo;
import com.zq.live.proto.Room.ReadyInfo;
import com.zq.live.proto.Room.ReadyNoticeMsg;
import com.zq.live.proto.Room.RoundInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameReadyModel implements Serializable {
    /**
     * readyInfo : [{"userID":2054313,"readySeq":1,"readyTimeMs":1548861310386},{"userID":200003,"readySeq":2,"readyTimeMs":1548861311058},{"userID":200276,"readySeq":3,"readyTimeMs":1548861312095}]
     * hasReadyedUserCnt : 3
     * isGameStart : true
     * gameStartInfo : {"startTimeMs":1548861312105,"startPassedMs":7210}
     * roundInfo : [{"userID":2054313,"playbookID":1456,"roundSeq":1,"singBeginMs":6000,"singEndMs":7079},{"userID":200003,"playbookID":909,"roundSeq":2,"singBeginMs":11079,"singEndMs":42079},{"userID":200276,"playbookID":970,"roundSeq":3,"singBeginMs":75000,"singEndMs":102000}]
     * qRoundInfo : null
     */

    private int hasReadyedUserCnt;
    private boolean isGameStart;
    private GameStartInfoModel gameStartInfo;
    private List<GrabRoundInfoModel> qRoundInfo;    //一唱到底roundInfo todo确定是不是要舍弃服务器的数据？
    private List<ReadyInfoModel> readyInfo;     //准备信息
    private List<RankRoundInfoModel> roundInfo;     //pk的轮次信息

    public int getHasReadyedUserCnt() {
        return hasReadyedUserCnt;
    }

    public void setHasReadyedUserCnt(int hasReadyedUserCnt) {
        this.hasReadyedUserCnt = hasReadyedUserCnt;
    }

    public boolean isGameStart() {
        return isGameStart;
    }

    public void setGameStart(boolean gameStart) {
        isGameStart = gameStart;
    }

    public GameStartInfoModel getGameStartInfo() {
        return gameStartInfo;
    }

    public void setGameStartInfo(GameStartInfoModel gameStartInfo) {
        this.gameStartInfo = gameStartInfo;
    }

    public List<GrabRoundInfoModel> getqRoundInfo() {
        return qRoundInfo;
    }

    public void setqRoundInfo(List<GrabRoundInfoModel> qRoundInfo) {
        this.qRoundInfo = qRoundInfo;
    }

    public List<ReadyInfoModel> getReadyInfo() {
        return readyInfo;
    }

    public void setReadyInfo(List<ReadyInfoModel> readyInfo) {
        this.readyInfo = readyInfo;
    }

    public List<RankRoundInfoModel> getRoundInfo() {
        return roundInfo;
    }

    public void setRoundInfo(List<RankRoundInfoModel> roundInfo) {
        this.roundInfo = roundInfo;
    }

    public void parse(ReadyNoticeMsg msg) {
        if (msg == null) {
            MyLog.e("GameReadyModel ReadyNoticeMsg == null");
            return;
        }

        this.setHasReadyedUserCnt(msg.getHasReadyedUserCnt());
        this.setGameStart(msg.getIsGameStart());

        GameStartInfoModel gameStartInfoModel = new GameStartInfoModel();
        gameStartInfoModel.parse(msg.getGameStartInfo());
        this.setGameStartInfo(gameStartInfoModel);

        List<ReadyInfoModel> readyInfoModels = new ArrayList<>();
        for (ReadyInfo info : msg.getReadyInfoList()) {
            ReadyInfoModel readyInfoModel = new ReadyInfoModel();
            readyInfoModel.parse(info);
            readyInfoModels.add(readyInfoModel);
        }
        this.setReadyInfo(readyInfoModels);

        List<RankRoundInfoModel> roundInfoModels = new ArrayList<>();
        for (RoundInfo roundInfo : msg.getRoundInfoList()) {
            RankRoundInfoModel jsonRoundInfo = RankRoundInfoModel.parseFromRoundInfo(roundInfo);
            roundInfoModels.add(jsonRoundInfo);
        }
        this.setRoundInfo(roundInfoModels);

        List<GrabRoundInfoModel> qroundInfoModels = new ArrayList<>();
        for (QRoundInfo roundInfo : msg.getQRoundInfoList()) {
            GrabRoundInfoModel jsonRoundInfo = GrabRoundInfoModel.parseFromRoundInfo(roundInfo);
            qroundInfoModels.add(jsonRoundInfo);
        }
        this.setqRoundInfo(qroundInfoModels);

        return;
    }

    @Override
    public String toString() {
        return "GameReadyModel{" +
                "hasReadyedUserCnt=" + hasReadyedUserCnt +
                ", isGameStart=" + isGameStart +
                ", gameStartInfo=" + gameStartInfo +
                ", qRoundInfo=" + qRoundInfo +
                ", readyInfo=" + readyInfo +
                ", roundInfo=" + roundInfo +
                '}';
    }
}
