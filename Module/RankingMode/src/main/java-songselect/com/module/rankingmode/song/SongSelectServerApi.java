package com.module.rankingmode.song;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SongSelectServerApi {

    /**
     * 获取曲库剧本的标签
     *
     * @param mode
     * @param offset
     * @param cnt
     * @return
     */
    @GET("v1/playbook/list-tags")
    Observable<ApiResult> getSongsListTags(@Query("mode") int mode,
                                           @Query("offset") int offset,
                                           @Query("cnt") int cnt);

    /**
     * 获取曲库剧本的详细条目
     *
     * @param tag
     * @param offset
     * @param cnt
     * @return
     */
    @GET("v1/playbook/list-items")
    Observable<ApiResult> getSongDetailListItems(@Query("tag") int tag,
                                @Query("offset") int offset,
                                @Query("cnt") int cnt);


    /**
     * 搜索曲库剧本的详细条目
     * @param key
     * @return
     */
    @GET("v1/playbook/search-items")
    Observable<ApiResult> searchSongDetail(@Query("keyword") String key);

}
