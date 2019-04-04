package com.module.playways.grab.room;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface GrabRoomServerApi {

    /** room 相关 **/

    /**
     *  请求发JoinNotice的push
     *
     * @param body 游戏标识 gameID (必选)
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v2/room/join-room")
    Observable<ApiResult> joinGrabRoom(@Body RequestBody body);

    /**
     * {
     *   "roomType": "RT_UNKNOWN",
     *   "tagID": 0
     * }
     * @param body
     * @return
     */
    //创建房间
    @PUT("http://dev.room.inframe.mobi/v2/room/create-room")
    Observable<ApiResult> createRoom(@Body RequestBody body);

    //检查公开房间有没有权限
    @GET("http://dev.room.inframe.mobi/v2/room/public-permission")
    Observable<ApiResult> checkCreatePublicRoomPermission();

    /**
     * {
     * "roomID" : 111
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v2/room/exit-room")
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
    @PUT("http://dev.room.inframe.mobi/v2/room/change-room")
    Observable<ApiResult> changeRoom(@Body RequestBody body);


    /** stand 相关 **/
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

    /**
     *
     * @param body
     *   "kickUserID": 0,
     *   "roomID": 0,
     *   "roundSeq": 0
     * @return
     */
    @PUT("http://dev.stand.inframe.mobi/v1/stand/req-kick-user")
    Observable<ApiResult> reqKickUser(@Body RequestBody body);

    /**
     * 回应踢人请求
     * @param body
     *   "agree": true,
     *   "kickUserID": 0,
     *   "roomID": 0,
     *   "sourceUserID": 0
     * @return
     */
    @PUT("http://dev.stand.inframe.mobi/v1/stand/agree-kick-user")
    Observable<ApiResult> repKickUser(@Body RequestBody body);


    /** 其余模块接口 **/

    //检查要不要显示红包领取
    @GET("http://dev.api.inframe.mobi/v1/task/list-newbee-task")
    Observable<ApiResult> checkRedPkg();

    //接受红包
    @PUT("http://dev.api.inframe.mobi/v1/task/trigger-task-reward")
    Observable<ApiResult> receiveCash(@Body RequestBody body);

    /**
     * 获取房间内的歌曲
     *
     * @param roomID
     * @param offset
     * @param limit
     * @return
     */
    @GET("http://dev.room.inframe.mobi/v2/room/playbook")
    Observable<ApiResult> getPlaybook(@Query("roomID") int roomID, @Query("offset") long offset, @Query("limit") int limit);

    /**
     * 房主改变当前房间的tag
     * {
     * "newTagID": 0,
     * "roomID": 0
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v2/room/change-music-tag")
    Observable<ApiResult> changeMusicTag(@Body RequestBody body);

    /**
     * 房主添加歌曲
     * {
     * "playbookItemID": 0
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v2/room/add-music")
    Observable<ApiResult> addMusic(@Body RequestBody body);

    /**
     * 房主闪促歌曲
     * {
     * "playbookItemID": 0,
     * "roundReq": 0
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v2/room/del-music")
    Observable<ApiResult> delMusic(@Body RequestBody body);

    /**
     * 得到专场列表
     *
     * @param offset
     * @param count
     */
    @GET("http://dev.api.inframe.mobi/v1/playbook/list-stand-tags")
    Observable<ApiResult> getSepcialList(@Query("offset") int offset, @Query("cnt") int count);

    /**
     * 获取好友列表
     * @param offset
     * @param count
     * @return
     */
    @GET("http://dev.api.inframe.mobi/v1/mate/room-friends")
    Observable<ApiResult> getRoomFriendList(@Query("offset") int offset, @Query("cnt") int count);

    /**
     * 邀请好友
     * {
     *   "roomID": 0,
     *   "userID": 0
     * }
     * @param body
     * @return
     */
    @PUT("http://dev.api.inframe.mobi/v1/mate/room-invite")
    Observable<ApiResult> inviteFriend(@Body RequestBody body);
}
