package com.module.playways.room.gift.model;

import com.module.playways.room.gift.GiftDB;

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
    private long giftID;
    private String giftName;
    private int giftType;
    private String giftURL;
    private int price;
    private int sortID;
    private String sourceURL;
    private float realPrice;

    public float getRealPrice() {
        return realPrice;
    }

    public void setRealPrice(float realPrice) {
        this.realPrice = realPrice;
    }

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

    public long getGiftID() {
        return giftID;
    }

    public void setGiftID(long giftID) {
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

    public static GiftDB toGiftDB(GiftServerModel giftServerModel) {
        GiftDB giftDB = new GiftDB();
        giftDB.setCanContinue(giftServerModel.isCanContinue());
        giftDB.setDescription(giftServerModel.getDescription());
        giftDB.setGiftID(giftServerModel.getGiftID());
        giftDB.setGiftName(giftServerModel.getGiftName());
        giftDB.setGiftType(giftServerModel.getGiftType());
        giftDB.setGiftURL(giftServerModel.getGiftURL());
        giftDB.setPrice(giftServerModel.getPrice());
        giftDB.setSortID(giftServerModel.getSortID());
        giftDB.setSourceURL(giftServerModel.getSourceURL());
        giftDB.setRealPrice(giftServerModel.getRealPrice());

        return giftDB;
    }
}
