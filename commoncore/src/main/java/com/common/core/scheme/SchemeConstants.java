package com.common.core.scheme;

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

    //inframeskr://game/rank/chooseSong?gameMode=1
    //inframeskr://web/fullScreen?url=https://www.baidu.com

    /**
     * @notice Scheme : inframeskr
     */
    public static final String SCHEME_INFRAMESKER = "inframeskr";

    //host
    public static final String HOST_ROOM = "room";
    public static final String HOST_PERSON = "person";
    public static final String HOST_WEB = "web";
    public static final String HOST_SHARE = "share";
    public static final String HOST_CHANNEL = "channel";
    public static final String HOST_WALLET = "wallet";
    public static final String HOST_RELATION = "relation";

    //path
    public static final String PATH_JOIN = "/join";
    public static final String PATH_FULL_SCREEN = "/fullScreen";
    public static final String PATH_RANK_CHOOSE_SONG = "/rank/chooseSong";
    public static final String PATH_GRAB_MATCH = "/grabmatch";
    public static final String PATH_WITH_DRAW = "/withdraw";

    //param
    public static final String PARAM_LIST_ID = "id";
    public static final String PARAM_URL = "url";
    public static final String PARAM_GAME_MODE = "gameMode";
    public static final String PARAM_GRAB_MODE = "mode";
    public static final String PARAM_SHOW_SHARE = "showShare";
    public static final String PARAM_TAG_ID = "tagId";
    public static final String PARAM_FROM = "from";


    /**
     *SchemeConstants 和 SpecificConstants 添加的scheme和host都要在這裡添加一下。否則会被过滤掉导致ui上不被显示。
     */

    /**
     * SCHEME常量集合，用于ui显示拦截
     */
//    public static final HashSet<String> ALL_CHANNEL_SCHEME_TYPE = new HashSet<>(Arrays.asList(
//            SchemeConstants.SCHEME_LIVESDK,
//            SchemeConstants.SCHEME_INFRAMESKER,
//            SpecificConstants.SCHEME_GAMECENTER,
//            SpecificConstants.SCHEME_HTTP,
//            SpecificConstants.SCHEME_HTTPS
//    ));
//    /**
//     * HOST常量集合，用于ui显示拦截
//     */
//    public static final HashSet<String> ALL_CHANNEL_HOST_TYPE = new HashSet<>(Arrays.asList(
//            SchemeConstants.HOST_ROOM,
//            SchemeConstants.HOST_PLAYBACK,
//            SchemeConstants.HOST_CHANNEL,
//            SchemeConstants.HOST_RECOMMEND,
//            SchemeConstants.HOST_OPEN_URL,
//            SchemeConstants.HOST_FEED,
//            SchemeConstants.HOST_CONTEST
//    ));
}
