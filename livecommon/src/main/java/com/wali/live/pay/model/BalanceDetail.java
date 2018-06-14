package com.wali.live.pay.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.base.utils.language.LocaleUtil;
import com.mi.live.data.repository.GiftRepository;
import com.wali.live.dao.Gift;
import com.wali.live.proto.PayProto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @module 充值
 * Created by rongzhisheng on 16-8-31.
 */
public class BalanceDetail implements Serializable{
    private static final String TAG = BalanceDetail.class.getSimpleName();

    private static long ONE_DAY_SECOND = 60 * 60 * 24;
    /**真实的钻石数量（用户通过充值得到的钻石数量、金钻）*/
    private int diamondCount;
    /**未过期虚拟钻石列表*/
    private List<VirtualDiamond> virtualDiamondList;
    /**未过期礼物卡列表*/
    private List<GiftCard> giftCardList;
    /**可用的虚拟钻石数量*/
    private int usableVirtualDiamondCount;
    /**已过期虚拟钻石列表*/
    private List<VirtualDiamond> expiredVirtualDiamondList;
    /**已过期礼物卡列表*/
    private List<GiftCard> expiredGiftCardList;

    private BalanceDetail() {
        virtualDiamondList = new ArrayList<>();
        giftCardList = new ArrayList<>();
        expiredVirtualDiamondList = new ArrayList<>();
        expiredGiftCardList = new ArrayList<>();
    }

    public int getDiamondCount() {
        return diamondCount;
    }

    @NonNull
    public List<GiftCard> getGiftCardList() {
        return giftCardList;
    }

    public int getUsableVirtualDiamondCount() {
        return usableVirtualDiamondCount;
    }

    @NonNull
    public List<VirtualDiamond> getVirtualDiamondList() {
        return virtualDiamondList;
    }

    @NonNull
    public List<GiftCard> getExpiredGiftCardList() {
        return expiredGiftCardList;
    }

    @NonNull
    public List<VirtualDiamond> getExpiredVirtualDiamondList() {
        return expiredVirtualDiamondList;
    }

    /**
     *
     * @param rsp code必须为0
     * @return
     */
    public static BalanceDetail parseFrom(PayProto.QueryBalanceDetailResponse rsp) {
        BalanceDetail balanceDetail = new BalanceDetail();
        if(rsp == null){
            return balanceDetail;
        }

        balanceDetail.diamondCount = rsp.getRealGemCnt();
        balanceDetail.usableVirtualDiamondCount = rsp.getUsableVirtualGemCnt();
        long nowInSecond = System.currentTimeMillis()/1000;
        if (rsp.getVirtualGemListCount() > 0) {
            for (PayProto.VirtualGem virtualGem : rsp.getVirtualGemListList()) {
                if (nowInSecond < virtualGem.getBeginTime()) {
                    continue;
                }
                VirtualDiamond virtualDiamond = new VirtualDiamond(virtualGem.getVirtualGemCnt(), virtualGem.getEndTime());
                balanceDetail.getVirtualDiamondList().add(virtualDiamond);
            }
        }
        if (rsp.getGiftCardListCount() > 0) {
            for (PayProto.GiftCard giftCard : rsp.getGiftCardListList()) {
                if (nowInSecond < giftCard.getBeginTime()) {
                    continue;
                }
                GiftCard gc = new GiftCard(giftCard.getGiftId(), giftCard.getGiftCardCnt(), giftCard.getEndTime());
                balanceDetail.getGiftCardList().add(gc);
            }
        }
        if (rsp.hasHistory()) {
            PayProto.ExpireOrderHistoryRecord historyRecord = rsp.getHistory();
            if (historyRecord != null) {
                if (historyRecord.getCardsCount() > 0) {
                    for (PayProto.ExpireOrderRecord record : historyRecord.getCardsList()) {
                        balanceDetail.getExpiredGiftCardList().add(
                            new GiftCard(record.getGiftId(), record.getGiftCnt(), record.getEndTime())
                        );
                    }
                }
                if (historyRecord.getGemsCount() > 0) {
                    for (PayProto.ExpireOrderRecord record : historyRecord.getGemsList()) {
                        balanceDetail.getExpiredVirtualDiamondList().add(
                            new VirtualDiamond(record.getWorthGem(), record.getEndTime())
                        );
                    }
                }
            }
        }
        return balanceDetail;
    }

