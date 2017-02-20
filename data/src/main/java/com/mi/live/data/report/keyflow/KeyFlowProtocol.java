package com.mi.live.data.report.keyflow;

/**
 * Created by yangli on 16-7-19.
 */
public class KeyFlowProtocol {

    public static final String KEY_PUSH = "push";

    public static final String KEY_PULL = "pull";

    public static final String KEY_PLAYBACK = "playback";

    /** 键值定义：本地存储以及上报服务器共用字段 */
    public static final String KEY_ID = "id"; // 用户ID

    public static final String KEY_ANCHOR_ID = "anchor_id"; // 主播ID

    public static final String KEY_URL = "url"; // 推拉流URL

    public static final String KEY_BEGIN_TIME = "begin_time"; // 用户点击发起(或观看)直播的时间点

    public static final String KEY_CREATE_ROOM = "create_room"; // 创建房间的时间点

    public static final String KEY_ENGINE_INIT = "engine_init"; // 引擎初始化的时间点

    public static final String KEY_DNS_PARSE = "dns_parse"; // DNS解析获取一个IP的过程

    public static final String KEY_END_TIME = "end_time"; // 结束直播(或结束观看)的时间点

    public static final String KEY_INFO = "info"; // IP、卡顿等信息

    public static final String KEY_IP = "ip"; // 推拉流IP

    public static final String KEY_FLAG = "flag"; // IP获取方式

    public static final String KEY_STUTTER = "stutter"; // 卡顿

    public static final String KEY_RATE = "rate"; // 推拉流码率

    public static final String KEY_STATUS = "status"; // 使用某个IP推拉流成功或者失败的状态

    public static final String KEY_ERRNO = "errno"; // 错误码

    public static final String KEY_ERROR = "error"; // 错误描述

    /** 键值定义：仅供本地记录事件序列使用 */
    public static final String KEY_IP_BEGIN = "ip_begin_time"; // 开始使用某个ip的时间点

    public static final String KEY_IP_END = "ip_end_time"; // 结束使用某个ip的时间点

    public static final String KEY_STUTTER_BEGIN = "stutter_begin"; // 卡顿开始

    public static final String KEY_STUTTER_END = "stutter_end"; // 卡顿结束

    /** 错误码定义 */
    public static final int ERR_CODE_UNKNOWN = -24000;

    public static final int ERR_CODE_INIT_ENGINE = -24100;

    public static final int ERR_CODE_CREATE_ROOM = -24200;

    public static final int ERR_CODE_DNS_PARSE = -24300;

    public static final int ERR_CODE_DNS_PARSE_SOCKET = -24400;

    public static final int ERR_CODE_DNS_PARSE_RTMP = -24401;

    public static final int ERR_CODE_DNS_PARSE_BADNAME = -24402;

    public static final int ERR_CODE_PUSH_STREAM = -24500;

    public static final int ERR_CODE_ILLEGAL_FLOW = -25100;
}
