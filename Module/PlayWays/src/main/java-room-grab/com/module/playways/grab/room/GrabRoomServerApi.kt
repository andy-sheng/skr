package com.module.playways.grab.room

import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiResult

import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Query

interface GrabRoomServerApi {

    // 抢唱页面
    @GET("v3/playbook/stand-tag-list")
    fun getStandTagList(@Query("userID") userID: Long, @Query("offset") offset: Int, @Query("cnt") cnt: Int): Call<ApiResult>

    /**
     * 获取一唱到底的表情列表
     *
     * @return
     */
    @get:GET("http://dev.stand.inframe.mobi/v1/stand/list-emoji")
    val dynamicEmoji: Observable<ApiResult>

    /**
     * 获取推荐tag列表
     *
     * @return
     */
    @get:GET("http://dev.api.inframe.mobi/v1/playbook/stand-billboards")
    val standBillBoards: Observable<ApiResult>

    /**
     * room 相关
     */

    /**
     * 房主开始游戏
     *
     * @param body
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v2/room/game-begin")
    fun ownerBeginGame(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 请求发JoinNotice的push
     *
     * @param body 游戏标识 gameID (必选)
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.room.inframe.mobi/v2/room/join-room")
    fun joinGrabRoom(@Body body: RequestBody): Observable<ApiResult>

    /**
     * {
     * "roomType": "RT_UNKNOWN",
     * "tagID": 0
     * }
     *
     * @param body
     * @return
     */
    //创建房间
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.room.inframe.mobi/v2/room/create-room")
    fun createRoom(@Body body: RequestBody): Observable<ApiResult>


    //检查公开房间有没有权限
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @GET("http://dev.room.inframe.mobi/v3/room/public-permission")
    fun checkCreatePublicRoomPermission(): Observable<ApiResult>

    /**
     * {
     * "roomID" : 111
     * }
     *
     * @param body
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.room.inframe.mobi/v3/room/exit-room")
    fun exitRoom(@Body body: RequestBody): Observable<ApiResult>

    /**
     * {
     * "roomID": 0,
     * "tagID": 0
     * }
     *
     * @param body
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.room.inframe.mobi/v2/room/change-room")
    fun changeRoom(@Body body: RequestBody): Observable<ApiResult>


    /** stand 相关  */
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
    fun lightOff(@Body body: RequestBody): Observable<ApiResult>

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
    fun lightBurst(@Body body: RequestBody): Observable<ApiResult>


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
    fun sendGrapOver(@Body body: RequestBody): Observable<ApiResult>

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
    fun saveRes(@Body body: RequestBody): Observable<ApiResult>


    /**
     * {
     * "roomID" : 30000004
     * }
     *
     * @param roomID
     * @return
     */
    @GET("http://dev.stand.inframe.mobi/v2/stand/result")
    fun getStandResult(@Query("roomID") roomID: Int): Observable<ApiResult>

    /**
     * {
     * "roomID" : 20001505,
     * "roundSeq" : 1
     * }
     * 上报结束一轮游戏
     *
     * @param body 游戏标识 roomID (必选)
     * 机器评分 sysScore (必选)
     * 时间戳 timeMs (必选)
     * 签名  sign (必选)  md5(skrer|roomID|score|timeMs)
     * @return 当前轮次结束时间戳roundOverTimeMs
     * 当前轮次信息currentRound
     * 下个轮次信息nextRound
     */
    @PUT("http://dev.stand.inframe.mobi/v1/stand/round-over")
    fun sendRoundOver(@Body body: RequestBody): Observable<ApiResult>

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
    fun swap(@Body body: RequestBody): Observable<ApiResult>

    @GET("http://dev.stand.inframe.mobi/v1/stand/sync-status")
    fun syncGameStatus(@Query("roomID") roomID: Int): Observable<ApiResult>

