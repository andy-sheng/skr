package com.module.playways.grab.room;

import com.module.playways.grab.room.model.NumericDetailModel;

import java.io.Serializable;
import java.util.List;

public class GrabResultData implements Serializable {

    List<NumericDetailModel> mDetailModels;

    public GrabResultData(List<NumericDetailModel> modelList) {
        this.mDetailModels = modelList;
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
