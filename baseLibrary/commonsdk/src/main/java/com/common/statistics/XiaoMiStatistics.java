package com.common.statistics;

/**
 * 目前适配了小米统计打点
 * 小米统计打点相关的api文档
 * https://dev.mi.com/console/doc/detail?pId=117
 * 强烈建议使用前仔细阅读api文档
 * <p>
 * <p>
 * 使用StatisticsAdapter
 */
public class XiaoMiStatistics {
//
//    public final static String TAG = "XiaoMiStatistics";
//
////    static final String MI_STAT_APP_ID = "2882303761517890001";
////    static final String MI_STAT_APP_KEY = "5671789084001";
//
//    static final String MI_STAT_APP_ID = "2882303761517932750";
//    static final String MI_STAT_APP_KEY = "5701793259750";
//
//    static boolean hasInited = false;
//
//    private static void init() {
//        if (hasInited) {
//            return;
//        }
//        synchronized (XiaoMiStatistics.class) {
//            if (hasInited) {
//                return;
//            }
//            MyLog.d(TAG, "init");
//            MiStatInterface.initialize(U.app(), MI_STAT_APP_ID, MI_STAT_APP_KEY, U.getChannelUtils().getChannel());
//            /**
//             * UPLOAD_POLICY_REALTIME 实时上报。每当有一条新的记录，就会激发一次上报。
//             * UPLOAD_POLICY_WIFI_ONLY 只在WIFI下上报。当设备处于WIFI连接时实时上报，否则不上报记录。
//             * UPLOAD_POLICY_BATCH 批量上报。当记录在本地累积超过一个固定值时（50条），会触发一次上报。
//             * UPLOAD_POLICY_WHILE_INITIALIZE 启动时候上报。每次应用启动（调用initialize方法）时候，会将上一次应用使用产生的数据记录打包上报。
//             * UPLOAD_POLICY_INTERVAL 指定时间间隔上报。开发者可以指定从1分钟-1天之间的任意时间间隔上报数据记录。需要注意，由于SDK并没有使用安卓的实时唤醒机制，因此采用此策略上报，SDK做不到严格的遵守开发者设定的间隔，而会根据应用数据采集的频率和设备休眠策略，会有一定的偏差。
//             * UPLOAD_POLICY_DEVELOPMENT 调试模式。使用此策略，只有开发者手动调用一个接口才会触发上报，否则在任何情况下都不上报。SDK中提供了一个triggerUploadManually方法用于手动触发。
//             */
//            MiStatInterface.setUploadPolicy(MiStatInterface.UPLOAD_POLICY_INTERVAL, 5 * 60 * 1000);
//            /**
//             * 开启实时网络监控功能
//             */
//            URLStatsRecorder.enableAutoRecord();
//            hasInited = true;
//        }
//    }
//
//    /**
//     * 记录某个页面被打开，并在sdk内部会创建一个session
//     *
//     * @param context 必填
//     * @param key
//     */
//    static void recordPageStart(Context context, String key) {
//        MyLog.d(TAG, "recordPageStart" + " key=" + key);
//        init();
//        MiStatInterface.recordPageStart(context, key);
//    }
//
//    /**
//     * 记录某个页面被关闭
//     *
//     * @param context
//     * @param key     必填，确保与recordActivityPageStart的一致
//     */
//    static void recordPageEnd(Context context, String key) {
//        MyLog.d(TAG, "recordPageEnd" + " key=" + key);
//        init();
//        MiStatInterface.recordPageEnd(context, key);
//    }
//
//
//    /**
//     * 字符串属性事件
//     * 字符串属性类型通常用来描述某个具备字符串特征的属性，适用的场景如用户性别、用户职业、用户爱好等，这类属性的取值是一个字符串值
//     * 例 MiStatInterface.recordStringPropertyEvent(“user_profile”, "genda", "female");
//     * 每次调用需要传入分类、主键和字符串值。对于同一主键的字符串属性，一个设备只会保存一个，
//     * 即，一个设备上报多次同一个主键的字符串属性类型，统计服务后台只会保存和统计最新的属性。
//     *
//     * @param category
//     * @param key
//     * @param desc
//     */
//    static void recordPropertyEvent(String category, String key, String desc) {
//        init();
//        MiStatInterface.recordStringPropertyEvent(category, key, desc);
//    }
//
//    /**
//     * 数值属性事件
//     * 数值属性类型通常用来描述某个具备数值特征的属性，适用的场景如用户年龄、工作年限、游戏等级等，这类属性的取值是一个整型数值。这
//     * 例MiStatInterface.recordNumericEvent(“user_profile”, "age", 26);
//     * 和 {字符串属性事件} 一样，也会覆盖
//     *
//     * @param category
//     * @param key
//     * @param value
//     */
//    static void recordPropertyEvent(String category, String key, long value) {
//        init();
//        MiStatInterface.recordNumericPropertyEvent(category, key, value);
//    }
//
//    /**
//     * 计数事件
//     * 如 统计礼物面板的打开次数 统计收藏按钮的点击次数等
//     * 可产生一次相应的计数事件
//     * 例 MiStatInterface.recordCountEvent("Button_Click", "Button_OK_click",null);
//     * 统计后台会对这类事件做总发生次数、总覆盖用户数等统计计算
//     *
//     * @param category 类别
//     * @param key      主key
//     * @param params   参数 可为null
//     */
//    static void recordCountEvent(String category, String key, HashMap params) {
//        init();
//        MiStatInterface.recordCountEvent(category, key, params);
//    }
//
//    /**
//     * 计算事件
//     * 适用的场景如用户消费事件，附带的数值是每次消费的金额；下载文件事件，附带的数值是每次下载消耗的时长等
//     * 例 MiStatInterface.recordCalculateEvent(“user_pay”, "buy_ebook", 20);
//     * 统计后台会对这类事件做累加、分布、按次平均、按人平均等统计计算
//     *
//     * @param category
//     * @param key
//     * @param value    一个long型的整数，可以是下载的耗时，也可以是消费的金额
//     * @param params
//     */
//    static void recordCalculateEvent(String category, String key, long value, HashMap params) {
//        init();
//        MiStatInterface.recordCalculateEvent(category, key, value, params);
//    }

}