    /**
     * {
     * "roomID" : 11111,
     * "roundSeq" : 1
     * }
     *
     * @param body
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.stand.inframe.mobi/v2/stand/want-sing-chance")
    fun wangSingChance(@Body body: RequestBody): Observable<ApiResult>

    //放弃演唱
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.stand.inframe.mobi/v1/stand/give-up")
    fun giveUpSing(@Body body: RequestBody): Observable<ApiResult>

    //房主控制结束小游戏
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.stand.inframe.mobi/v1/stand/owner-stop-mini-game")
    fun stopMiniGameByOwner(@Body body: RequestBody): Observable<ApiResult>

    //房主控制结束自由麦
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.stand.inframe.mobi/v1/stand/owner-stop-free-micro")
    fun stopFreeMicroByOwner(@Body body: RequestBody): Observable<ApiResult>

    /**
     * @param body "kickUserID": 0,
     * "roomID": 0,
     * "roundSeq": 0
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.stand.inframe.mobi/v1/stand/req-kick-user")
    fun reqKickUser(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 回应踢人请求
     *
     * @param body "agree": true,
     * "kickUserID": 0,
     * "roomID": 0,
     * "sourceUserID": 0
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.stand.inframe.mobi/v1/stand/agree-kick-user")
    fun rspKickUser(@Body body: RequestBody): Observable<ApiResult>


    /**
     * 其余模块接口
     */

    //检查要不要显示红包领取
    @GET("http://dev.api.inframe.mobi/v1/task/list-newbee-task")
    fun checkRedPkg(): Observable<ApiResult>

