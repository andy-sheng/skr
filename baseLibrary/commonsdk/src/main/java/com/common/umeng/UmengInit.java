package com.common.umeng;

import android.util.Log;

import com.common.statistics.UmengStatistics;
import com.common.utils.U;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

public class UmengInit {
    private static boolean hasInited = false;

    public static void init() {
        if (hasInited) {
            return;
        }
        synchronized (UmengStatistics.class) {
            if (hasInited) {
                return;
            }
            Log.d("SKRER", "UmengInit init 友盟初始化");
            UMConfigure.init(U.app(), "5bf40cc8f1f556f36200032b"
                    , U.getChannelUtils().getChannel(), UMConfigure.DEVICE_TYPE_PHONE, "");
            //UMConfigure.setLogEnabled(true);
            MobclickAgent.setScenarioType(U.app(), MobclickAgent.EScenarioType.E_UM_NORMAL);
            hasInited = true;
        }
    }
}
