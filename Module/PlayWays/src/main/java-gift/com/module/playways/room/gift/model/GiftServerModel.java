package com.module.playways.room.gift.model;

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
    private String sourceBaseURL; // 礼物资源URL前缀
    private String sourceMp4;  // mp4格式
    private String sourceH265; // h256格式，ios13用
    private Boolean noticeAll; // 是否飘屏
    private int sortID;
    private int giftType;
    private boolean canContinue;
    private String description;
    private float realPrice;
    private boolean play;
    private int textContinueCount;
    private int displayType;
    private String extra;
    private int balance;


    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
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

    public String getSourceBaseURL() {
        return sourceBaseURL;
    }

    public void setSourceBaseURL(String sourceBaseURL) {
        this.sourceBaseURL = sourceBaseURL;
    }

    public String getSourceMp4() {
        return sourceMp4;
    }

    public void setSourceMp4(String sourceMp4) {
        this.sourceMp4 = sourceMp4;
    }

    public String getSourceH265() {
        return sourceH265;
    }

    public void setSourceH265(String sourceH265) {
        this.sourceH265 = sourceH265;
    }

    public Boolean getNoticeAll() {
        return noticeAll;
    }

    public void setNoticeAll(Boolean noticeAll) {
        this.noticeAll = noticeAll;
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
        giftDB.setSourceBaseURL(giftServerModel.getSourceBaseURL());
        giftDB.setSourceMp4(giftServerModel.getSourceMp4());
        giftDB.setSourceH265(giftServerModel.getSourceH265());
        giftDB.setNoticeAll(giftServerModel.getNoticeAll());
        giftDB.setPlay(giftServerModel.isPlay());
        giftDB.setDisplayType(giftServerModel.getDisplayType());
        giftDB.setExtra(giftServerModel.getExtra());
        return giftDB;
    }
}
