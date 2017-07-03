package com.wali.live.watchsdk.scheme.gamecenter;

/**
 * Created by lan on 2017/7/3.
 */
public class GamecenterConstants {
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
     * @description {@link #PARAM_IS_LIVE}
     */
    public static final int PARAM_TYPE_LIVE = 1;
    public static final int PARAM_TYPE_PLAYBACK = 2;
}
