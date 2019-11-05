package com.common.statistics;

import android.app.Activity;
import android.text.TextUtils;

import com.common.cache.LruCache;
import com.common.log.MyLog;
import com.common.statistics.umeng.UmengStatistics;
import com.common.utils.U;

import java.util.HashMap;

/**
 * 目前适配了小米统计打点
 * 小米统计打点相关的api文档
 * https://dev.mi.com/console/doc/detail?pId=117
 * 强烈建议使用前仔细阅读api文档
 */
public class StatisticsAdapter {

    public static final String TAG = "StatisticsAdapter";

//    static boolean useXiaomi = false;
    static boolean userUmeng = true;
    /**
     * 去抖动，防止因为某些bug，快速的重复打某个点，
     * 比如某些情况下 Fragment 的 onresume 调用了多次
     */
    static LruCache<String, Long> debounceLruCache = new LruCache<>(10);

    public static void recordSessionStart(Activity activity, String simpleName) {
//        if (useXiaomi) {
//            XiaoMiStatistics.recordPageStart(activity, simpleName);
//        }
        if (userUmeng) {
            UmengStatistics.recordSessionStart(activity, simpleName);
        }
    }

    public static void recordSessionEnd(Activity activity, String simpleName) {
//        if (useXiaomi) {
//            XiaoMiStatistics.recordPageEnd(activity, simpleName);
//        }
        if (userUmeng) {
            UmengStatistics.recordSessionEnd(activity, simpleName);
        }
    }

    public static void recordPageStart(Activity activity, String pageName) {
//        if (useXiaomi) {
//            XiaoMiStatistics.recordPageStart(activity, pageName);
//        }
        if (userUmeng) {
            UmengStatistics.recordPageStart(pageName);
        }
    }

    public static void recordPageEnd(Activity activity, String pageName) {
//        if (useXiaomi) {
//            XiaoMiStatistics.recordPageEnd(activity, pageName);
//        }
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
    public static void recordCountEvent(String category, String key, HashMap<String,String> params) {
        recordCountEvent(category,key,params,false);
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
     * @param onlyOneTimeInAppLifeCycle 在app生命周期内这个点只会打一次，除非app卸载，常用于注册 登录等漏斗转化
     */
    public static void recordCountEvent(String category, String key, HashMap<String,String> params,boolean onlyOneTimeInAppLifeCycle) {
//        if (useXiaomi) {
//            MiStatInterface.recordCountEvent(category, key, params);
//        }
        if (userUmeng) {
            String s = key;
            if (!TextUtils.isEmpty(category)) {
                s = category + "_" + key;
            }
            Long ts = debounceLruCache.get(s);
            long now = System.currentTimeMillis();
            if (ts != null && now - ts < 400) {
                MyLog.d(TAG, "recordCountEvent 删除" + s + ",去抖动");
            } else {
                if(onlyOneTimeInAppLifeCycle){
                    if(U.getPreferenceUtils().getSettingBoolean(U.getPreferenceUtils().longlySp(),s,false)){
                       //  如果打过
                        MyLog.d(s+" 这个点在App周期内已经打过了 cancel");
                        return;
                    }
                    U.getPreferenceUtils().setSettingBoolean(U.getPreferenceUtils().longlySp(),s,true);
                }
                debounceLruCache.put(s, now);
                UmengStatistics.recordCountEvent(s, params);
            }
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
//        if (useXiaomi) {
//            MiStatInterface.recordCalculateEvent(category, key, value, params);
//        }
        if (userUmeng) {
            String s = key;
            if (!TextUtils.isEmpty(category)) {
                s = category + "_" + key;
            }
            UmengStatistics.recordCalculateEvent(s, params, (int) value);
        }

    }

    public static void recordPropertyEvent(String category, String key, String value) {
//        if (useXiaomi) {
//            MiStatInterface.recordStringPropertyEvent(category, key, value);
//        }
        if (userUmeng) {

        }
    }
}
