package com.wali.live.modulechannel.helper;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.wali.live.modulechannel.model.viewmodel.BaseJumpItem;


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

        //TOdo-暂时去除打点
//        MyLog.d(TAG, "exposure tag=" + tag);
//        if (!TextUtils.isEmpty(tag)) {
//            MilinkStatistics.getInstance().statisticsChannelExposure(tag);
//        }
    }

    /**
     * 停留1s曝光打点统计
     */
    public static void sendStayExposureCommand(BaseJumpItem item, long channelId) {
        //TOdo-暂时去除打点
//        if (item == null) {
//            return;
//        }
//        String tag = item.getRecommendTag();
//        if (!TextUtils.isEmpty(tag)) {
//            MilinkStatistics.getInstance().statisticStayExposure(MyUserInfoManager.getInstance().getUuid(), tag, channelId);
//        }
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
        //TOdo-暂时去除打点
//        MyLog.d(TAG, "click tag=" + tag);
//        if (!TextUtils.isEmpty(tag)) {
//            MilinkStatistics.getInstance().statisticsChannelClick(tag);
//        }
    }
}
