package com.common.utils;

import java.lang.reflect.Field;

public class ChannelUtils {
    private String channelName = "DEFAULT";

    ChannelUtils() {
        try {
            Class ct = Class.forName(U.getAppInfoUtils().getPackageName() + ".BuildConfig");
            Field field = ct.getField("CHANNEL_NAME");
            channelName = (String) field.get(null);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public String getChannelNameFromBuildConfig() {
        return channelName;
    }

    /**
     * 这里以后会修改，返回真正渠道号。
     * 原理： DEFAULT 为自升级渠道，不能覆盖原本的渠道号
     * @return
     */
    public String getChannel() {
        return channelName;
    }

}
