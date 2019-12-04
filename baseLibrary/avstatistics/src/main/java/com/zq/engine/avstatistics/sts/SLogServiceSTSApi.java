package com.zq.engine.avstatistics.sts;


import retrofit2.Call;
import retrofit2.http.GET;

public interface SLogServiceSTSApi
{

    /**
     * 获取上传sts token
     *
     * @return
     */
    @GET("http://dev.res.inframe.mobi/v1/getLogSTSToken/skrApp")
    Call<String> getSTSTokenByString();
}