package com.module.home.game.model;

import com.component.busilib.friends.RecommendModel;

import java.util.List;

public class RecommendRoomModel {

    List<RecommendModel> mRoomModels;
    int offset;
    int totalNum;

    public RecommendRoomModel(List<RecommendModel> list, int offset, int totalNum) {
        this.mRoomModels = list;
        this.offset = offset;
        this.totalNum = totalNum;
    }

    public List<RecommendModel> getRoomModels() {
        return mRoomModels;
    }

    public void setRoomModels(List<RecommendModel> roomModels) {
        mRoomModels = roomModels;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }
}
