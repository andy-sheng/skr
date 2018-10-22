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

    public String getChannel() {
        return "";
    }

}
