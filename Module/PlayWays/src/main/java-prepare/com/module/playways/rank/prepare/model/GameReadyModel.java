package com.module.playways.rank.prepare.model;

import com.common.log.MyLog;
import com.zq.live.proto.Room.ReadyInfo;
import com.zq.live.proto.Room.ReadyNoticeMsg;
import com.zq.live.proto.Room.RoundInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameReadyModel implements Serializable {
    /**
     * jsonReadyInfo : [{"userID":7,"readySeq":1,"readyTimeMs":1544584287997},{"userID":8,"readySeq":2,"readyTimeMs":1544584290741},{"userID":9,"readySeq":3,"readyTimeMs":1544586876228}]
     * HasReadyedUserCnt : 3
     * isGameStart : true
     * jsonRoundInfo : [{"userID":7,"playbookID":1,"roundSeq":1,"singBeginMs":3000,"singEndMs":341000},{"userID":8,"playbookID":1,"roundSeq":2,"singBeginMs":344000,"singEndMs":682000},{"userID":9,"playbookID":1,"roundSeq":3,"singBeginMs":685000,"singEndMs":1023000}]
     * jsonGameStartInfo : {"startTimeMs":1544586876239,"startPassedMs":3119}
     */

    private int HasReadyedUserCnt;
    private boolean isGameStart;
    private GameStartInfoModel jsonGameStartInfo;
    private List<ReadyInfoModel> jsonReadyInfo;
    private List<RoundInfoModel> jsonRoundInfo;

    public int getHasReadyedUserCnt() {
        return HasReadyedUserCnt;
    }

    public void setHasReadyedUserCnt(int HasReadyedUserCnt) {
        this.HasReadyedUserCnt = HasReadyedUserCnt;
    }

    public boolean isIsGameStart() {
        return isGameStart;
    }

    public void setIsGameStart(boolean isGameStart) {
        this.isGameStart = isGameStart;
    }

    public GameStartInfoModel getJsonGameStartInfo() {
        return jsonGameStartInfo;
    }

    public void setJsonGameStartInfo(GameStartInfoModel jsonGameStartInfo) {
        this.jsonGameStartInfo = jsonGameStartInfo;
    }

    public List<ReadyInfoModel> getJsonReadyInfo() {
        return jsonReadyInfo;
    }

    public void setJsonReadyInfo(List<ReadyInfoModel> jsonReadyInfo) {
        this.jsonReadyInfo = jsonReadyInfo;
    }

    public List<RoundInfoModel> getJsonRoundInfo() {
        return jsonRoundInfo;
    }

    public void setJsonRoundInfo(List<RoundInfoModel> jsonRoundInfo) {
        this.jsonRoundInfo = jsonRoundInfo;
    }

    public void parse(ReadyNoticeMsg msg) {
        if (msg == null) {
            MyLog.e("JsonGameReadyInfo ReadyNoticeMsg == null");
            return;
        }

        this.setHasReadyedUserCnt(msg.getHasReadyedUserCnt());
        this.setIsGameStart(msg.getIsGameStart());

        GameStartInfoModel jsonGameStartInfo = new GameStartInfoModel();
        jsonGameStartInfo.parse(msg.getGameStartInfo());
        this.setJsonGameStartInfo(jsonGameStartInfo);

        List<ReadyInfoModel> jsonReadyInfos = new ArrayList<>();
        for (ReadyInfo info : msg.getReadyInfoList()) {
            ReadyInfoModel jsonReadyInfo = new ReadyInfoModel();
            jsonReadyInfo.parse(info);
            jsonReadyInfos.add(jsonReadyInfo);
        }
        this.setJsonReadyInfo(jsonReadyInfos);

        List<RoundInfoModel> jsonRoundInfos = new ArrayList<>();
        for (RoundInfo roundInfo : msg.getRoundInfoList()) {
            RoundInfoModel jsonRoundInfo = new RoundInfoModel();
            jsonRoundInfo.parse(roundInfo);
            jsonRoundInfos.add(jsonRoundInfo);
        }
        this.setJsonRoundInfo(jsonRoundInfos);

        return;
    }

    @Override
    public String toString() {
        return "JsonGameReadyInfo{" +
                "HasReadyedUserCnt=" + HasReadyedUserCnt +
                ", isGameStart=" + isGameStart +
                ", jsonGameStartInfo=" + jsonGameStartInfo +
                ", jsonReadyInfo=" + jsonReadyInfo +
                ", jsonRoundInfo=" + jsonRoundInfo +
                '}';
    }
}
