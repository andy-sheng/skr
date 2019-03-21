package com.module.playways.rank.room;

import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface RoomServerApi {
    /**
     * "{\n\t\"gameID\" : 20000220,\n\t\"content\" : \"hello xxx\"\n}")
     *
     * @param body
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/chat")
    Observable<ApiResult> sendMsg(@Body RequestBody body);

    /**
     * {
     *   "gameID": 0,
     *   "roundSeq": 0,
     *   "sign": "string",
     *   "sysScore": 0,
     *   "timeMs": 0
     * }
     * @return 当前轮次结束时间戳roundOverTimeMs
     * 当前轮次信息currentRound
     * 下个轮次信息nextRound
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/round/score")
    Observable<ApiResult> sendRoundScore(@Body RequestBody body);

    /**
     * 上报结束一轮游戏
     *
     * @param body 游戏标识 gameID (必选)
     *             机器评分 sysScore (必选)
     *             时间戳 timeMs (必选)
     *             签名  sign (必选)  md5(skrer|gameID|score|timeMs)
     * @return 当前轮次结束时间戳roundOverTimeMs
     * 当前轮次信息currentRound
     * 下个轮次信息nextRound
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/round/over")
    Observable<ApiResult> sendRoundOver(@Body RequestBody body);

    /**
     * 当前轮次时上报心跳
     *
     * @param body 游戏标识 gameID (必选)
     *             身份标识 userID (必选）
     * @return
     */
    @Headers(ApiManager.NO_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/game/hb")
    Observable<ApiResult> sendHeartBeat(@Body RequestBody body);

    /**
     * 同步游戏详情状态
     *
     * @param gameID
     * @return 同步请求时间戳  syncStatusTimeMs
     * 游戏结束时间戳  gameOverTimeMs
     * 游戏在线  onlineInfo（List）
     * 当前轮次信息 currentRound
     * 下个轮次信息 nextRound
     */
    @GET("http://dev.game.inframe.mobi/v1/game/status")
    Observable<ApiResult> syncGameStatus(@Query("gameID") int gameID);

    /**
     * 退出游戏
     *
     * @param body 游戏标识 gameID (必选)
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/exit")
    Observable<ApiResult> exitGame(@Body RequestBody body);


    /**
     * 切换前后台
     *
     * @param body 游戏标识 gameID (必选)
     *             切到后台 out (必选)
     *             切回来   in  (必选)
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/swap")
    Observable<ApiResult> swap(@Body RequestBody body);


    /**
     * 进行投票
     *
     * @param body 游戏标识 gameID (必选)
     *             被投票人 pickUserID (必选)
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/vote")
    Observable<ApiResult> vote(@Body RequestBody body);

    /**
     * 获取投票结果
     *
     * @param gameID
     * @return 投票打分信息 List<VoteInfoModel>
     * 分值信息    List<UserScoreModel>
     */
    @GET("http://dev.game.inframe.mobi/v1/game/vote")
    Observable<ApiResult> getVoteResult(@Query("gameID") int gameID);


    /**
     * 上传机器人ai需要的相关音频资源
     * <p>
     * {
     * "gameID" : 20001505,
     * "itemID" : 11,
     * "sysScore" : 99,
     * "audioURL" : "http://xxxxxx.com",
     * "midiURL" : "http://yyyy.com",
     * "timeMs" : 1545312998095,
     * "sign" : "8b700ccc3c236d46b97d364c60febda9"
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/resource")
    Observable<ApiResult> putGameResource(@Body RequestBody body);

    /**
     * 参数
     * { "gameID" : 1,
     * "roundSeq" : 1
     * }
     * <p>
     * res
     * <p>
     * 灭灯
     *
     * @param body
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/pk-show-mlight")
    Observable<ApiResult> pklightOff(@Body RequestBody body);

    /**
     * 参数
     * { "gameID" : 1,
     * "roundSeq" : 1
     * }
     * <p>
     * res
     * <p>
     * 爆灯
     *
     * @param body
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/game/pk-show-blight")
    Observable<ApiResult> pkburst(@Body RequestBody body);

    /*
        提交单句反馈请求{
            gameID	integer($int64)
                    title: [必传]游戏ID
            itemID	integer($int64)
                    title: [必传]演唱曲目
            mainLevel	integer($int64)
                    title: [必传]主段位
            no	integer($int64)
                    title: [必传]对应截断后的歌词行号,从0开始为第一行
            roundSeq	integer($int64)
                    title: [必传]轮次顺序
            score	integer($int64)
                    title: [必传]机器分数
            sign	string
            title: [必传]签名 md5(skrer|userID|itemID|score|no|gameID|mainLevel|singScond|roundSeq|timeMs)
            singSecond	integer($int64)
                    title: [必传]已唱秒数
            timeMs	integer($int64)
                    title: [必传] 当前毫秒时间戳
            userID	integer($int64)
                    title: [必传]演唱者
        */
    @PUT("http://dev.game.inframe.mobi/v1/game/pk-commit-segment-result")
    Observable<ApiResult> sendPkPerSegmentResult(@Body RequestBody body);


    @POST("http://dev.grade.inframe.mobi/v1/grade/sings")
    Observable<ApiResult> requestMelpScore(@Body RequestBody body);

}