    //接受红包
    @PUT("http://dev.api.inframe.mobi/v1/task/trigger-task-reward")
    fun receiveCash(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 获取房间内的歌曲
     *
     * @param roomID
     * @param offset
     * @param limit
     * @return
     */
    @GET("http://dev.room.inframe.mobi/v2/room/playbook")
    fun getPlaybook(@Query("roomID") roomID: Int, @Query("offset") offset: Long, @Query("limit") limit: Int): Observable<ApiResult>

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
    fun changeMusicTag(@Body body: RequestBody): Observable<ApiResult>

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
    fun addMusic(@Body body: RequestBody): Observable<ApiResult>

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
    fun delMusic(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 修改房间名
     *
     * @param body "roomID": 0,
     * "roomName": "string"
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v2/room/update-name")
    fun updateRoomName(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 得到专场列表
     *
     * @param offset
     * @param count
     */
    @GET("http://dev.api.inframe.mobi/v1/playbook/list-tags")
    fun getSepcialList(@Query("offset") offset: Int, @Query("cnt") count: Int, @Query("mediaType") type: Int): Observable<ApiResult>

    /**
     * 获取好友列表
     *
     * @param offset
     * @param count
     * @return
     */
    @GET("http://dev.api.inframe.mobi/v1/mate/room-friends")
    fun getRoomFriendList(@Query("offset") offset: Int, @Query("cnt") count: Int): Observable<ApiResult>

    //    /**
    //     * 获取粉丝列表
    //     *
    //     * @param offset
    //     * @param count
    //     * @return
    //     */
    //    @GET("http://dev.api.inframe.mobi/v1/mate/room-fans")
    //    Observable<ApiResult> getRoomFansList(@Query("offset") int offset, @Query("cnt") int count);

    /**
     * 获取粉丝列表
     *
     * @param offset
     * @param count
     * @return
     */
    @GET("http://dev.api.inframe.mobi/v1/mate/room-fans-by-page")
    fun getRoomFansList(@Query("offset") offset: Int, @Query("cnt") count: Int): Observable<ApiResult>

    /**
     * 搜索房间内粉丝
     *
     * @param searchContent
     * @return
     */
    @GET("http://dev.api.inframe.mobi/v1/mate/room-search-fans")
    fun searchFans(@Query("searchContent") searchContent: String): Observable<ApiResult>

    /**
     * 邀请好友
     * {
     * "roomID": 0,
     * "userID": 0
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.api.inframe.mobi/v1/mate/room-invite")
    fun inviteFriend(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 发送一唱到底表情
     *
     * @param body "gameID": 0  游戏id
     * "id": 0      表情id
     * @return
     */
    @PUT("http://dev.stand.inframe.mobi/v1/stand/send-emoji")
    fun sendDynamicEmoji(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 用户是否有未激活的红包
     *
     * @return
     */
    @GET("http://dev.api.inframe.mobi/v1/redbag/check-newbie-task")
    fun checkNewBieTask(): Observable<ApiResult>

    /**
     * 激活红包
     *
     * @param body
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.api.inframe.mobi/v1/redbag/trigger-newbie-task")
    fun triggerNewBieTask(@Body body: RequestBody): Observable<ApiResult>


    /*
     // 提交单句反馈请求
message STCommitSegmentResultReq
{
    uint32 userID    = 1; // [必传]演唱者
    uint32 itemID    = 2; // [必传]演唱曲目
    uint32 score     = 3; // [必传]机器分数
    uint32 no        = 4; // [必传]对应截断后的歌词行号,从0开始为第一行
    uint32 gameID    = 5; // [必传]游戏ID
    uint32 mainLevel = 6; // [忽略]主段位
    uint32 singSecond= 7; // [忽略]已唱秒数
    uint32 roundSeq  = 8; // [必传]轮次顺序
    sint64 timeMs    = 9; //[必传] 当前毫秒时间戳
    string sign      = 10; //[必传]签名 md5(skrer|userID|itemID|score|no|gameID|mainLevel|singSecond|roundSeq|timeMs)
}
      */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.stand.inframe.mobi/v1/stand/pk-commit-segment-result")
    fun sendPkPerSegmentResult(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 获取推荐歌曲
     *
     * @return
     */
    @GET("http://dev.api.inframe.mobi/v1/playbook/list-stand-billboard")
    fun getListStandBoards(@Query("type") type: Int, @Query("offset") offset: Int, @Query("cnt") count: Int): Observable<ApiResult>

    /**
     * 获取推荐歌曲
     *
     * @return
     */
    @GET("http://dev.api.inframe.mobi/v1/playbook/list-magpie-billboard")
    fun getDoubleListStandBoards(@Query("type") type: Int, @Query("offset") offset: Int, @Query("cnt") count: Int): Observable<ApiResult>


    /**
     * 非房主申请点歌
     *
     * @param body {
     * "itemID": 0,
     * "roomID": 0
     * }
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v1/room/suggest-music")
    fun suggestMusic(@Body body: RequestBody): Observable<ApiResult>


    /**
     * 房主获取用户点的歌曲
     *
     * @param roomID
     * @param offset
     * @param limit
     * @return
     */
    @GET("http://dev.room.inframe.mobi/v1/room/list-music-suggested")
    fun getListMusicSuggested(@Query("roomID") roomID: Int, @Query("offset") offset: Long, @Query("limit") limit: Int): Observable<ApiResult>

    /**
     * 房主添加用户点的歌曲
     *
     * @param body {
     * "itemID": 0,
     * "roomID": 0
     * }
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v1/room/add-music-suggested")
    fun addSuggestMusic(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 房主删除用户点的歌曲
     *
     * @param body {
     * "itemID": 0,
     * "roomID": 0
     * }
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v1/room/del-music-suggested")
    fun deleteSuggestMusic(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 房主添加自定义小游戏
     *
     * @param body
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v1/room/add-custom-game")
    fun addCustomGame(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 礼物60秒打卡
     *
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @GET("http://dev.api.inframe.mobi/v1/bonus/ask-for-flower")
    fun punch(@Query("timestamp") offset: Long, @Query("signV2") signV2: String): Observable<ApiResult>

    /**
     * 上传个人标签声音资源
     * @param body
     * @return
     */
    @PUT("http://dev.api.inframe.mobi/v1/stand/challenge-resource")
    fun uploadChallengeResource(@Body body: RequestBody): Call<ApiResult>


    @GET("http://dev.api.inframe.mobi/v1/stand/challenge-star-cnt")
    fun getChallengeStarCount(@Query("roomID") roomID: Int, @Query("enterRoundSeq") enterRoundSeq: Int, @Query("curRoundSeq") curRoundSeq: Int): Call<ApiResult>
}
