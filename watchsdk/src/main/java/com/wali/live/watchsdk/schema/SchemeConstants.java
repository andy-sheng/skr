package com.wali.live.watchsdk.schema;

/**
 * Created by lan on 16/10/26.
 *
 * @module scheme
 */
public class SchemeConstants {
    public static final String LOG_PREFIX = "SchemeLog#";

    /**
     * @notice SCHEME : WALILIVE
     */
    public static final String SCHEME_WALILIVESDK = "walilivesdk";

    /**
     * @notice SCHEME : WALILIVE
     */
    public static final String SCHEME_TAMLL = "tmall";

    // 跳转到直播间
    public final static String HOST_ROOM = "room";
    public final static String PATH_JOIN = "/join";
    public final static String PARAM_ROOMID = "liveid";                         // 直播间id
    public final static String PARAM_PLAYER_ID = "playerid";
    public final static String PARAM_NEED_QUERY_ROOM_INFO = "query_room_info";  // 是否需要查询房间信息
    public final static String PARAM_VIDEOURL = "videourl";                     // 视频播放地址
    public final static String PARAM_RECOMMEND_TAG = "recommend";               // 频道打点标签
    public static final String PARAM_TYPE = "type";
    public final static String PARAM_AFTER_LIVE_END = "after_live_end";         // 直播结束去向
    public final static String PARAM_IS_LIVE = "islive";

    /**
     * 跳转到回放页
     * <p>
     * path is same as {@link #PATH_JOIN}
     */
    public final static String HOST_PLAYBACK = "playback";
    public final static String PARAM_PLAYBACK_ROOM_ID = "liveid";               // 回放直播间id
    public final static String PARAM_PLAYBACK_VIDEO_URL = "videourl";           // 回放视频播放地址
    public final static String PARAM_ONLY_SHOW_PLAYER = "onlyShowPlayer";       // 回放只显示视频其他view隐藏
    public final static String PARAM_ENTER_FEEDS_FIRST = "feeds";

    /**
     * 跳转到一直播
     * <p>
     * path is same as {@link #PATH_JOIN}
     */
    public final static String HOST_CHATROOM = "chatroom";                      // 进入一直播

    /**
     * 跳转到电视台
     * <p>
     * path is same as {@link #PATH_JOIN}
     * param(playerid) is same as {@link #PARAM_PLAYER_ID}
     * param(liveid) is same as {@link #PARAM_ROOMID}
     */
    public static final String HOST_MIVIDEO = "mivideo";
    public static final String PARAM_VIDEO_ID = "videoid";


    public static final String HOST_SHOP = "shopping";
    // 跳转到主播购物列表
    public static final String PATH_LIVELIST = "/livelist";
    // 商城打开京东或淘宝
    public final static String PATH_ENTER_SHOP = "/entershop";
    public final static String PARAMETER_SHOP_TYPE = "type";
    public final static String PARAMETER_SHOP_SHOW_TYPE = "showtype";
    public final static String PARAMETER_SHOP_GOOD_ID = "id";
    public final static String PARAMETER_SHOP_DETAIL_URL = "url";
    public final static String PARAMETER_SHOP_ZUID = "zuid";
    public final static String PARAMETER_SHOP_UUID = "uuid";
    public final static String PARAMETER_SHOP_ROOMID = "lid";
    public final static String PARAMETER_SHOP_PID = "pid";

    // 跳转到名片
    public static final String HOST_USER_INFO = "user";
    public static final String PATH_USER_INFO = "/info";
    public static final String PARAM_USER_UUID = "uuid";
    public static final String PARAM_CERTIFICATION_AGENCY = "cert";

    // 跳转到首页fragment
    public static final String HOST_MAIN_PAGE = "mainpage";
    public static final String PATH_FRAGMENT_PAGE = "/fragement_page";
    public static final String PARAM_FRAGMENT_PAGE_ID = "id";

    // 跳转到频道
    public static final String HOST_CHANNEL = "channel";
    public static final String PARAM_CHANNEL_ID = "channel_id";

    //  跳转到频道二级页面
    public static final String HOST_RECOMMEND = "recommend";
    public static final String PATH_SUB_LIST = "/sublist";
    public static final String PARAM_LIST_ID = "id";
    public static final String PARAM_LIST_TITLE = "title";
    public static final String PARAM_LIST_CHANNEL_ID = "channelid";
    public static final String PARAM_LIST_KEY = "key";
    public static final String PARAM_LIST_KEY_ID = "key_id";
    public static final String PARAM_LIST_ANIMATION = "animation";
    public static final String PARAM_LIST_SOURCE = "source";

