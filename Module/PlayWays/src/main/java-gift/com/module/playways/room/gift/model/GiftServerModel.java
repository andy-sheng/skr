package com.module.playways.room.gift.model;

import android.text.TextUtils;

import com.module.playways.room.gift.GiftDB;

import java.io.Serializable;

public class GiftServerModel implements Serializable {
    /**
     * giftID : 2
     * giftName : 大便
     * giftURL : http://res-static.inframe.mobi/gift/img/liwu_dabian.png
     * price : 2
     * sourceURL :
     * sortID : 2
     * giftType : 1
     * canContinue : true
     * description : 大便
     * realPrice : 2
     * play : false
     * textContinueCount : 0
     * displayType : 0
     * extra : null
     */

    private long giftID;
    private String giftName;
    private String giftURL;
    private int price;
    private String sourceURL;
    private int sortID;
    private int giftType;
    private boolean canContinue;
    private String description;
    private float realPrice;
    private boolean play;
    private int textContinueCount;
    private int displayType;
    private String extra;

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

    public String getSourceURL() {
        return sourceURL;
    }

    public int getSortID() {
        return sortID;
    }

    public void setSortID(int sortID) {
        this.sortID = sortID;
    }

    public int getGiftType() {
        return giftType;
    }

    public void setGiftType(int giftType) {
        this.giftType = giftType;
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

    public float getRealPrice() {
        return realPrice;
    }

    public void setRealPrice(float realPrice) {
        this.realPrice = realPrice;
    }

    public boolean isPlay() {
        return play;
    }

    public void setPlay(boolean play) {
        this.play = play;
    }

    public int getTextContinueCount() {
        return textContinueCount;
    }

    public void setTextContinueCount(int textContinueCount) {
        this.textContinueCount = textContinueCount;
    }

    public int getDisplayType() {
        return displayType;
    }

    public void setDisplayType(int displayType) {
        this.displayType = displayType;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
        // TODO: 2019-05-08  假数据
        if (!TextUtils.isEmpty(sourceURL)) {
            this.play = true;
            this.textContinueCount = 3;
            this.displayType = 2;
        }
    }

    public static GiftDB toGiftDB(GiftServerModel giftServerModel) {
        GiftDB giftDB = new GiftDB();
        giftDB.setGiftID(giftServerModel.getGiftID());
        giftDB.setGiftName(giftServerModel.getGiftName());
        giftDB.setGiftType(giftServerModel.getGiftType());
        giftDB.setCanContinue(giftServerModel.isCanContinue());
        giftDB.setTextContinueCount(giftServerModel.getTextContinueCount());
        giftDB.setGiftURL(giftServerModel.getGiftURL());
        giftDB.setPrice(giftServerModel.getPrice());
        giftDB.setRealPrice(giftServerModel.getRealPrice());
        giftDB.setSortID(giftServerModel.getSortID());
        giftDB.setDescription(giftServerModel.getDescription());
        giftDB.setSourceURL(giftServerModel.getSourceURL());
        giftDB.setPlay(giftServerModel.isPlay());
        giftDB.setDisplayType(giftServerModel.getDisplayType());
        giftDB.setExtra(giftServerModel.getExtra());
        return giftDB;
    }
}
