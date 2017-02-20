package com.wali.live.common.action;

/**
 * Created by lan on 15-12-10.
 * 1000-1999
 */
public class VideoAction {
    /*use for VideoSelectFragment click*/
    public static final int ACTION_CANCEL = 1000;                // 取消选视频,回退页面
    public static final int ACTION_OK = 1001;                    // 点击确定,视频选择成功,回退到上个页面

    /*share click*/
    public static final int ACTION_SHARE_TO_WEIXIN = 1002;                // 分享到微信
    public static final int ACTION_SHARE_TO_MOMENT = 1003;                // 分享到微信朋友圈
    public static final int ACTION_SHARE_TO_QQ = 1004;                    //分享到QQ
    public static final int ACTION_SHARE_TO_QZONE = 1005;                    //分享到Qzone
    public static final int ACTION_SHARE_TO_WEIBO = 1006;                // 分享到微博
    public static final int ACTION_SHARE_TO_SNS = 1007;               //分享到小米

    public static final int ACTION_COPY_LINK = 1008;
    public static final int ACTION_OPEN_SYS_BROWER = 1009;
    public static final int ACTION_SHARE_TO_FACEBOOK = 1403;                // 分享到微博
    public static final int ACTION_SHARE_TO_TWITTER = 1404;               //分享到小米

    /*use for PostLocalFragment & PostLiveFragment click*/
    public static final int ACTION_POST_CANCEL = 1100;           // 取消选视频,回退页面
    public static final int ACTION_POST_OK = 1101;               // 点击确定,视频选择成功,回退到上个页面
    public static final int ACTION_POST_SELECT = 1102;           // 选视频,进入视频选择页面 & 选图片,进入图片选择页面
    public static final int ACTION_POST_CLOSE = 1103;            // 关闭页面
    public static final int ACTION_POST_TIME = 1104;             // 进入时间选择页面

    /*use for PrepareLiveFragment click*/
    public static final int ACTION_PREPARE_CANCEL = 1200;       // 取消预览
    public static final int ACTION_PREPARE_OK = 1201;           // 确定预览
    public static final int ACTION_PREPARE_CLOSE = 1202;        // 关闭页面
    public static final int ACTION_PREPARE_PRIVATE = 1203;      // 进入私密直播
    public static final int ACTION_PREPARE_PLAYBACK = 1204;     // 是否保存私密直播
    public static final int ACTION_PREPARE_ADD_TOPIC = 1205;    // 添加话题
    public static final int ACTION_PREPARE_LOCATE = 1206;       // 重新定位

    /*use for LiveActivity & WatchActivity click*/
    public static final int ACTION_VIDEO_CLOSE = 1300;           // 关闭页面
    public static final int ACTION_VIDEO_AVATAR_CLICK = 1301;    // 头像点击
    public static final int ACTION_VIDEO_CONTROL_HIDE = 1302;    // 控制区域隐藏
    public static final int ACTION_VIDEO_LOCK = 1303;            // 锁住模式
    public static final int ACTION_VIDEO_PROGRAM = 1304;         // 参看节目单
    public static final int ACTION_VIDEO_FORBID_BULLET = 1305;   // 静止弹幕
    public static final int ACTION_VIDEO_SEND_BULLET = 1306;     // 发送弹幕
    public static final int ACTION_VIDEO_EVENT_REPLAY = 1307;    // 视频播放完重播
    public static final int ACTION_VIDEO_EVENT_PLAY = 1308;      // 视频等待播放
    public static final int ACTION_VIDEO_EVENT_ERROR = 1309;     // 错误重置播放
    public static final int ACTION_VIDEO_LIKE = 1310;            // 视频点赞逻辑
    public static final int ACTION_VIDEO_PLAY = 1311;            // 播放暂停逻辑
    public static final int ACTION_VIDEO_RESOLUTION = 1312;      // 选择视频分辨率
    public static final int ACTION_VIDEO_STOP = 1313;            // 停止直播
    public static final int ACTION_VIDEO_CAMERA_SWITCH = 1317;   // 切换前后摄像头
    public static final int ACTION_VIDEO_WORD_OVERTURN = 1318;   // 是否文字翻转
    public static final int ACTION_VIDEO_TORCH = 1319;           // 是否打开闪光灯
    public static final int ACTION_VIDEO_BEAUTY_SWITCH = 1320;   // 是否美颜
    public static final int ACTION_VIDEO_MUTE_SWITCH = 1321;     // 是否静音
    public static final int ACTION_VIDEO_TICKET = 1322;          // 点击星票
    public static final int ACTION_TITLE_FOLLOW = 1323;          // 头像赶关注按钮
    public static final int ACTION_VIDEO_PK = 1326;              // 连麦操作

    /*use for EndLiveFragment click*/
    public static final int ACTION_END_BACK = 1400;              // 主播停止
    public static final int ACTION_END_HISTORY_DELETE = 1401;    // 删除回放
    public static final int ACTION_END_CONCERN = 1402;           // 关注主播
}
