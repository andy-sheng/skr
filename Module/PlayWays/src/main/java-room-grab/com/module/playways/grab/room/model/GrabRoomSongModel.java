package com.module.playways.grab.room.model;

import java.io.Serializable;

public class GrabRoomSongModel implements Serializable {
    /**
     * itemName : string
     * owner : string
     * playbookItemID : 0
     * roundSeq : 0
     */

    private String itemName;
    private String owner;
    private int playbookItemID;
    private int roundSeq;

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

    public int getPlaybookItemID() {
        return playbookItemID;
    }

    public void setPlaybookItemID(int playbookItemID) {
        this.playbookItemID = playbookItemID;
    }

    public int getRoundSeq() {
        return roundSeq;
    }

    public void setRoundSeq(int roundSeq) {
        this.roundSeq = roundSeq;
    }
}