    /**
     * 只显示包裹礼物的信息
     * 从直播间 礼物橱窗 包裹礼物 详情点击进来的
     * @param rsp code必须为0
     * @return
     */
    public static BalanceDetail parseOnlyPktFrom(PayProto.QueryBalanceDetailResponse rsp) {
        BalanceDetail balanceDetail = new BalanceDetail();
        long nowInSecond = System.currentTimeMillis()/1000;
        if(rsp == null){
            return balanceDetail;
        }

        if (rsp.getGiftCardListCount() > 0) {
            for (PayProto.GiftCard giftCard : rsp.getGiftCardListList()) {
                if (nowInSecond < giftCard.getBeginTime()) {
                    continue;
                }
                GiftCard gc = new GiftCard(giftCard.getGiftId(), giftCard.getGiftCardCnt(), giftCard.getEndTime());
                balanceDetail.getGiftCardList().add(gc);
            }
        }
        if (rsp.hasHistory()) {
            PayProto.ExpireOrderHistoryRecord historyRecord = rsp.getHistory();
            if (historyRecord != null) {
                if (historyRecord.getCardsCount() > 0) {
                    for (PayProto.ExpireOrderRecord record : historyRecord.getCardsList()) {
                        balanceDetail.getExpiredGiftCardList().add(
                                new GiftCard(record.getGiftId(), record.getGiftCnt(), record.getEndTime())
                        );
                    }
                }
            }
        }
        return balanceDetail;
    }

    public static class VirtualDiamond implements Serializable {
        public final int count;
        public final int leftDay;
        /**注意国际化*/
        public final CharSequence expireDate;

        /**
         *
         * @param count
         * @param endTime 单位秒， 不能小于当前时间
         */
        public VirtualDiamond(int count, long endTime) {
            this.count = count;
            long leftSecond = endTime - System.currentTimeMillis()/1000;
            if (leftSecond >= 0) {
                this.leftDay = getLeftDay(endTime * 1000);
            } else {// 已过期的钻石就不必计算了
                this.leftDay = 0;
            }
            this.expireDate = getExpireDate((int) leftSecond);
        }
    }

    public static class GiftCard implements Serializable {
        public final int count;
        @Nullable public final CharSequence giftName;
        public final int totalPrice;
        public final int leftDay;
        /**注意国际化*/
        public final CharSequence expireDate;

        /**
         *
         * @param giftId
         * @param count
         * @param endTime 单位秒， 不能小于当前时间
         */
        public GiftCard(int giftId, int count, long endTime) {
            this.count = count;
            Gift gift = GiftRepository.findGiftById(giftId);
            if (gift != null) {
                this.giftName = gift.getInternationalName();
                this.totalPrice = count * gift.getPrice();
            } else {
                MyLog.e(TAG, "gift is null, giftId:" + giftId);
                this.giftName = null;
                this.totalPrice = 0;
            }
            long leftSecond =  endTime - System.currentTimeMillis() / 1000;
            if (leftSecond >= 0) {
                this.leftDay = getLeftDay(endTime * 1000);
            } else {// 已过期的礼物卡就不必计算了
                this.leftDay = 0;
            }
            this.expireDate = getExpireDate((int) leftSecond);
        }
    }

    /**
     * 已过期返回负数，今天过期返回0，否则返回剩余过期天数
     * @param endTime 毫秒
     * @return
     */
    private static int getLeftDay(long endTime) {
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar expire = new GregorianCalendar();
        expire.setTimeInMillis(endTime);

        if (expire.before(now)) {
            return -1;
        } else {
//            int nowYear = now.get(GregorianCalendar.YEAR);
//            int nowDayOfYear = now.get(GregorianCalendar.DAY_OF_YEAR);
//            int expireYear = expire.get(GregorianCalendar.YEAR);
//            int expireDayOfYear = expire.get(GregorianCalendar.DAY_OF_YEAR);
//            if (nowYear == expireYear && nowDayOfYear == expireDayOfYear) {
//                return 0;
//            }
//            // 一天已经过去的秒数
//            int secondInDay = now.get(GregorianCalendar.HOUR_OF_DAY) * 3600
//                    + now.get(GregorianCalendar.MINUTE) * 60
//                    + now.get(GregorianCalendar.SECOND);
//            // 一天剩余的秒数
//            long leftSecondInDay = ONE_DAY_SECOND - secondInDay;
//            // 最小还剩1天
//            return (int) (((endTime-now.getTimeInMillis())/1000 - leftSecondInDay) / ONE_DAY_SECOND) + 1;
            return (int) (((endTime - now.getTimeInMillis()) / 1000) / ONE_DAY_SECOND + 1);
        }
    }

    private static CharSequence getExpireDate(int leftSecond) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(GregorianCalendar.SECOND, leftSecond);
        java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM,
                LocaleUtil.getLocale());
        return dateFormat.format(calendar.getTime());
    }

}
