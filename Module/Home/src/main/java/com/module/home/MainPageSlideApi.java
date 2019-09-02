package com.module.home;

import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 *
 */
public interface MainPageSlideApi {

    /**
     * 拿到某个人基本的信息
     *
     * @return
     */
    @GET("v1/kconf/slide-show")
    Observable<ApiResult> getSlideList();

    @GET("http://dev.game.inframe.mobi/v1/kconf/game-play")
    Call<ApiResult> getGameConfig(@Query("mode") int mode, @Query("debugOpen") boolean debug);

    @GET("http://dev.kconf.inframe.mobi/v1/kconf/app")
    Observable<ApiResult> getKConfig();

    //检查要不要显示红包领取
    @GET("http://dev.api.inframe.mobi/v1/task/list-newbee-task")
    Observable<ApiResult> checkRedPkg();

    //检查每日签到
    @GET("http://dev.api.inframe.mobi/v1/event/app-home-signin-info")
    Observable<ApiResult> checkInInfo();

    //领取
    @POST("http://dev.api.inframe.mobi/v1/event/app-home-signin")
    Observable<ApiResult> signIn(@Body RequestBody body);

    /**
     * 用户是否有未激活的红包
     *
     * @return
     */
    @GET("http://dev.api.inframe.mobi/v1/redbag/check-newbie-task")
    Observable<ApiResult> checkNewBieTask();

    /**
     * 做任务要不要显示红点
     *
     * @return
     */
    @GET("http://dev.api.inframe.mobi/v1/task/show-red-dot")
    Observable<ApiResult> taskRedDotState();

    /**
     * {
     * "peerUserID": 0
     * }
     *
     * @param body
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/magpie/invite-user-enter")
    Observable<ApiResult> enterInvitedDoubleRoom(@Body RequestBody body);

    /**
     * 拒绝邀请
     *
     * @param body {"peerUserID": 0}
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/magpie/refuse-enter")
    Observable<ApiResult> refuseInvitedDoubleRoom(@Body RequestBody body);

    /**
     * 这个是从唱聊房里邀请，收到邀请之后点击进入房间的短链接
     * {
     * "peerUserID": 0,
     * "roomID": 0
     * }
     *
     * @param body
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/magpie/room-invite-user-enter")
    Observable<ApiResult> enterInvitedDoubleFromCreateRoom(@Body RequestBody body);


    /**
     * 获取用户房间数据(每日剩余匹配开房次数)
     *
     * @return
     */
    @GET("http://dev.game.inframe.mobi/v1/magpie/room-data")
    Observable<ApiResult> getRemainTime();


    @GET("http://dev.game.inframe.mobi/v1/raceroom/check-rank")
    Call<ApiResult> checkRank(@Query("roomType")int roomType);
}
