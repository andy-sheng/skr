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

    public static void setExchangeableGemCnt(int sExchangeableGemCnt) {
        RechargeInfo.sExchangeableGemCnt = sExchangeableGemCnt;
    }

    public static int getMiWalletTodayAmount() {
        return sMiWalletTodayAmount;
    }

    public static void setMiWalletTodayAmount(int sMiWalletTodayAmount) {
        RechargeInfo.sMiWalletTodayAmount = sMiWalletTodayAmount;
    }

    public static int getWeiXinTodayAmount() {
        return sWeiXinTodayAmount;
    }

    public static void setWeiXinTodayAmount(int sWeiXinTodayAmount) {
        RechargeInfo.sWeiXinTodayAmount = sWeiXinTodayAmount;
    }

    public static int getWillExpireGemCnt() {
        return sWillExpireGemCnt;
    }

    public static void setWillExpireGemCnt(int sWillExpireGemCnt) {
        RechargeInfo.sWillExpireGemCnt = sWillExpireGemCnt;
    }

    public static int getWillExpireGiftCardCnt() {
        return sWillExpireGiftCardCnt;
    }

    public static void setWillExpireGiftCardCnt(int sWillExpireGiftCardCnt) {
        RechargeInfo.sWillExpireGiftCardCnt = sWillExpireGiftCardCnt;
    }

    public static int getUsableGemCount() {
        return sUsableGemCount;
    }

    public static void setUsableGemCount(int sUsableGemCount) {
        RechargeInfo.sUsableGemCount = sUsableGemCount;
    }

    public static int getUsableVirtualGemCount() {
        return sUsableVirtualGemCount;
    }

    public static void setUsableVirtualGemCount(int sUsableVirtualGemCount) {
        RechargeInfo.sUsableVirtualGemCount = sUsableVirtualGemCount;
    }

}
