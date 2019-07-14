package com.module.playways.doubleplay.pbLocalModel;

import java.io.Serializable;

public class LocalGameItemInfo implements Serializable {

    private int itemID;

    private String itemDesc;

    public int getItemID() {
        return itemID;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public LocalGameItemInfo(int itemID, String itemDesc) {
        this.itemID = itemID;
        this.itemDesc = itemDesc;
    }

    @Override
    public String toString() {
        return "LocalGameItemInfo{" +
                "itemID=" + itemID +
                ", itemDesc='" + itemDesc + '\'' +
                '}';
    }
}
