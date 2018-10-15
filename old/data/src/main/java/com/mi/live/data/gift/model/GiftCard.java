package com.mi.live.data.gift.model;

import com.wali.live.proto.EffectProto;
import com.wali.live.proto.GiftProto;
import com.wali.live.proto.PayProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengsimin on 16/7/27.
 */
public class GiftCard {
    private int giftId;
    private int giftCardCount;
    private long beginTime;
    private long endTime;

    public int getGiftId() {
        return giftId;
    }

    public void setGiftId(int giftId) {
        this.giftId = giftId;
    }

    public int getGiftCardCount() {
        return giftCardCount;
    }

    public void setGiftCardCount(int giftCardCount) {
        this.giftCardCount = giftCardCount;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public static GiftCard loadFromPB(EffectProto.GiftCard card) {
        GiftCard giftCard = new GiftCard();
        giftCard.giftId = card.getGiftId();
        giftCard.giftCardCount = card.getGiftCardCnt();
        giftCard.beginTime = card.getBeginTime();
        giftCard.endTime = card.getEndTime();
        return giftCard;
    }

    public static GiftCard loadFromPB(GiftProto.VGiftCard card) {
        GiftCard giftCard = new GiftCard();
        giftCard.giftId = card.getGiftId();
        giftCard.giftCardCount = card.getGiftCardCnt();
        giftCard.beginTime = 0;
        giftCard.endTime = card.getEndTime();
        return giftCard;
    }

    public static GiftCard loadFromPB(PayProto.GiftCard card) {
        GiftCard giftCard = new GiftCard();
        giftCard.giftId = card.getGiftId();
        giftCard.giftCardCount = card.getGiftCardCnt();
        giftCard.beginTime = 0;
        giftCard.endTime = card.getEndTime();
        return giftCard;
    }

    public static void copy(GiftCard from,GiftCard to) {
        to.giftCardCount = from.giftCardCount;
        to.beginTime = from.beginTime;
        to.endTime = from.endTime;
    }

    @Override
    public String toString() {
        return "GiftCard{" +
                "giftId=" + giftId +
                ", giftCardCount=" + giftCardCount +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                '}';
    }

    public static List<GiftCard> convert(List<PayProto.GiftCard> giftCardsList) {
        List<GiftCard> list = new ArrayList<>();
        if(giftCardsList!=null) {
            for (PayProto.GiftCard card : giftCardsList) {
                list.add(loadFromPB(card));
            }
        }
        return list;
    }
}
