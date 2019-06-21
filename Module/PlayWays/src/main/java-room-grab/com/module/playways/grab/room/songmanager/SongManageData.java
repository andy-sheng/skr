package com.module.playways.grab.room.songmanager;

import com.component.busilib.friends.SpecialModel;
import com.module.playways.doubleplay.DoubleRoomData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;

import java.io.Serializable;

public class SongManageData implements Serializable {
    GrabRoomData mGrabRoomData;
    DoubleRoomData mDoubleRoomData;

    public SongManageData(GrabRoomData grabRoomData) {
        mGrabRoomData = grabRoomData;
    }

    public SongManageData(DoubleRoomData doubleRoomData) {
        mDoubleRoomData = doubleRoomData;
    }

    public SpecialModel getSpecialModel() {
        if (mGrabRoomData != null) {
            mGrabRoomData.getSpecialModel();
        }

        return null;
    }

    public void setSpecialModel(SpecialModel specialModel) {
        if (mGrabRoomData != null) {
            mGrabRoomData.setSpecialModel(specialModel);
        }
    }

    public void setTagId(int tagId) {
        if (mGrabRoomData != null) {
            mGrabRoomData.setTagId(tagId);
        }
    }

    public int getGameId() {
        if (mGrabRoomData != null) {
            return mGrabRoomData.getGameId();
        }

        if (mDoubleRoomData != null) {
            return mDoubleRoomData.getGameId();
        }

        return -1;
    }

    public boolean hasGameBegin() {
        if (mGrabRoomData != null) {
            return mGrabRoomData.hasGameBegin();
        }

        if (mDoubleRoomData != null) {
            return mDoubleRoomData.getDoubleGameState() == DoubleRoomData.DoubleGameState.START;
        }

        return false;
    }

    public int getRealRoundSeq() {
        if (mGrabRoomData != null) {
            return mGrabRoomData.getRealRoundSeq();
        }

        return 0;
    }

    public void setExpectRoundInfo(GrabRoundInfoModel expectRoundInfo) {
        if (mGrabRoomData != null) {
            mGrabRoomData.setExpectRoundInfo(expectRoundInfo);
        }


    }
}
