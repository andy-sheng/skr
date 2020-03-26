package com.module.playways.room.gift;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Keep;


@Entity(
)
public class GiftDB implements Serializable {
    private static final long serialVersionUID = -4809782578272913999L;

    @Id
    private Long giftID;
    private Boolean canContinue = true;
    private String description;
    private String giftName;
    private Integer giftType = 0;
    private String giftURL;
    private Integer price = 0;
    private Integer sortID = 0;
    private String sourceURL;
    private Float realPrice;
    private Boolean play;
    private Integer textContinueCount;
    private Integer displayType;
    private String extra;
    private String sourceBaseURL; // 礼物资源URL前缀
    private String sourceMp4;  // mp4格式
    private String sourceH265; // h256格式，ios13用
    private Boolean noticeAll; // 是否飘屏
    @Generated(hash = 2023922111)
    public GiftDB(Long giftID, Boolean canContinue, String description,
            String giftName, Integer giftType, String giftURL, Integer price,
            Integer sortID, String sourceURL, Float realPrice, Boolean play,
            Integer textContinueCount, Integer displayType, String extra,
            String sourceBaseURL, String sourceMp4, String sourceH265,
            Boolean noticeAll) {
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
        this.play = play;
        this.textContinueCount = textContinueCount;
        this.displayType = displayType;
        this.extra = extra;
        this.sourceBaseURL = sourceBaseURL;
        this.sourceMp4 = sourceMp4;
        this.sourceH265 = sourceH265;
        this.noticeAll = noticeAll;
    }
    @Generated(hash = 2046579016)
    public GiftDB() {
    }
    public Long getGiftID() {
        return this.giftID;
    }
    public void setGiftID(Long giftID) {
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
    public Boolean getPlay() {
        return this.play;
    }
    public void setPlay(Boolean play) {
        this.play = play;
    }
    public Integer getTextContinueCount() {
        return this.textContinueCount;
    }
    public void setTextContinueCount(Integer textContinueCount) {
        this.textContinueCount = textContinueCount;
    }
    public Integer getDisplayType() {
        return this.displayType;
    }
    public void setDisplayType(Integer displayType) {
        this.displayType = displayType;
    }
    public String getExtra() {
        return this.extra;
    }
    public void setExtra(String extra) {
        this.extra = extra;
    }
    public String getSourceBaseURL() {
        return this.sourceBaseURL;
    }
    public void setSourceBaseURL(String sourceBaseURL) {
        this.sourceBaseURL = sourceBaseURL;
    }
    public String getSourceMp4() {
        return this.sourceMp4;
    }
    public void setSourceMp4(String sourceMp4) {
        this.sourceMp4 = sourceMp4;
    }
    public String getSourceH265() {
        return this.sourceH265;
    }
    public void setSourceH265(String sourceH265) {
        this.sourceH265 = sourceH265;
    }
    public Boolean getNoticeAll() {
        return this.noticeAll;
    }
    public void setNoticeAll(Boolean noticeAll) {
        this.noticeAll = noticeAll;
    }


}

