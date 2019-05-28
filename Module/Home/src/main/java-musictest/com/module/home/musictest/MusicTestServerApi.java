package com.module.home.musictest;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface MusicTestServerApi {

    /**
     * 检测是否进行过音乐测试
     *
     * @return
     */
    @GET("v1/questions/answer/before-game")
    Observable<ApiResult> checkHasTest();

    /**
     * 获取音乐测试题目
     *
     * @return
     */
    @GET("v1/questions/list")
    Observable<ApiResult> getQuestionList();

    /**
     * 上报音乐测试用户答案数据
     * 
     * @param body
     * @return
     */
    @PUT("/v1/questions/select")
    Observable<ApiResult> reportAnswer(@Body RequestBody body);
}
