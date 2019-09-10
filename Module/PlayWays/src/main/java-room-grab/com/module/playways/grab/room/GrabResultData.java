package com.module.playways.grab.room;

import com.module.playways.grab.room.model.NumericDetailModel;
import com.module.playways.race.room.model.LevelResultModel;

import java.io.Serializable;
import java.util.List;

public class GrabResultData implements Serializable {

    List<NumericDetailModel> mDetailModels;
    public LevelResultModel mLevelResultModel;
    public Integer starCnt;

    public GrabResultData(List<NumericDetailModel> modelList, LevelResultModel model, Integer starCnt) {
        this.mDetailModels = modelList;
        this.mLevelResultModel = model;
        this.starCnt = starCnt;
    }

    public NumericDetailModel getNumericDetailModel(int numericType) {
        if (mDetailModels != null && mDetailModels.size() > 0) {
            for (NumericDetailModel model : mDetailModels) {
                if (model.getNumericType() == numericType) {
                    return model;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "GrabResultData{" +
                "mDetailModels=" + mDetailModels +
                '}';
    }
}
