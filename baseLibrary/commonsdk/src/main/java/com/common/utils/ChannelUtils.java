package com.common.utils;

import android.text.TextUtils;

import com.pgyersdk.crash.PgyCrashManager;

import java.lang.reflect.Field;

public class ChannelUtils {
    public final static String TAG = "ChannelUtils";
    private static final String PREF_KEY_CHANNEL = "key_channel";
    private String channelNameFromBuildConfig = "DEFAULT";
    private String channelNameFromPref;

    ChannelUtils() {
        try {
            Class ct = Class.forName(U.getAppInfoUtils().getPackageName() + ".BuildConfig");
            Field field = ct.getField("CHANNEL_NAME");
            channelNameFromBuildConfig = (String) field.get(null);
        } catch (Exception e) {
            PgyCrashManager.reportCaughtException(e);
        }
    }

    public String getChannelNameFromBuildConfig() {
        return channelNameFromBuildConfig;
    }

    /**
     * 这里以后会修改，返回真正渠道号。
     * 原理： DEFAULT 为自升级渠道，不能覆盖原本的渠道号
     *
     * @return
     */
    public String getChannel() {
        if ("TEST".equals(channelNameFromBuildConfig)) {
            return "TEST";
        }
        if (TextUtils.isEmpty(channelNameFromPref)) {
            channelNameFromPref = U.getPreferenceUtils().getSettingString(PREF_KEY_CHANNEL, "DEFAULT");
        }
        if (!channelNameFromBuildConfig.equals(channelNameFromPref)) {
            if (channelNameFromBuildConfig.equals("DEFAULT")) {
                // 如果是自升级渠道
                return channelNameFromPref;
            } else {
                // 如果不是自升级渠道
                channelNameFromPref = channelNameFromBuildConfig;
                U.getPreferenceUtils().setSettingString(PREF_KEY_CHANNEL, channelNameFromPref);
            }
        }
        return channelNameFromPref;
    }

    public boolean isTestChannel() {
        return getChannel().equals(channelNameFromBuildConfig);
    }

}
