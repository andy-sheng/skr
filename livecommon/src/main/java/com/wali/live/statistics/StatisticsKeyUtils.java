package com.wali.live.statistics;

import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.pay.constant.PayWay;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wali.live.statistics.StatisticsKey.KET_TYPE_FEEDS_VIDEO;
import static com.wali.live.statistics.StatisticsKey.KEY_LOGIN_TYPE_FACEBOOK;
import static com.wali.live.statistics.StatisticsKey.KEY_LOGIN_TYPE_GOOGLE;
import static com.wali.live.statistics.StatisticsKey.KEY_LOGIN_TYPE_INSTAGRAM;
import static com.wali.live.statistics.StatisticsKey.KEY_LOGIN_TYPE_MI;
import static com.wali.live.statistics.StatisticsKey.KEY_LOGIN_TYPE_MISSO;
import static com.wali.live.statistics.StatisticsKey.KEY_LOGIN_TYPE_QQ;
import static com.wali.live.statistics.StatisticsKey.KEY_LOGIN_TYPE_WB;
import static com.wali.live.statistics.StatisticsKey.KEY_LOGIN_TYPE_WX;
import static com.wali.live.statistics.StatisticsKey.KEY_TYPE_FEEDS_JOURNAL;
import static com.wali.live.statistics.StatisticsKey.KEY_TYPE_FEEDS_PIC;
import static com.wali.live.statistics.StatisticsKey.KEY_TYPE_FEEDS_PLAYBACK;
import static com.wali.live.statistics.StatisticsKeyUtils.RechargeScribeParam.param;

/**
 * Created by lan on 16/11/22.
 */
public class StatisticsKeyUtils {

    private static final Pattern RECHARGE_PLACEHOLDER_PATTERN = Pattern.compile("(%s)");

    @CheckResult
    public static String getRechargeTemplate(@NonNull String template, @NonNull PayWay payWay, Object... args) {
        Matcher matcher = RECHARGE_PLACEHOLDER_PATTERN.matcher(template);
        int n = 0;
        while (matcher.find()) {
            n++;
        }
        n -= args.length;
        if (n < 1) {
            throw new IllegalStateException("missing from in template:" + template);
        }

        Object[] array = new Object[args.length + n];//至少有from，不一定有payway
        boolean hasPayWay = n > 1;
        if (hasPayWay) {// 说明有payway和from，payway在第一个占位符上
            array[0] = payWay.getAbbr();
        }
        int from = StatisticsKey.Recharge.FROM_OTHER;
        if (param != null) {
            try {
                from = param.getInt(StatisticsKey.Recharge.RECHARGE_FROM, StatisticsKey.Recharge.FROM_OTHER);
            } catch (Exception e) {
                // 可能被其他线程修改
                MyLog.w("StatisticsKeyUtils", "RechargeScribeParam.param is null");
            }
        }
        array[array.length - 1] = from;//from一定时在最后的
        if (args.length > 0) {
            System.arraycopy(args, 0, array, hasPayWay ? 1 : 0, args.length);
        }
        String log = "";
        try {
            log = String.format(template, array);
        } catch (Exception e) {
            MyLog.e("StatisticsKey", "format recharge scribe log fail", e);
        }
        return log;
    }

    /**
     * 由于打点位置比较分散，把参数放在RechargeFragment的{@code argument}里的话，
     * 需要传递Fragment的引用或Bundle，比较麻烦，且不利于降低耦合度。
     * 考虑到同一时刻只能通过一个入口进行充值，所以把打点所需的参数集中配置也不会有问题，只需进入充值界面前修改一下这个类里的值即可。
     * <p>
     * Created by rongzhisheng on 16-12-30.
     */
    public static class RechargeScribeParam {
        /**
         * 表示从哪里进入充值界面
         * 主线程赋值，但有可能在非主线程访问
         *
         * @see Recharge
         */
        @Nullable
        public volatile static Bundle param;
    }

}