    // 跳转到资讯详情
    public static final String HOST_FEED = "feed";
    public static final String PATH_NEWS_INFO = "/newsinfo";
    public static final String PARAM_FEED_ID = "feedid";
    public static final String PARAM_OWENER_ID = "ownerid";
    public static final String PARAM_FEEDS_TYPE = "feed_type";
    public static final String PARAM_EXT_TYPE = "ext_type";
    public static final String PARAM_OPEN_FROM = "feeds_open_from";
    public static final String PARAM_IS_GRAME_CHANNEL = "is_game_channel";

    //当web前端添加关注的人后，可通过调用客户端操作，刷新app的关注的人列表
    //walilive://update/follow?uuid=1,2,3    （uuid代表用户ID列表，以“,”来分隔）
    public static final String HOST_UPDATE = "update";
    public static final String PATH_FOLLOW = "/follow";

    // 跳转到米家
    public static final String HOST_MI_FAMILY = "mijia";
    public static final String PATH_LIST = "/list";

    // 跳转到话题页
    public static final String HOST_TOPIC = "topic";
    public static final String PARAM_TOPIC_ID = "tid";

    // 跳转到网页
    public static final String HOST_OPEN_URL = "openurl";
    public static final String PATH_WINDOW_TYPE = "/newwindow";
    // 获取从H5跳转获取界面
    public final static String PARAM_URI_WEBVIEW_RUL = "url";
    // 是否分享
    public final static String PARAM_URI_WEBVIEW_SHARABLE = "sharable";
    public final static String PARAM_URI_WEBVIEW_ISHALF = "ishalf";

    // 跳转到充值页
    public final static String HOST_RECHARGE = "recharge";

    // 跳转到余额页
    public final static String HOST_BALANCE = "balance";

    // 跳转到提现页
    public final static String HOST_WITHDRAW = "withdraw";

    // 跳转到兑现钻石页
    public final static String HOST_EXCHANGE = "exchange";

    // 跳转到收益页
    public final static String HOST_PROFIT = "profit";

    // 电竞
    public final static String HOST_SUBJECT = "tplsubject";
    public final static String HOST_CATEGORY = "tplcategory";

    // 跳转到主播排行榜
    public static final String HOST_ANCHOR_RANK = "ranklist";
    public static final String PATH_RANK = "/global";
    public static final String PARAM_RANK_LIST_ID = "listid";
    public static final String PARAM_SUB_RANK_LIST_ID = "sublistid";

    //跳转到预告
    public static final String HOST_NOTICE = "notice";

    // 来源，区分从外部应用打开
    public final static String PARAM_OPEN_SOURCE = "source";

    // 从未登陆的用户唤醒
    public static final String PARAM_WAKEUP = "wakeup";
    public static final int PARAM_WAKEUP_VALUE = 1;

    /**
     * @notice SCHEME : GAME
     */
    public final static String SCHEME_GAME = "migamecenter";

    /**
     * @notice SCHEME : HTTP
     */
    public final static String SCHEME_HTTP = "http";

    /**
     * @notice SCHEME : MI_VIDEO
     */
    public final static String SCHEME_MI_VIDEO = "mivideo";

    /**
     * @notice SCHEME : OPEN_LIVE
     */
    public final static String SCHEME_OPEN_LIVE = "liveopen";
    public final static String HOST_OPEN_LIVE_START_STREAM = "startstream";
    public final static String HOST_OPEN_LIVE_END_STREAM = "endstream";
    public final static String HOST_LIVE_GET_USER_INFO = "getInfo";

    // 点击h5页面跳转回app的行为 "android.intent.action.VIEW"
    public static final String ACTION_FROM_HPAGE = "from_h5_page";

    // 跳转到首页频道的行为
    public static final String ACTION_JUMP_TO_CHANNEL = "jump_to_channel";
    // 跳转到首页频道的参数
    public static final String EXTRA_CHANNEL_URI = "channel_uri";

    // 广告的数据
    public static final String EXTRA_BANNER_INFO = "extra_banner_info";

    //backToOrigin 有此字段 从别的app打开直播，返回别的别app
    public static final String PARAM_BACK_TO_ORIGIN = "backToOrigin";
}
