package com.mi.liveassistant.avatar;

/**
 * Created by lan on 17/5/3.
 */
public class AvatarConstants {
    /**
     * 头像链接拼接方式
     * 第一个%d：uid；
     * 第一个%s：用来裁切缩略图
     * 第二个%d：timeStamp，没有服务器时间戳，采用本地时间戳
     */
    public static String AVATAR_URL = "http://dl.zb.mi.com/%d%s?timestamp=%d";
}
