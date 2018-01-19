package com.wali.live.watchsdk.contest.util;

import java.text.DecimalFormat;

/**
 * Created by lan on 2018/1/18.
 */
public class FormatUtils {
    public static String formatMoney(float money) {
        if (money == 0) {
            return "0";
        } else {
            DecimalFormat df = new DecimalFormat("#0.00");
            return df.format(money);
        }
    }
}
