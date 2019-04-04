package com.module.home.game.model;

import com.component.busilib.friends.SpecialModel;

import java.util.List;

public class QuickJoinRoomModel {

    List<SpecialModel> mModelList;
    int offset;

    public QuickJoinRoomModel(List<SpecialModel> modelList, int offset) {
        this.mModelList = modelList;
        this.offset = offset;
    }

    public List<SpecialModel> getModelList() {
        return mModelList;
    }

    public void setModelList(List<SpecialModel> modelList) {
        mModelList = modelList;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

}
