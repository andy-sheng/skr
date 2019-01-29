package com.common.umeng;

import com.common.statistics.UmengStatistics;
import com.common.utils.U;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

public class UmengInit {
    static boolean hasInited = false;

    public static void init() {
        if (hasInited) {
            return;
        }
        synchronized (UmengStatistics.class) {
            if (hasInited) {
                return;
            }
            UMConfigure.init(U.app(), "5c5044c1f1f556c70000049f"
                    , U.getChannelUtils().getChannel(), UMConfigure.DEVICE_TYPE_PHONE, "");
            MobclickAgent.setScenarioType(U.app(), MobclickAgent.EScenarioType.E_UM_NORMAL);
            hasInited = true;
        }
    }
}
