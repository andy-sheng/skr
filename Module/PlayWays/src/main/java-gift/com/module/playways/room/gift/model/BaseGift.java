package com.module.playways.room.gift.model;

import com.alibaba.fastjson.JSON;
import com.module.playways.room.gift.GiftDB;
import com.zq.live.proto.Common.GiftExtraInfo;
import com.zq.live.proto.Common.GiftInfo;

public abstract class BaseGift {

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
    private String giftURL;// 礼物图片
    private int price;
    private float realPrice;
    private int sortID;
    private String sourceURL;// 礼物动画资源
    private String sourceBaseURL; // 礼物资源URL前缀
    private String sourceMp4;  // mp4格式
    private String sourceH265; // h256格式，ios13用
    private Boolean noticeAll; // 是否飘屏
    //有没有动画
    private boolean play;
    //连送展示几个，0为不展示，-1，为一直展示，
    private int textContinueCount;
    //展示方式，有免费礼物展示，小礼物，中礼物，大礼物四种（0， 1， 2， 3）
    private int displayType;
    //附加信息
//    private String extra;

    private int balance;

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

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

    //
//    public String getExtra() {
//        return extra;
//    }
//
//    public void setExtra(String extra) {
//        this.extra = extra;
//    }

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

    /**
     * 从服务器api解析
     *
     * @param giftServerModel
     * @param <T>
     * @return
     */
    public static <T extends BaseGift> T parse(GiftServerModel giftServerModel) {
        if (giftServerModel.isPlay()) {
            AnimationGift animationGift = new AnimationGift();
            animationGift.parseFromSever(giftServerModel);
            return (T) animationGift;
        } else {
            NormalGift normalGift = new NormalGift();
            normalGift.parseFromSever(giftServerModel);
            return (T) normalGift;
        }
    }

    /**
     * 数据库解析
     *
     * @param giftDB
     * @param <T>
     * @return
     */
    public static <T extends BaseGift> T parse(GiftDB giftDB) {
        if (giftDB.getPlay()) {
            AnimationGift animationGift = new AnimationGift();
            animationGift.parseFromDB(giftDB);
            return (T) animationGift;
        } else {
            NormalGift normalGift = new NormalGift();
            normalGift.parseFromDB(giftDB);
            return (T) normalGift;
        }
    }


    /**
     * 从服务器Push PB解析
     *
     * @param <T>
     * @return
     */
    public static <T extends BaseGift> T parse(GiftInfo giftPb) {
        if (giftPb.getPlay()) {
            AnimationGift animationGift = new AnimationGift();
            animationGift.parseFromPB(giftPb);
            return (T) animationGift;
        } else {
            NormalGift normalGift = new NormalGift();
            normalGift.parseFromPB(giftPb);
            return (T) normalGift;
        }
    }

    void parseFromSever(GiftServerModel giftServerModel) {
        setGiftID(giftServerModel.getGiftID());
        setGiftName(giftServerModel.getGiftName());
        setGiftType(giftServerModel.getGiftType());
        setCanContinue(giftServerModel.isCanContinue());
        setTextContinueCount(giftServerModel.getTextContinueCount());
        setGiftURL(giftServerModel.getGiftURL());
        setPrice(giftServerModel.getPrice());
        setRealPrice(giftServerModel.getRealPrice());
        setSortID(giftServerModel.getSortID());
        setDescription(giftServerModel.getDescription());
        setSourceURL(giftServerModel.getSourceURL());
        setSourceBaseURL(giftServerModel.getSourceBaseURL());
        setSourceMp4(giftServerModel.getSourceMp4());
        setSourceH265(giftServerModel.getSourceH265());
        setNoticeAll(giftServerModel.getNoticeAll());
        setPlay(giftServerModel.isPlay());
        setDisplayType(giftServerModel.getDisplayType());
        setBalance(giftServerModel.getBalance());
        // 解析
        parseFromJson(giftServerModel.getExtra());
    }

    public void parseFromDB(GiftDB giftDB) {
        setGiftID(giftDB.getGiftID());
        setGiftName(giftDB.getGiftName());
        setGiftType(giftDB.getGiftType());
        setCanContinue(giftDB.getCanContinue());
        setTextContinueCount(giftDB.getTextContinueCount());
        setGiftURL(giftDB.getGiftURL());
        setPrice(giftDB.getPrice());
        setRealPrice(giftDB.getRealPrice());
        setSortID(giftDB.getSortID());
        setDescription(giftDB.getDescription());
        setSourceURL(giftDB.getSourceURL());
        setSourceBaseURL(giftDB.getSourceBaseURL());
        setSourceMp4(giftDB.getSourceMp4());
        setSourceH265(giftDB.getSourceH265());
        setNoticeAll(giftDB.getNoticeAll());
        setPlay(giftDB.getPlay());
        setDisplayType(giftDB.getDisplayType());

        // 解析
        parseFromJson(giftDB.getExtra());
    }

    public void parseFromPB(GiftInfo giftPB) {
        setGiftID(giftPB.getGiftID());
        setGiftName(giftPB.getGiftName());
        setGiftType(giftPB.getGiftType().getValue());
        setCanContinue(giftPB.getCanContinue());
        setTextContinueCount(giftPB.getTextContinueCount());
        setGiftURL(giftPB.getGiftURL());
        setPrice(giftPB.getPrice().intValue());
        setRealPrice(giftPB.getRealPrice());
        setSortID(giftPB.getSortID());
        setDescription(giftPB.getDescription());
        setSourceURL(giftPB.getSourceURL());
        setSourceBaseURL(giftPB.getSourceBaseURL());
        setSourceMp4(giftPB.getSourceMp4());
        setSourceH265(giftPB.getSourceH265());
        setNoticeAll(giftPB.getNoticeAll());
        setPlay(giftPB.getPlay());
        setDisplayType(giftPB.getDisplayType().getValue());

        // 解析
        GiftExtraInfo extraInfo = giftPB.getExtra();
        if (extraInfo != null) {
            parseFromJson(JSON.toJSONString(extraInfo));
        }
    }

    /**
     * 被子类复写
     *
     * @param extra
     */
    protected abstract void parseFromJson(String extra);
}
