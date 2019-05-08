package com.module.playways.room.gift.model;

import com.alibaba.fastjson.JSONObject;
import com.module.playways.room.gift.GiftDB;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;
import com.module.playways.room.room.gift.model.GiftPlayModel;
import com.zq.live.proto.Common.EGiftDisplayType;

import java.util.ArrayList;
import java.util.List;

public class BaseGift {

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
    private float realPrice;
    private int sortID;
    private String sourceURL;
    //有没有动画
    private boolean play;
    //连送展示几个，0为不展示，-1，为一直展示，
    private int textContinueCount;
    //展示方式，有免费礼物展示，小礼物，中礼物，大礼物四种（0， 1， 2， 3）
    private int displayType;
    //附加信息
    private String extra;

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

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
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


    public static <T extends BaseGift> List<T> parseFromGiftDB(List<GiftDB> giftDBList) {
        ArrayList<T> list = new ArrayList<>();
        if (giftDBList == null) {
            return list;
        }

        for (GiftDB giftDB : giftDBList) {
            list.add(parse(giftDB));
        }

        return list;
    }

    public static <T extends BaseGift> List<T> parse(List<GiftServerModel> giftServerModelList) {
        ArrayList<T> list = new ArrayList<>();
        if (giftServerModelList == null) {
            return list;
        }

        for (GiftServerModel giftServerModel : giftServerModelList) {
            list.add(parse(giftServerModel));
        }

        return list;
    }

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

    public void parseFromSever(GiftServerModel giftServerModel) {
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
        setPlay(giftServerModel.isPlay());
        setDisplayType(giftServerModel.getDisplayType());
        setExtra(giftServerModel.getExtra());

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
        setPlay(giftDB.getPlay());
        setDisplayType(giftDB.getDisplayType());

        // 解析
        parseFromJson(giftDB.getExtra());
    }

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


    protected void parseFromJson(String extra) {

    }
}
