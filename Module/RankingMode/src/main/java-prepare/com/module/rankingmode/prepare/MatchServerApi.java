package com.module.rankingmode.prepare;


import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * 匹配过程中向服务器请求api
 */
public interface MatchServerApi {

    /**
     * 请求匹配
     * @param body 包含mode 游戏类型(必选)
     *             包含playbookItemID 演唱歌曲id(必选)
     *             包含platform 平台类型(必选)
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/query-match")
    Observable<ApiResult> startMatch(@Body RequestBody body);


    /**
     * 取消匹配
     * @param body  游戏类型 mode (必选)
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/cancel-match")
    Observable<ApiResult> cancleMatch(@Body RequestBody body);


    /**
     * 加入游戏
     * @param body  游戏标识 gameID (必选)
     * @return    当前已加入游戏  joinInfo（List）
     *            当前已加入游戏人数 hasJoinedUserCnt
     *            准备倒计时 readyClockResMs
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/join")
    Observable<ApiResult> joinGame(@Body RequestBody body);

    /**
     * 获取已加入游戏信息
     * @param gameID 游戏标识 gameID (必选)
     * @return    当前已加入游戏  joinInfo（List）
     *            当前已加入游戏人数 hasJoinedUserCnt
     *            准备倒计时 readyClockResMs
     */
    @GET("http://dev.game.inframe.mobi/v1/game/join")
    Observable<ApiResult> getCurrentGameData(@Query("gameID") int gameID);

    /**
     * 准备游戏
     * @param body  游戏标识 gameID (必选)
     * @return   当前已准备游戏 readyInfo (List）
     *           当前已准备游戏人数 HasReadyedUserCnt
     *           游戏是否开始 isGameStart
     *               轮次信息  roundInfoList (List 游戏开始才有值)
     *               游戏开始信息 gameStartInfo（游戏开始才有值）
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/ready")
    Observable<ApiResult> readyGame(@Body RequestBody body);

    /**
     * 获取准备游戏信息
     * @param gameID 游戏标识 gameID (必选)
     * @return   当前已准备游戏 readyInfo (List）
     *           当前已准备游戏人数 HasReadyedUserCnt
     *           游戏是否开始 isGameStart
     *               轮次信息  roundInfoList (List 游戏开始才有值)
     *               游戏开始信息 gameStartInfo（游戏开始才有值）
     */
    @GET("http://dev.game.inframe.mobi/v1/game/ready")
    Observable<ApiResult> getCurrentReadyData(@Query("gameID") int gameID);


    /**
     * 获取匹配中头像
     * @param mode 游戏类型
     * @return
     */
    @GET("http://dev.game.inframe.mobi/v1/game/on-match-player-avatar")
    Observable<ApiResult> getMatchingAvatar(@Query("mode") int mode);
}
