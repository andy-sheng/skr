package com.wali.live.watchsdk.scheme.specific;

/**
 * Created by lan on 2017/7/3.
 * SchemeConstants 和 SpecificConstants 添加的scheme要在ALL_CHANNEL_SCHEME_TYPE里添加。
 * host要在ALL_CHANNEL_HOST_TYPE裡添加。否則会被过滤掉导致ui上不被显示。
 */
public class SpecificConstants {
    /**
     * @notice Scheme : GameCenter
     * @example migamecenter://openlive?liveId=18109355&roomId=18109355_1497501557&isLive=1&gameId=3333
     */
    public static final String SCHEME_GAMECENTER = "migamecenter";

    public static final String HOST_OPEN_LIVE = "openlive";
    public static final String PARAM_LIVE_ID = "roomId";
    public static final String PARAM_PLAYER_ID = "liveId";
    public static final String PARAM_IS_LIVE = "isLive";
    public static final String PARAM_GAME_ID = "gameId";

    /**
     * @notice SCHEME : HTTP
     */
    public static final String SCHEME_HTTP = "http";
    public static final String SCHEME_HTTPS = "https";

    /**
     * @notice SCHEME : MI_VIDEO
     */
    public final static String SCHEME_MI_VIDEO = "mivideo";

    /**
     * @description {@link #PARAM_IS_LIVE}
     */
    public static final int PARAM_TYPE_LIVE = 1;
    public static final int PARAM_TYPE_PLAYBACK = 2;
}
