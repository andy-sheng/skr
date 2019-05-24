package com.module.home.game.model;

import com.component.busilib.friends.RecommendModel;

import java.util.List;

public class RecommendRoomModel {

    List<RecommendModel> mRoomModels;

    public RecommendRoomModel(List<RecommendModel> list) {
        this.mRoomModels = list;
    }

    public List<RecommendModel> getRoomModels() {
        return mRoomModels;
    }

    public void setRoomModels(List<RecommendModel> roomModels) {
        mRoomModels = roomModels;
    }

}
