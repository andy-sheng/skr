package com.module.home;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 *
 */
public interface MainPageSlideApi {

    /**
     * 拿到某个人基本的信息
     * @return
     */
    @GET("v1/kconf/slide-show")
    Observable<ApiResult> getSlideList();

    @GET("http://dev.game.inframe.mobi/v1/kconf/game-play")
    Observable<ApiResult> getGameConfig(@Query("mode") int mode, @Query("debugOpen") boolean debug);

    @GET("http://dev.kconf.inframe.mobi")
    Observable<ApiResult> getKConfig();

}
