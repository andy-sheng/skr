package com.wali.live.watchsdk.channel.helper;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.mi.live.data.account.MyUserInfoManager;
import com.wali.live.watchsdk.channel.viewmodel.BaseJumpItem;
import com.wali.live.watchsdk.statistics.MilinkStatistics;

/**
 * Created by lan on 2017/8/31.
 */
public class HolderHelper {
    private final static String TAG = HolderHelper.class.getSimpleName();

    /**
     * 曝光打点统计
     */
    public static void sendExposureCommand(BaseJumpItem item) {
        if (item == null) {
            return;
        }
        String tag = item.getRecommendTag();
        sendExposureCommand(tag);
    }

    public static void sendExposureCommand(String tag) {
        MyLog.d(TAG, "exposure tag=" + tag);
        if (!TextUtils.isEmpty(tag)) {
            MilinkStatistics.getInstance().statisticsChannelExposure(tag);
        }
    }

    /**
     * 停留1s曝光打点统计
     */
    public static void sendStayExposureCommand(BaseJumpItem item) {
        if (item == null) {
            return;
        }
        String tag = item.getRecommendTag();
        if (!TextUtils.isEmpty(tag)) {
            MilinkStatistics.getInstance().statisticStayExposure(MyUserInfoManager.getInstance().getUuid(), tag);
        }
    }


    /**
     * 点击打点统计
     */
    public static void sendClickCommand(BaseJumpItem item) {
        if (item == null) {
            return;
        }
        String tag = item.getRecommendTag();
        sendClickCommand(tag);
    }

    public static void sendClickCommand(String tag) {
        MyLog.d(TAG, "click tag=" + tag);
        if (!TextUtils.isEmpty(tag)) {
            MilinkStatistics.getInstance().statisticsChannelClick(tag);
        }
    }
}
