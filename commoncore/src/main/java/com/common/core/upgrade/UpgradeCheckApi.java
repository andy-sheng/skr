package com.common.core.upgrade;

import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;


public interface UpgradeCheckApi {
    /**
     * {
     *     "errno": 0,
     *     "errmsg": "",
     *     "data": {
     *         "needUpdate": true,
     *         "updateInfo": {
     *             "downloadURL": "http://xxx",
     *             "updateTimeMs": 1548242332000,
     *             "latestVersionCode": 10001,
     *             "forceUpdate": true,
     *             "packageSize": 123,
     *             "updateTitle": "测试标题",
     *             "updateContent": "测试内容..."
     *         }
     *     },
     *     "traceId": "5c497e5c53a42b4b6e00006f"
     * }
     * @param packageName
     * @param platform
     * @param channel
     * @param versionCode
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @GET("http://dev.api.inframe.mobi/v1/kconf/app-version")
    io.reactivex.Observable<ApiResult> getUpdateInfo(@Query("packageName")String packageName,
                                                     @Query("platform")int platform,
                                                     @Query("channel")int channel,
                                                     @Query("versionCode")int versionCode
    );
}
