package com.module.playways.grab.room;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface GrabRoomServerApi {

    /**
     * {
     * "roomID" : 111
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v1/room/exit-room")
    Observable<ApiResult> exitRoom(@Body RequestBody body);

    /**
     * {
     * "roomID": 0,
     * "tagID": 0
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v1/room/change-room")
    Observable<ApiResult> switchRoom(@Body RequestBody body);

    /**
     * {
     * "roomID" : 111,
     * "roundSeq" : 1
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.stand.inframe.mobi/v1/stand/m-light")
    Observable<ApiResult> lightOff(@Body RequestBody body);

    /**
     * {
     * "roomID" : 111,
     * "roundSeq" : 1
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.stand.inframe.mobi/v1/stand/b-light")
    Observable<ApiResult> lightBurst(@Body RequestBody body);


    /**
     * 相当于告知服务器，我不抢
     * {
     * "roomID" : 20001505,
     * "roundSeq" : 1
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.stand.inframe.mobi/v1/stand/intro-over")
    Observable<ApiResult> sendGrapOver(@Body RequestBody body);

    /**
     * {
     * "roomID" : 30000004,
     * "itemID" : 11,
     * "sysScore": 90,
     * "audioURL" : "http://audio-4.xxxxxx.com",
     * "timeMs" : 1545312998095,
     * "sign" : "239f75edd08c029b2dbb012e0c6d931d"
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.stand.inframe.mobi/v1/stand/resource")
    Observable<ApiResult> saveRes(@Body RequestBody body);


    /**
     * {
     * "roomID" : 30000004
     * }
     *
     * @param roomID
     * @return
     */
    @GET("http://dev.stand.inframe.mobi/v1/stand/result")
    Observable<ApiResult> getStandResult(@Query("roomID") int roomID);

    /**
     * {
     * "roomID" : 20001505,
     * "roundSeq" : 1
     * }
     * 上报结束一轮游戏
     *
     * @param body 游戏标识 roomID (必选)
     *             机器评分 sysScore (必选)
     *             时间戳 timeMs (必选)
     *             签名  sign (必选)  md5(skrer|roomID|score|timeMs)
     * @return 当前轮次结束时间戳roundOverTimeMs
     * 当前轮次信息currentRound
     * 下个轮次信息nextRound
     */
    @PUT("http://dev.stand.inframe.mobi/v1/stand/round-over")
    Observable<ApiResult> sendRoundOver(@Body RequestBody body);

    /**
     * {
     * "roomID" : 111,
     * "status" : 1
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.stand.inframe.mobi/v1/stand/swap")
    Observable<ApiResult> swap(@Body RequestBody body);

    @GET("http://dev.stand.inframe.mobi/v1/stand/sync-status")
    Observable<ApiResult> syncGameStatus(@Query("roomID") int roomID);

    /**
     * {
     * "roomID" : 11111,
     * "roundSeq" : 1
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.stand.inframe.mobi/v1/stand/want-sing-chance")
    Observable<ApiResult> wangSingChance(@Body RequestBody body);

    //放弃演唱
    @PUT("http://dev.stand.inframe.mobi/v1/stand/give-up")
    Observable<ApiResult> giveUpSing(@Body RequestBody body);

    /*----------牛逼---------*/

    //检查要不要显示红包领取
    @GET("http://dev.api.inframe.mobi/v1/task/list-newbee-task")
    Observable<ApiResult> checkRedPkg();

    //接受红包
    @PUT("http://dev.api.inframe.mobi/v1/task/trigger-task-reward")
    Observable<ApiResult> receiveCash(@Body RequestBody body);



}
