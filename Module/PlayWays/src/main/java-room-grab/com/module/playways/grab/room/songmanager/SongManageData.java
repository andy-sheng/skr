package com.module.playways.grab.room.songmanager;

import com.component.busilib.friends.SpecialModel;
import com.module.playways.doubleplay.DoubleRoomData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;

import java.io.Serializable;

public class SongManageData implements Serializable {
    private GrabRoomData mGrabRoomData;
    private DoubleRoomData mDoubleRoomData;

    public SongManageData(GrabRoomData grabRoomData) {
        mGrabRoomData = grabRoomData;
    }

    public SongManageData(DoubleRoomData doubleRoomData) {
        mDoubleRoomData = doubleRoomData;
    }

    public SpecialModel getSpecialModel() {
        if (mGrabRoomData != null) {
            return mGrabRoomData.getSpecialModel();
        }

        return null;
    }

    public GrabRoomData getGrabRoomData() {
        return mGrabRoomData;
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

    public String getRoomName() {
        if (mGrabRoomData != null) {
            return mGrabRoomData.getRoomName();
        }

        if (mDoubleRoomData != null) {
            return mDoubleRoomData.getConfig().getRoomSignature();
        }

        return "";
    }

    public void setRoomName(String roomName) {
        if (mGrabRoomData != null) {
            mGrabRoomData.setRoomName(roomName);
        }
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

    public boolean isOwner() {
        if (mGrabRoomData != null) {
            return mGrabRoomData.isOwner();
        }

        return false;
    }

    public boolean isDoubleRoom() {
        return mDoubleRoomData != null;
    }

    public boolean isGrabRoom() {
        return mGrabRoomData != null;
    }
}
