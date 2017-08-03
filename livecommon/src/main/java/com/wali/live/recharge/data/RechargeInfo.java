package com.wali.live.recharge.data;

import android.support.annotation.Nullable;

import com.wali.live.pay.model.Diamond;

import java.util.List;
import java.util.Vector;

/**
 * Created by rongzhisheng on 17-1-1.
 */

public class RechargeInfo {
    private static volatile List<Diamond> sGemCache = new Vector<>();
    private static volatile int sExchangeableGemCnt;
    private static volatile int sWillExpireGemCnt;
    private static volatile int sWillExpireGiftCardCnt;
    private static volatile int sWeiXinTodayAmount;//微信当日已支付金额，单位：分
    private static volatile int sMiWalletTodayAmount;//小米钱包当日已支付金额，单位：分
    private static volatile int sUsableGemCount;//可用钻石数
    private static volatile int sUsableVirtualGemCount;//可用虚拟钻石数量

    public static List<Diamond> getGemCache() {
        return sGemCache;
    }

    public static void replaceGemCache(@Nullable List<Diamond> diamondList) {
        sGemCache.clear();
        if (diamondList != null && !diamondList.isEmpty()) {
            sGemCache.addAll(diamondList);
        }
    }

    public static int getExchangeableGemCnt() {
        return sExchangeableGemCnt;
    }

    public static void setExchangeableGemCnt(int exchangeableGemCnt) {
        RechargeInfo.sExchangeableGemCnt = exchangeableGemCnt;
    }

    public static int getMiWalletTodayAmount() {
        return sMiWalletTodayAmount;
    }

    public static void setMiWalletTodayAmount(int miWalletTodayAmount) {
        RechargeInfo.sMiWalletTodayAmount = miWalletTodayAmount;
    }

    public static int getWeiXinTodayAmount() {
        return sWeiXinTodayAmount;
    }

    public static void setWeiXinTodayAmount(int weiXinTodayAmount) {
        RechargeInfo.sWeiXinTodayAmount = weiXinTodayAmount;
    }

    public static int getWillExpireGemCnt() {
        return sWillExpireGemCnt;
    }

    public static void setWillExpireGemCnt(int willExpireGemCnt) {
        RechargeInfo.sWillExpireGemCnt = willExpireGemCnt;
    }

    public static int getWillExpireGiftCardCnt() {
        return sWillExpireGiftCardCnt;
    }

    public static void setWillExpireGiftCardCnt(int willExpireGiftCardCnt) {
        RechargeInfo.sWillExpireGiftCardCnt = willExpireGiftCardCnt;
    }

    public static int getUsableGemCount() {
        return sUsableGemCount;
    }

    public static void setUsableGemCount(int usableGemCount) {
        RechargeInfo.sUsableGemCount = usableGemCount;
    }

    public static int getUsableVirtualGemCount() {
        return sUsableVirtualGemCount;
    }

    public static void setUsableVirtualGemCount(int usableVirtualGemCount) {
        RechargeInfo.sUsableVirtualGemCount = usableVirtualGemCount;
    }

}
