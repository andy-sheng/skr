package com.mi.liveassistant.config;

import com.mi.liveassistant.channel.ReleaseChannelUtils;

/**
 * Created by lan on 17/4/25.
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

    public static final int MILINK_APP_ID = 10013;

    public static final String APP_NAME = "WALI_LIVE_SDK";
}
