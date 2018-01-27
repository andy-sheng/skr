package com.wali.live.watchsdk.scheme;

import com.wali.live.watchsdk.scheme.specific.SpecificConstants;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by lan on 16/10/26.
 * <p>
 * SchemeConstants 和 SpecificConstants 添加的scheme要在ALL_CHANNEL_SCHEME_TYPE里添加。
 * host要在ALL_CHANNEL_HOST_TYPE裡添加。否則会被过滤掉导致ui上不被显示。
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

    /**
     * 安全验证参数
     *
     * @notice PARAM_CHANNEL 防止重名
     */
    public static final String PARAM_CHANNEL = "channel";
    public static final String PARAM_PACKAGE_NAME = "package_name";
    public static final String PARAM_CHANNEL_SECRET = "channel_secret";

    // 频道打点标签
    public final static String PARAM_RECOMMEND_TAG = "recommend";

    // 跳转到直播间
    public static final String HOST_ROOM = "room";
    public static final String PATH_JOIN = "/join";
    public static final String PARAM_LIVE_ID = "liveid";
    public static final String PARAM_PLAYER_ID = "playerid";
    public static final String PARAM_VIDEO_URL = "videourl";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_TYPE_LIVE_END = "after_live_end"; // 0: live 1:playback 2:person info fragment
    public final static String PARAM_IS_CONTEST = "is_contest";

    public static final int TYPE_LIVE = 0;
    public static final int TYPE_PLAYBACK = 1;
    public static final int TYPE_PERSON_INFO = 2;

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
     * 跳转到频道二级页面 <Walilive only>
     */
    public static final String HOST_RECOMMEND = "recommend";
    public static final String PATH_SUB_LIST = "/sublist";
    public static final String PATH_VIDEO_SUB_LIST = "/video_sublist";
    public static final String PARAM_LIST_ID = "id";
    public static final String PARAM_LIST_TITLE = "title";
    public static final String PARAM_LIST_CHANNEL_ID = "channelid";
    public static final String PARAM_LIST_KEY = "key";
    public static final String PARAM_LIST_KEY_ID = "key_id";
    public static final String PARAM_LIST_ANIMATION = "animation";
    public static final String PARAM_LIST_SOURCE = "source";
    public static final String PARAM_SELECT = "select";
    public static final String PARAM_RET_PAGE = "ret_page";

    /**
     * 购物相关，目前挂件部分使用了参数，先留着
     */
    public static final String HOST_SHOP = "shopping";
    /**
     * 跳转到主播购物列表
     */
    public static final String PATH_LIVELIST = "/livelist";
    /**
     * 商城打开京东或淘宝
     */
    public final static String PATH_ENTER_SHOP = "/entershop";
    public final static String PARAM_SHOP_TYPE = "type";
    public final static String PARAM_SHOP_SHOW_TYPE = "showtype";
    public final static String PARAM_SHOP_GOOD_ID = "id";
    public final static String PARAM_SHOP_DETAIL_URL = "url";
    public final static String PARAM_SHOP_ZUID = "zuid";
    public final static String PARAM_SHOP_UUID = "uuid";
    public final static String PARAM_SHOP_ROOMID = "lid";
    public final static String PARAM_SHOP_PID = "pid";

    /**
     * 跳转到充值页--h5活动需要
     */
    public final static String HOST_RECHARGE = "recharge";

    /**
     * 跳转到余额页
     */
    public final static String HOST_BALANCE = "balance";

    /**
     * 跳转到提现页
     */
    public final static String HOST_WITHDRAW = "withdraw";

    /**
     * 跳转到兑现钻石页
     */
    public final static String HOST_EXCHANGE = "exchange";

    /**
     * 跳转到收益页
     */
    public final static String HOST_PROFIT = "profit";

    /**
     * 来源，区分从外部应用打开
     */
    public final static String PARAM_OPEN_SOURCE = "source";

    /**
     * 跳转到网页
     */
    public static final String HOST_OPEN_URL = "openurl";
    public static final String PATH_NEW_WINDOW = "/newwindow";
    /**
     * 获取从H5跳转获取界面
     */
    public final static String PARAM_WEBVIEW_RUL = "url";
    public final static String PARAM_WEBVIEW_ISHALF = "ishalf";

    /**
     * 跳转到咨询详情页 <Walilive only>
     * eg:uri=walilive://feed/newsinfo?feedid=6212263_1492610945&ownerid=6212263&feed_type=3
     */
    public static final String HOST_FEED = "feed";
    public static final String PATH_NEWS_INFO = "/newsinfo";
    public static final String PARAM_FEED_ID = "feedid";
    public static final String PARAM_OWENER_ID = "ownerid";
    public static final String PARAM_FEEDS_TYPE = "feed_type";
    public static final String PARAM_EXT_TYPE = "ext_type";
    public static final String PARAM_OPEN_FROM = "feeds_open_from";

    public static final int EXT_TYPE_LONG_TEXT = 3;

    public static final String HOST_LIVE_MI = "live.mi.com";
    public static final String HOST_ACTIVITY_ZB_MI = "activity.zb.mi.com";

    /**
     * 广告的数据
     */
    public static final String EXTRA_BANNER_INFO = "extra_banner_info";

    /**
     * 登录跳转
     *
     * @deprecated
     */
    public static final String HOST_LOGIN = "login";

    /**
     * h5页面上 未登录的scheme
     */
    public static final String HOST_UNLOGIN_H5 = "unloginHtml5";

    /**
     * "http://zhibo.mi.com & https://zhibo.mi.com"
     */
    public static final String HOST_ZHIBO_COM = "zhibo.mi.com";
    public static final String PARAM_ACTION = "action";

    //冲顶大会
    public static final String HOST_CONTEST = "contest";
    public static final String PATH_PREPARE = "/prepare";
    public static final String PARAM_ZUID = "zuid";

    /**
     *SchemeConstants 和 SpecificConstants 添加的scheme和host都要在這裡添加一下。否則会被过滤掉导致ui上不被显示。
     */

    /**
     * SCHEME常量集合，用于ui显示拦截
     */
    public static final HashSet<String> ALL_CHANNEL_SCHEME_TYPE = new HashSet<>(Arrays.asList(
            SchemeConstants.SCHEME_LIVESDK,
            SchemeConstants.SCHEME_WALILIVE,
            SpecificConstants.SCHEME_GAMECENTER,
            SpecificConstants.SCHEME_HTTP,
            SpecificConstants.SCHEME_HTTPS
    ));
    /**
     * HOST常量集合，用于ui显示拦截
     */
    public static final HashSet<String> ALL_CHANNEL_HOST_TYPE = new HashSet<>(Arrays.asList(
            SchemeConstants.HOST_ROOM,
            SchemeConstants.HOST_PLAYBACK,
            SchemeConstants.HOST_CHANNEL,
            SchemeConstants.HOST_RECOMMEND,
            SchemeConstants.HOST_OPEN_URL,
            SchemeConstants.HOST_FEED,
            SchemeConstants.HOST_CONTEST
    ));
}
