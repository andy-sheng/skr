package com.module.home;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
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
    Observable<ApiResult> getGameConfig(@Query("mode") int mode, @Query("debugOpen") boolean debug);

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
}
