package com.mi.live.data.repository.model.turntable;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.base.log.MyLog;
import com.wali.live.proto.BigTurnTableProto;

/**
 * Created by zhujianning on 18-4-16.
 * 大转盘每个奖项的model
 */

public class PrizeItemModel {
    private int giftType;
    private int num;
    private int giftId;
    private BigTurnTableProto.ToWhom toWhom;
    private String times;
    private boolean isCustom;
    private String customDes;
    private String giftPic;
    private int tickets;

    public PrizeItemModel(BigTurnTableProto.PrizeItem data) {
        if(data == null) {
            return;
        }

        this.giftType = data.getGiftType().getNumber();
        this.num = data.getNum();
        this.giftId = data.getGiftId();
        this.toWhom = data.getToWhom();
        this.times = data.getTimes();
        this.isCustom = data.getIsCustom();
        this.customDes = data.getCustomPrizeName();
        this.tickets = data.getGiftValue();
    }

    public PrizeItemModel() {
    }


    public int getGiftType() {
        return giftType;
    }

    public void setGiftType(int giftType) {
        this.giftType = giftType;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getGiftId() {
        return giftId;
    }

    public void setGiftId(int giftId) {
        this.giftId = giftId;
    }

    public BigTurnTableProto.ToWhom getToWhom() {
        return toWhom;
    }

    public void setToWhom(BigTurnTableProto.ToWhom toWhom) {
        this.toWhom = toWhom;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public String getCustomDes() {
        return customDes;
    }

    public void setCustomDes(String customDes) {
        this.customDes = customDes;
    }

    public int getTickets() {
        return tickets;
    }

    public void setTickets(int tickets) {
        this.tickets = tickets;
    }

    @Override
    public String toString() {
        return "PrizeItemModel{" +
                "giftType=" + giftType +
                ", num=" + num +
                ", giftId=" + giftId +
                ", toWhom=" + toWhom +
                ", times='" + times + '\'' +
                ", isCustom=" + isCustom +
                ", customDes='" + customDes + '\'' +
                '}';
    }

    public String getGiftPic() {
        return giftPic;
    }

    public void setGiftPic(String giftPic) {
        this.giftPic = giftPic;
    }
}
