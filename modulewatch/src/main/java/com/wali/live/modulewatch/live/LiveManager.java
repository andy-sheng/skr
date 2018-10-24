package com.wali.live.modulewatch.live;

/**
 * Created by lan on 15-11-27.
 */
public class LiveManager {
    private static final String TAG = LiveManager.class.getSimpleName();

    // Live.proto里的直播类型定义
    public static final int TYPE_LIVE_PUBLIC = 0;
    public static final int TYPE_LIVE_PRIVATE = 1;
    public static final int TYPE_LIVE_TOKEN = 2;
    public static final int TYPE_LIVE_TICKET = 3;
    public static final int TYPE_LIVE_VR = 5;
    public static final int TYPE_LIVE_GAME = 6;
    public static final int TYPE_LIVE_RADIO = 8; //电台
    public static final int TYPE_LIVE_HUYA = 9;  //虎牙直播

}
