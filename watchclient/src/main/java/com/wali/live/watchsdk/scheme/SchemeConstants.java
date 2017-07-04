package com.wali.live.watchsdk.scheme;

/**
 * Created by lan on 16/10/26.
 *
 * @module scheme
 */
public class SchemeConstants {
    public static final String LOG_PREFIX = "SchemeLog#";

    /**
     * @notice Scheme : LiveSdk
     * @description 尽量和直播scheme保持一致
     */
    public static final String SCHEME_LIVESDK = "livesdk";

    /**
     * @notice Scheme : Walilive
     */
    public static final String SCHEME_WALILIVE = "walilive";

    // 跳转到直播间
    public static final String HOST_ROOM = "room";
    public static final String PATH_JOIN = "/join";
    public static final String PARAM_LIVE_ID = "liveid";
    public static final String PARAM_PLAYER_ID = "playerid";
    public static final String PARAM_VIDEO_URL = "videourl";
    public static final String PARAM_TYPE = "type";

    /**
     * 跳转到回放页
     * <p>
     * path is same as {@link #PATH_JOIN}
     * param(liveId) is same as{@link #PARAM_LIVE_ID}
     * param(playerId) is same as{@link #PARAM_PLAYER_ID}
     * param(videoUrl) is same as{@link #PARAM_VIDEO_URL}
     * param(type) is same as{@link #PARAM_TYPE}
     */
    public final static String HOST_PLAYBACK = "playback";

    /**
     * 跳转到频道
     */
    public static final String HOST_CHANNEL = "channel";
    public static final String PARAM_CHANNEL_ID = "channel_id";

    /**
     * 购物相关，目前挂件部分使用了参数，先留着
     */
    public static final String HOST_SHOP = "shopping";
    // 跳转到主播购物列表
    public static final String PATH_LIVELIST = "/livelist";
    // 商城打开京东或淘宝
    public final static String PATH_ENTER_SHOP = "/entershop";
    public final static String PARAM_SHOP_TYPE = "type";
    public final static String PARAM_SHOP_SHOW_TYPE = "showtype";
    public final static String PARAM_SHOP_GOOD_ID = "id";
    public final static String PARAM_SHOP_DETAIL_URL = "url";
    public final static String PARAM_SHOP_ZUID = "zuid";
    public final static String PARAM_SHOP_UUID = "uuid";
    public final static String PARAM_SHOP_ROOMID = "lid";
    public final static String PARAM_SHOP_PID = "pid";

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

    // 来源，区分从外部应用打开
    public final static String PARAM_OPEN_SOURCE = "source";

    // 跳转到网页
    public static final String HOST_OPEN_URL = "openurl";
    public static final String PATH_NEW_WINDOW = "/newwindow";
    // 获取从H5跳转获取界面
    public final static String PARAM_WEBVIEW_RUL = "url";
    public final static String PARAM_WEBVIEW_ISHALF = "ishalf";

    //跳转到咨询详情页,walilive专有.
    //eg:uri=walilive://feed/newsinfo?feedid=6212263_1492610945&ownerid=6212263&feed_type=3
    public static final String HOST_FEED = "feed";
    public static final String PATH_NEWS_INFO = "/newsinfo";
    public static final String PARAM_FEED_ID = "feedid";
    public static final String PARAM_OWENER_ID = "ownerid";
    public static final String PARAM_FEEDS_TYPE = "feed_type";
    public static final String PARAM_EXT_TYPE = "ext_type";
    public static final String PARAM_OPEN_FROM = "feeds_open_from";
}
