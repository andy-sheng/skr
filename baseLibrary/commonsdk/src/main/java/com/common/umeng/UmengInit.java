package com.common.umeng;

import android.app.Notification;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import com.common.base.R;
import com.common.log.MyLog;
import com.common.statistics.UmengStatistics;
import com.common.utils.U;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import org.android.agoo.xiaomi.MiPushRegistar;

import java.util.Map;

public class UmengInit {
    public final static String TAG = "UmengInit";
    private static boolean hasInited = false;

    /**
     * 需要保证线性不交叉
     * 每个 onPageStart 都有一个 onPageEnd 配对。这样才能保证每个页面统计的正确。
     */
    public static void init() {
        if (hasInited) {
            return;
        }
        synchronized (UmengStatistics.class) {
            if (hasInited) {
                return;
            }
            Log.d(TAG, "UmengInit init 友盟初始化开始 " + U.getProcessName());
            UMConfigure.init(U.app(), "5bf40cc8f1f556f36200032b"
                    , U.getChannelUtils().getChannel(), UMConfigure.DEVICE_TYPE_PHONE, "34d3e8844e007050b8a968d974f1adee");
            //MobclickAgent.setScenarioType(U.app(), MobclickAgent.EScenarioType.E_UM_NORMAL);
            // 选用MANUAL页面采集模式
            //默认SDK对Activity页面进行自动统计，现在我们自己统计，所以需要把自动统计关掉
            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL);
            UmengPush.pushInit();
            hasInited = true;
        }
    }


}
