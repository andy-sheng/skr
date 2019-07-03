package com.module.playways.doubleplay;

import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface DoubleRoomServerApi {

    /**
     * {
     * "roomID": 0
     * }
     * 时间到结束房间
     *
     * @param body
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/magpie/close")
    Observable<ApiResult> closeByTimerOver(@Body RequestBody body);

    /**
     * {
     * "roomID": 0
     * }
     * 进入房间失败，需要让服务器知道
     *
     * @param body
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/magpie/enter-failed")
    Observable<ApiResult> enterRoomFailed(@Body RequestBody body);

    /**
     * {
     * "roomID": 0
     * }
     * 直接退出房间
     *
     * @param body
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/magpie/exit")
    Observable<ApiResult> exitRoom(@Body RequestBody body);

    /**
     * 获取用户游戏数据
     * 例如，还剩多少个匹配次数
     *
     * @return
     */
    @GET("http://dev.game.inframe.mobi/v1/magpie/game-data")
    Observable<ApiResult> getGameData();

    /**
     * 邀请完一个人之后需要轮询拉接口
     *
     * @return
     */
    @GET("http://dev.game.inframe.mobi/v1/magpie/invite-result")
    Observable<ApiResult> getInviteEnterResult();

    /**
     * 房间结束信息
     *
     * @return
     */
    @GET("http://dev.game.inframe.mobi/v1/magpie/result")
    Observable<ApiResult> getEndGameInfo(@Query("roomID") int roomID);

    /**
     * 被邀请的人进入房间时候调用
     */
    @PUT("http://dev.game.inframe.mobi/v1/magpie/invite-user-enter")
    Observable<ApiResult> toInvitedRoom(@Body RequestBody body);

    /**
     * {
     * "birthday": "string",
     * "sex": "unknown"
     * }
     *
     * @param roomID
     * @return
     */
    @GET("http://dev.game.inframe.mobi/v1/magpie/modify-user-info")
    Observable<ApiResult> modifyUserInfo(@Query("roomID") int roomID);

    /**
     * {
     * "roomID" : 11111,
     * }
     *
     * @param body
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/magpie/next-music")
    Observable<ApiResult> nextSong(@Body RequestBody body);

    /**
     * {
     * "roomID" : 11111,
     * }
     *
     * @param body
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/magpie/invite-user-enter")
    Observable<ApiResult> enterInvitedDoubleRoom(@Body RequestBody body);

    /**
     * {
     * "count": 0,
     * "fromPickuserID": 0,
     * "roomID": 0,
     * "toPickUserID": 0
     * }
     *
     * @param body
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/magpie/pick")
    Observable<ApiResult> pickOther(@Body RequestBody body);

    /**
     * 请求匹配
     * {
     * "platform": "PF_UNKNOWN",
     * "sex": "unknown",
     * "wantPeerSex": "unknown"
     * }
     *
     * @param body
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/magpie/query-match")
    Observable<ApiResult> quaryMatch(@Body RequestBody body);

    /**
     * 取消匹配
     * {
     * "platform": "PF_UNKNOWN"
     * }
     *
     * @param body
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/magpie/cancel-match")
    Observable<ApiResult> cancleMatch(@Body RequestBody body);

    /**
     * 获取背景音乐
     *
     * @return
     */
    @GET("http://dev.game.inframe.mobi/v1/magpie/on-match-music")
    Observable<ApiResult> getDoubleMatchMusic();

    /**
     * {
     * "inviteUserID": 0
     * }
     * 邀请一个人畅聊
     *
     * @param body
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/magpie/send-invite-user")
    Observable<ApiResult> sendInvite(@Body RequestBody body);

    /**
     * 主动获取游戏状态
     *
     * @return
     */
    @GET("http://dev.game.inframe.mobi/v1/magpie/sync-status")
    Observable<ApiResult> syncStatus(@Query("roomID") int roomID);

    /**
     * 拉取已点歌曲
     *
     * @return
     */
    @GET("http://dev.game.inframe.mobi/v1/magpie/list-music")
    Observable<ApiResult> getSongList(@Query("roomID") int roomID, @Query("offset") long offset, @Query("limit") int limit);

    /**
     * {
     * "roomID": 0
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/magpie/unlock-user-info")
    Observable<ApiResult> unLock(@Body RequestBody body);

    /**
     * {
     * "itemID": 0,
     * "roomID": 0
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/magpie/add-music")
    Observable<ApiResult> addSong(@Body RequestBody body);

    /**
     * {
     * "roomID": 0,
     * "uniqTag": "string"
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/magpie/del-music")
    Observable<ApiResult> deleteSong(@Body RequestBody body);

    /**
     * {
     * "roomID": 0
     * }
     *
     * @return
     */
    @GET("http://dev.game.inframe.mobi/v1/magpie/room-user-info")
    Observable<ApiResult> getRoomUserInfo(@Query("roomID") int roomID);

    /**
     * {}
     *
     * @param body
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/magpie/room-create")
    Observable<ApiResult> createRoom(@Body RequestBody body);

    /**
     * 双人房间内邀请
     *
     * @param body {
     *             "inviteUserID": 0,
     *             "roomID": 0
     *             }
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/magpie/room-send-invite-user")
    Observable<ApiResult> roomSendInvite(@Body RequestBody body);

    /**
     * 双人房邀请响应
     *
     * @param body {
     *             "peerUserID": 0,
     *             "roomID": 0
     *             }
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/magpie/room-invite-user-enter")
    Observable<ApiResult> roomInviteEnter(@Body RequestBody body);

}
