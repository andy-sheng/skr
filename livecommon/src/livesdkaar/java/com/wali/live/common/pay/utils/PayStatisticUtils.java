package com.wali.live.common.pay.utils;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.wali.live.common.pay.constant.PayWay;
import com.wali.live.statistics.StatisticsKey;

/**
 * Created by chengsimin on 2016/12/5.
 */

public class PayStatisticUtils {
    @CheckResult
    public static String getRechargeTemplate(@NonNull String template, @NonNull PayWay payWay, Object... args) {
        if (args.length > 0) {
            Object[] array = new Object[args.length + 1];
            array[0] = getPayWayAbbr(payWay);
            System.arraycopy(args, 0, array, 1, args.length);
            return String.format(template, array);
        }
        String log = "";
        try {
            log = String.format(template, getPayWayAbbr(payWay));
        } catch (Exception e) {
            MyLog.e("StatisticsKey", "format recharge scribe log fail", e);
        }
        return log;
    }
    @CheckResult
    private static String getPayWayAbbr(@NonNull PayWay payWay) {
        switch (payWay) {
            case WEIXIN:
                return StatisticsKey.Recharge.PayWay.WeiXin;
            case ZHIFUBAO:
                return StatisticsKey.Recharge.PayWay.Alipay;
            case MIWALLET:
                return StatisticsKey.Recharge.PayWay.MiWallet;
//            case GOOGLEWALLET:
//                return StatisticsKey.Recharge.PayWay.GoogleWallet;
//            case PAYPAL:
//                return StatisticsKey.Recharge.PayWay.PayPal;
            default:
                return "";
        }
    }
}
