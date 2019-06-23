package com.module.playways.grab.room.songmanager.model;

import android.text.TextUtils;

import com.zq.live.proto.Common.StandPlayType;

import java.io.Serializable;

public class GrabRoomSongModel implements Serializable {

    /**
     * itemName : 说爱你
     * owner : 沈以诚
     * roundSeq : 19
     * itemID : 4008
     * playType : 3
     * challengeAvailable : false
     */

    protected String itemName;
    protected String owner;
    protected int roundSeq;
    protected int itemID;
    protected int playType;
    protected boolean challengeAvailable;
    protected String uniqTag;

    public String getUniqTag() {
        return uniqTag;
    }

    public void setUniqTag(String uniqTag) {
        this.uniqTag = uniqTag;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getRoundSeq() {
        return roundSeq;
    }

    public void setRoundSeq(int roundSeq) {
        this.roundSeq = roundSeq;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getPlayType() {
        return playType;
    }

    public void setPlayType(int playType) {
        this.playType = playType;
    }

    public boolean isChallengeAvailable() {
        return challengeAvailable;
    }

    public void setChallengeAvailable(boolean challengeAvailable) {
        this.challengeAvailable = challengeAvailable;
    }

    public String getDisplaySongName() {
        if (playType == StandPlayType.PT_SPK_TYPE.getValue()) {
            if (!TextUtils.isEmpty(itemName) && itemName.contains("（PK版）")) {
                return itemName.substring(0, itemName.length() - 5);
            }
        } else if (playType == StandPlayType.PT_CHO_TYPE.getValue()) {
            if (!TextUtils.isEmpty(itemName) && itemName.contains("（合唱版）")) {
                return itemName.substring(0, itemName.length() - 5);
            }
        }
        return itemName;
    }

    @Override
    public String toString() {
        return "GrabRoomSongModel{" +
                "itemName='" + itemName + '\'' +
                ", owner='" + owner + '\'' +
                ", roundSeq=" + roundSeq +
                ", itemID=" + itemID +
                ", playType=" + playType +
                ", challengeAvailable=" + challengeAvailable +
                '}';
    }
}
