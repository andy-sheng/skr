package com.module.playways.room.gift.model;

import com.module.playways.room.gift.GiftDB;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;

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
        NormalGift normalGift = new NormalGift();
        normalGift.setGiftID(giftServerModel.getGiftID());
        normalGift.setGiftName(giftServerModel.getGiftName());
        normalGift.setGiftURL(giftServerModel.getGiftURL());
        normalGift.setPrice(giftServerModel.getPrice());
        normalGift.setCanContinue(giftServerModel.isCanContinue());
        normalGift.setDescription(giftServerModel.getDescription());
        normalGift.setGiftType(giftServerModel.getGiftType());
        normalGift.setSortID(giftServerModel.getSortID());
        normalGift.setSourceURL(giftServerModel.getSourceURL());
        normalGift.setRealPrice(giftServerModel.getRealPrice());

        return (T) normalGift;
    }

    public static <T extends BaseGift> T parse(GiftDB giftDB) {
        NormalGift normalGift = new NormalGift();
        normalGift.setGiftID(giftDB.getGiftID());
        normalGift.setGiftName(giftDB.getGiftName());
        normalGift.setGiftURL(giftDB.getGiftURL());
        normalGift.setPrice(giftDB.getPrice());
        normalGift.setCanContinue(giftDB.getCanContinue());
        normalGift.setDescription(giftDB.getDescription());
        normalGift.setGiftType(giftDB.getGiftType());
        normalGift.setSortID(giftDB.getSortID());
        normalGift.setSourceURL(giftDB.getSourceURL());
        normalGift.setRealPrice(giftDB.getRealPrice());
        return (T) normalGift;
    }
}
