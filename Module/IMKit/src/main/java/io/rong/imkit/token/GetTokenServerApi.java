package io.rong.imkit.token;

import com.alibaba.fastjson.JSONObject;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GetTokenServerApi {

    /**
     * 从融云服务器获取token(返回格式json）
     * @param useId
     * @param name
     * @param portraitUri
     * @return
     */
    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded")
    @POST("user/getToken.json")
    Observable<JSONObject> getToken(@Field("userId") String useId,
                                    @Field("name") String name,
                                    @Field("portraitUri") String portraitUri);
}
