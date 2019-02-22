package com.common.statistics;

import android.app.Activity;
import android.text.TextUtils;

import com.common.log.MyLog;
import com.xiaomi.mistatistic.sdk.MiStatInterface;

import java.util.HashMap;

/**
 * 目前适配了小米统计打点
 * 小米统计打点相关的api文档
 * https://dev.mi.com/console/doc/detail?pId=117
 * 强烈建议使用前仔细阅读api文档
 */
public class StatisticsAdapter {

    public final static String TAG = "StatisticsAdapter";

    static boolean useXiaomi = false;
    static boolean userUmeng = true;

    public static void recordSessionStart(Activity activity, String simpleName) {
        if (useXiaomi) {
            XiaoMiStatistics.recordPageStart(activity, simpleName);
        }
        if (userUmeng) {
            UmengStatistics.recordSessionStart(activity, simpleName);
        }
    }

    public static void recordSessionEnd(Activity activity, String simpleName) {
        if (useXiaomi) {
            XiaoMiStatistics.recordPageEnd(activity, simpleName);
        }
        if (userUmeng) {
            UmengStatistics.recordSessionEnd(activity, simpleName);
        }
    }

    public static void recordPageStart(Activity activity, String pageName) {
        if (useXiaomi) {
            XiaoMiStatistics.recordPageStart(activity, pageName);
        }
        if (userUmeng) {
            UmengStatistics.recordPageStart(pageName);
        }
    }

    public static void recordPageEnd(Activity activity, String pageName) {
        if (useXiaomi) {
            XiaoMiStatistics.recordPageEnd(activity, pageName);
        }
        if (userUmeng) {
            UmengStatistics.recordPageEnd(pageName);
        }
    }

    /**
     * 计数事件
     * 如 统计礼物面板的打开次数 统计收藏按钮的点击次数等
     * 可产生一次相应的计数事件
     * 例 MiStatInterface.recordCountEvent("Button_Click", "Button_OK_click",null);
     * 统计后台会对这类事件做总发生次数、总覆盖用户数等统计计算
     *
     * @param category 类别
     * @param key      主key
     * @param params   参数 可为null
     */
    public static void recordCountEvent(String category, String key, HashMap params) {
        if (useXiaomi) {
            MiStatInterface.recordCountEvent(category, key, params);
        }
        if (userUmeng) {
            String s = key;
            if (!TextUtils.isEmpty(category)) {
                s = category + "_" + key;
            }
            UmengStatistics.recordCountEvent(s, params);
        }
    }

    /**
     * 计算事件
     * 适用的场景如用户消费事件，附带的数值是每次消费的金额；下载文件事件，附带的数值是每次下载消耗的时长等
     * 例 MiStatInterface.recordCalculateEvent(“user_pay”, "buy_ebook", 20);
     * 统计后台会对这类事件做累加、分布、按次平均、按人平均等统计计算
     *
     * @param category
     * @param key
     * @param value    一个long型的整数，可以是下载的耗时，也可以是消费的金额
     * @param params
     */
    public static void recordCalculateEvent(String category, String key, long value, HashMap params) {
        if (useXiaomi) {
            MiStatInterface.recordCalculateEvent(category, key, value, params);
        }
        if (userUmeng) {
            String s = key;
            if (!TextUtils.isEmpty(category)) {
                s = category + "_" + key;
            }
            UmengStatistics.recordCalculateEvent(s, params, (int) value);
        }

    }

    public static void recordPropertyEvent(String category, String key, String value) {
        if(useXiaomi) {
            MiStatInterface.recordStringPropertyEvent(category, key, value);
        }
        if(userUmeng){

        }
    }
}
