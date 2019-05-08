package com.module.playways.room.gift;

import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.io.Serializable;

import org.greenrobot.greendao.annotation.Generated;

@Entity(
)
public class GiftDB implements Serializable {
    private static final long serialVersionUID = -4809782578272913999L;

    @Id
    private Integer giftID;
    private Boolean canContinue = true;
    private String description;
    private String giftName;
    private Integer giftType = 0;
    private String giftURL;
    private Integer price = 0;
    private Integer sortID = 0;
    private String sourceURL;
    private Float realPrice;
    @Generated(hash = 1211253132)
    public GiftDB(Integer giftID, Boolean canContinue, String description,
            String giftName, Integer giftType, String giftURL, Integer price,
            Integer sortID, String sourceURL, Float realPrice) {
        this.giftID = giftID;
        this.canContinue = canContinue;
        this.description = description;
        this.giftName = giftName;
        this.giftType = giftType;
        this.giftURL = giftURL;
        this.price = price;
        this.sortID = sortID;
        this.sourceURL = sourceURL;
        this.realPrice = realPrice;
    }
    @Generated(hash = 2046579016)
    public GiftDB() {
    }
    public Integer getGiftID() {
        return this.giftID;
    }
    public void setGiftID(Integer giftID) {
        this.giftID = giftID;
    }
    public Boolean getCanContinue() {
        return this.canContinue;
    }
    public void setCanContinue(Boolean canContinue) {
        this.canContinue = canContinue;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getGiftName() {
        return this.giftName;
    }
    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }
    public Integer getGiftType() {
        return this.giftType;
    }
    public void setGiftType(Integer giftType) {
        this.giftType = giftType;
    }
    public String getGiftURL() {
        return this.giftURL;
    }
    public void setGiftURL(String giftURL) {
        this.giftURL = giftURL;
    }
    public Integer getPrice() {
        return this.price;
    }
    public void setPrice(Integer price) {
        this.price = price;
    }
    public Integer getSortID() {
        return this.sortID;
    }
    public void setSortID(Integer sortID) {
        this.sortID = sortID;
    }
    public String getSourceURL() {
        return this.sourceURL;
    }
    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }
    public Float getRealPrice() {
        return this.realPrice;
    }
    public void setRealPrice(Float realPrice) {
        this.realPrice = realPrice;
    }
    
    

}

