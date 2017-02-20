package com.wali.live.statistics;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.channel.ReleaseChannelUtils;
import com.base.utils.network.Network;
import com.mi.live.data.account.UserAccountManager;
import com.mi.milink.sdk.debug.MiLinkMonitor;

/**
 * Created by yurui on 3/17/16.
 */
public class StatisticUtils {
    public static final String TAG = StatisticUtils.class.getSimpleName();

    private static boolean LOG_ENABLE = true;

    public static final int SUCCESS = 0;
    public static final int FAILED = 1;

    /**
     * @return
     */
    public static final boolean canDoStatistic() {
        //只有有账号，或者没帐号时不时CTA包才可以打点数据
        return (UserAccountManager.getInstance().hasAccount() || !ReleaseChannelUtils.isMIUICTAPkg());
    }

    public static void addToMiLinkMonitor(final String key, int code, long requestTime, long responseTime) {
        try {
            if (!canDoStatistic()) {
                return;
            }
            if (Network.hasNetwork(GlobalData.app())) {
                MiLinkMonitor.getInstance().trace(StatisticsKey.AC_CALL_FACTOR, "", 0, key, code, requestTime,
                        responseTime, 0, 0, 0, UserAccountManager.getInstance().getUuidAsLong() + "");
                MiStatAdapter.recordCalculateEvent(null, key + "_" + code, 1);
            }
            if (LOG_ENABLE) {
                MyLog.v(StatisticUtils.TAG + " " + key + " " + (responseTime - requestTime));
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
    }

    public static void addToMiLinkMonitor(final String key, int code) {
        addToMiLinkMonitor(key, code, "");
    }

    public static void addToMiLinkMonitor(final String key, int code, String info) {
        if (!canDoStatistic()) {
            return;
        }
        if (Network.hasNetwork(GlobalData.app())) {
            MiLinkMonitor.getInstance().trace(StatisticsKey.AC_CALL_FACTOR, info, 0, key, code, 0,
                    0, 0, 0, 0, UserAccountManager.getInstance().getUuidAsLong() + "");
            MiStatAdapter.recordCalculateEvent(null, key + "_" + code, 1);
        }
        if (LOG_ENABLE) {
            MyLog.v(StatisticUtils.TAG + " " + key);
        }
    }
}
