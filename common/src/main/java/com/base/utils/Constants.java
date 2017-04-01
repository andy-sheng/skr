package com.base.utils;

import com.base.utils.channel.ReleaseChannelUtils;

/**
 * @module 常量
 * Created by linjinbin on 15/11/2.
 */
public class Constants {

    public static final String ReleaseChannel = "@SHIP.TO.2A2FE0D7@";
//    public static final String ReleaseChannel = "TEST";

    public static final boolean isDefaultChanel = ReleaseChannel.contains("2A2FE0D7");

    public static final String DEBUG_CHANNEL = "DEBUG";
    public static final String DB_CHANNEL = "DB";
    public static final String TEST_CHANNEL = "TEST";
    public static final String GOOGLE_PLAY_CHANNEL = "meng_1254_11_android";
    public static final String INDIA_CHANNEL = "meng_1332_1_android";
    public static final String DEBUGMI_CHANNEL = "debugmi";

    public static final boolean isDebugOrTestBuild = DEBUG_CHANNEL.equals(ReleaseChannelUtils.getReleaseChannel()) || TEST_CHANNEL.equals(ReleaseChannelUtils.getReleaseChannel());
    public static final boolean isDebugBuild = DEBUG_CHANNEL.equals(ReleaseChannelUtils.getReleaseChannel());
    public static final boolean isDailyBuild = DB_CHANNEL.equals(ReleaseChannelUtils.getReleaseChannel());
    public static final boolean isTestBuild = TEST_CHANNEL.equals(ReleaseChannelUtils.getReleaseChannel());
    public static final boolean isRCBuild = "RC".equals(ReleaseChannelUtils.getReleaseChannel());
    public static final boolean isGooglePlayBuild = GOOGLE_PLAY_CHANNEL.equals(ReleaseChannelUtils.getReleaseChannel());
    public static final boolean isDebugMiChanel = DEBUGMI_CHANNEL.equals(ReleaseChannel);
    public static final boolean isIndiaBuild = INDIA_CHANNEL.equals(ReleaseChannelUtils.getReleaseChannel());

    public static final String APPNAME = "WALI_LIVE_SDK";

    public static final int MILINK_APP_ID = 10008;

    public static final String SID = "miui-social";

    public static final String LOGTAG = "WALI_LIVE";

    public static final int KEEP_LOG_DAYS = 4;//log保持时间4天

    public static final long TIME_ONE_HOUR = 60 * 60 * 1000;//一个小时
    public static final long TIME_ONE_MINUTE = 60 * 1000;
    public static String XiaoMiAccountTail = "@xiaomi.com";

    /**
     * 头像链接拼接方式
     * 第一个%d：uid；
     * 第一个%s：用来裁切缩略图
     * 第二个%d：timeStamp，没有服务器时间戳，采用本地时间戳
     */
    public static String AVATAR_URL = "http://dl.zb.mi.com/%d%s?timestamp=%d";
    /**
     * 不带时间戳的头像拼接方法
     */
    public static String AVATAR_DEFAULT_URL = "http://dl.zb.mi.com/%d%s";

    public static String DEFAULT_DOMAIN = "dl.zb.mi.com";
    //选图选视频
    public static final String FILE = "file";
    public static final String CONTENT = "content";

    public static final String CRASHEYE_APPID = isDebugOrTestBuild ? "b7b0b9c0" : "98f83170";//CrashEye的AppId

    public static final String MI_STATISTIC_APPID = "2882303761517438806";
    public static final String MI_STATISTIC_APPKEY = "5431743870806";
    public static final int PLAYER_BUFFER_MAX_TIME_LIMIT = 8;//秒
    public static final int PLAYER_BUFFER_MIN_TIME_LIMIT = 4;//秒
    public static final int PLAYER_KADUN_RELOAD_TIME = 5000;//毫秒

    //翻页默认offset和一次拉取最大值
    public static final int DEFAULT_OFFSET = 0;
    public static final int DEFAULT_LIMIT = 20;
    public static final int DEFAULT_FRIEND_SERARCH_LIMIT = 40;

    public static final int ScrollToTopTipsCount = Integer.MAX_VALUE;
    public static final String AGREEMENT_URL = "http://live.mi.com/cash/agreement.html";
    //密码的最小长度
    public static final int PASSWORD_MIN_LENGTH = 6;
    //密码的最大长度
    public static final int PASSWORD_MAX_LENGTH = 16;
    //两次验证码短信发送的时间间隔60s
    public static final int SEND_SMS_INTERVAL = 60;
    //昵称的最大长度
    public static final int NICKNAME_MAX_LEN = 20;

    public static int RECORD_MAX_TIME = 16 * 1000;//最大录制时间
    public static final String GOOGLE_OAUTH2_PREFIX = "oauth2:%s";

    public static final int REFRESH_WATERMARK_INTERVAL = 60 * 60 * 1000; //水印刷新时间间隔一小时

    public static final int HAVE_KICK_VIEWER_PERMISSION = 1;

    //数字类型的true
    public static final String INTEGER_TYPE_TRUE = "1";
    //数字类型的false
    public static final String INTEGER_TYPE_FALSE = "0";

    public static final long WALI_LIVE_ASSISTANT_UUID = 100000L;  //小米直播助手的UID
    public static final long WALI_LIVE_CUSTOMER_SERVICE_UUID = 999L;  // 小米客服UID
    public static final long WALI_LIVE_SIGNED_UUID = 888L; // 小米直播签约号UID
    public static final String WALI_LIVE_CUSTOMER_SERVICE_NAME = "小米直播客服号";


    public static final int SCHEMA_FEEDS_TYPE_PIC = 1; //feeds的图片类型
    public static final int SCHEMA_FEEDS_TYPE_VEIDO = 2; //feeds的视频类型
    public static final int SCHEMA_FEEDS_TYPE_PLAYBACK = 3; //feeds的回放类型

    public static final int SCHEMA_FEEDS_EXT_TYPE = 1;// feeds schema的日志类型


}
