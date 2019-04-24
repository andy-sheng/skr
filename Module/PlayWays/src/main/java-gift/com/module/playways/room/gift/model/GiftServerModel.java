package com.module.playways.room.gift.model;

import java.io.Serializable;

public class GiftServerModel implements Serializable {


    /**
     * canContinue : true
     * description :
     * giftID : 2
     * giftName : 爱心2
     * giftType : 1
     * giftURL : www.inframe.com
     * price : 2
     * sortID : 1
     * sourceURL : www.source.com
     */

    private boolean canContinue;
    private String description;
    private int giftID;
    private String giftName;
    private int giftType;
    private String giftURL;
    private int price;
    private int sortID;
    private String sourceURL;

    public boolean isCanContinue() {
        return canContinue;
    }

    public void setCanContinue(boolean canContinue) {
        this.canContinue = canContinue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getGiftID() {
        return giftID;
    }

    public void setGiftID(int giftID) {
        this.giftID = giftID;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public int getGiftType() {
        return giftType;
    }

    public void setGiftType(int giftType) {
        this.giftType = giftType;
    }

    public String getGiftURL() {
        return giftURL;
    }

    public void setGiftURL(String giftURL) {
        this.giftURL = giftURL;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getSortID() {
        return sortID;
    }

    public void setSortID(int sortID) {
        this.sortID = sortID;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }
}
