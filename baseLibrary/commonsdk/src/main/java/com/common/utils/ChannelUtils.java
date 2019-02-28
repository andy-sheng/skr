package com.common.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;


import com.common.log.MyLog;

import java.lang.reflect.Field;

public class ChannelUtils {
    public final static String TAG = "ChannelUtils";
    private static final String PREF_KEY_CHANNEL = "key_channel";
    private static final String PREF_KEY_SUB_CHANNEL = "key_sub_channel";
    private static final String PREF_KEY_DEBUG_CHANNEL = "key_debug_channel";
    private String channelNameFromBuildConfig = "DEFAULT";
    private String channelNameFromPref;
    private String mSubChannel;

    ChannelUtils() {
        try {
            Class ct = Class.forName(U.getAppInfoUtils().getPackageName() + ".BuildConfig");
            Field field = ct.getField("CHANNEL_NAME");
            channelNameFromBuildConfig = (String) field.get(null);
            channelNameFromBuildConfig = U.getPreferenceUtils().getSettingString(PREF_KEY_DEBUG_CHANNEL, channelNameFromBuildConfig);
        } catch (Exception e) {
            MyLog.e(e);
//            PgyCrashManager.reportCaughtException(e);
        }
    }

    public String getChannelNameFromBuildConfig() {
        return channelNameFromBuildConfig;
    }

    public void setChannelNameFromBuildConfig(String channel) {
        channelNameFromBuildConfig = channel;
        U.getPreferenceUtils().setSettingString(PREF_KEY_DEBUG_CHANNEL, channel);
    }

    /**
     * 这里以后会修改，返回真正渠道号。
     * 原理： DEFAULT 为自升级渠道，不能覆盖原本的渠道号
     *
     * @return
     */
    public String getChannel() {
        if ("DEV".equals(channelNameFromBuildConfig)) {
            return channelNameFromBuildConfig;
        }
        if ("TEST".equals(channelNameFromBuildConfig)) {
            return channelNameFromBuildConfig;
        }
        if ("SANDBOX".equals(channelNameFromBuildConfig)) {
            return channelNameFromBuildConfig;
        }
        if (TextUtils.isEmpty(channelNameFromPref)) {
            //读一下 pref
            channelNameFromPref = U.getPreferenceUtils().getSettingString(PREF_KEY_CHANNEL, "DEFAULT");
        }
        if (!channelNameFromBuildConfig.equals(channelNameFromPref)) {
            if (channelNameFromBuildConfig.equals("DEFAULT")) {
                // 如果是自升级渠道，保持原来的channel
                if (!TextUtils.isEmpty(channelNameFromPref)) {
                    return channelNameFromPref;
                } else {
                    // 按理不会有这种情况
                    return "DEFAULT";
                }
            } else {
                // 如果不是自升级渠道,保存起来
                channelNameFromPref = channelNameFromBuildConfig;
                U.getPreferenceUtils().setSettingString(PREF_KEY_CHANNEL, channelNameFromPref);
            }
        }
        return channelNameFromPref;
    }

    public boolean isStaging() {
        return isDevChannel() || isTestChannel() || isSandboxChannel();
    }

    public boolean isDevChannel() {
        return getChannel().equals("DEV");
    }

    public boolean isTestChannel() {
        return getChannel().equals("TEST");
    }

    public boolean isSandboxChannel() {
        return getChannel().equals("SANDBOX");
    }


    public void setSubChannel(String subChannel) {
        mSubChannel = subChannel;
        if (!TextUtils.isEmpty("mSubChannel")) {
            U.getPreferenceUtils().setSettingString(PREF_KEY_SUB_CHANNEL, mSubChannel);
        } else {
            U.getPreferenceUtils().setSettingString(PREF_KEY_SUB_CHANNEL, "");
        }
    }

    public String getSubChannel() {
        return mSubChannel;
    }
}
