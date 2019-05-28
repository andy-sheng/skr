package com.module.playways.grab.room;

import com.module.playways.grab.room.model.GrabResultInfoModel;
import com.module.playways.room.room.model.score.ScoreResultModel;

import java.io.Serializable;
import java.util.List;

public class GrabResultData implements Serializable {
    GrabResultInfoModel grabResultInfoModel;     //一唱到底结果
    List<ScoreResultModel> scoreResultModels;         // 一唱到底后段位信息

    public GrabResultData(GrabResultInfoModel grabResultInfoModel, List<ScoreResultModel> scoreResultModels) {
        this.grabResultInfoModel = grabResultInfoModel;
        this.scoreResultModels = scoreResultModels;
    }

    public GrabResultInfoModel getGrabResultInfoModel() {
        return grabResultInfoModel;
    }

    public void setGrabResultInfoModel(GrabResultInfoModel grabResultInfoModel) {
        this.grabResultInfoModel = grabResultInfoModel;
    }

    public List<ScoreResultModel> getScoreResultModels() {
        return scoreResultModels;
    }

    public void setScoreResultModels(List<ScoreResultModel> scoreResultModels) {
        this.scoreResultModels = scoreResultModels;
    }

    @Override
    public String toString() {
        return "GrabResultData{" +
                "grabResultInfoModel=" + grabResultInfoModel +
                ", scoreResultModels=" + scoreResultModels +
                '}';
    }


}
