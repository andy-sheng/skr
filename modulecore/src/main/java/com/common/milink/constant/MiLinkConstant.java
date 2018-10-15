package com.common.milink.constant;

/**
 * Created by lan on 15-11-11.
 */
public class MiLinkConstant {
    public static final int TIME_OUT = 10 * 1000;

    public static final int UPLOAD_CONNECTION_TIMEOUT = 10 * 1000;

    public static final int ERROR_CODE_SUCCESS = 0;
    public static final int ERROR_CODE_MSG_TOO_LARGE = 8502;
    public static final int NOT_FOLLOW_EACH_OTHER = 8503;
    public static final int ERROR_CODE_BAN_SPEAKER = 8504;
    public static final int ILLEGAL_MSG = 8505;
    public static final int DUPLICATED_MSG = 8506;
    public static final int INVALID_PARAM = 8507;
    public static final int BLOCKD = 8508;
    public static final int DUPLICATED_HI_MSG = 8510;   // 连续发送打招呼消息

    public static final String AUTH_TOKEN_HOST = "http://voip.game.xiaomi.com/mfas/auth"; // 获取上传图片token的域名
    public static final String AUTH_TOKEN_IP = "http://120.134.33.113/mfas/auth"; // 获取上传图片token的保底ip


    public static final int UNKNOWN_APP_TYPE = -1; // 未定义类型
    public static final int MY_APP_TYPE = 0; // 小米直播app
    public static final int THIRD_APP_TYPE = 1;// 无人机
    public static final int DIRECTOR_APP_TYPE = 2;// 导播台
    public static final int REPLAY_KIT = 3;// iPhone的replay kit
    public static final int PUSH_SDK = 6;// 授权sdk
}
