package com.module.playways.room.song;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface SongSelectServerApi {

    /**
     * 根据游戏model列出剧本
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
     * 搜索曲库剧本的详细条目
     *
     * @param key
     * @return
     */
    @GET("v1/playbook/search-items")
    Observable<ApiResult> searchSongDetail(@Query("keyword") String key);


    /**
     * 获取推荐的列表
     *
     * @param offset
     * @param cnt
     * @return 包含  歌曲信息 items（List）
     * 偏移量 offset
     */
    @GET("/v2/recommend/list-playbook-items")
    Observable<ApiResult> getRcomdMusicItems(@Query("offset") int offset,
                                             @Query("cnt") int cnt);


    /**
     * 获取已点的列表
     *
     * @param offset
     * @param cnt
     * @return 包含  歌曲信息 items（List）
     * 偏移量 offset
     */
    @GET("/v1/mate/list-playbook-items-clicked")
    Observable<ApiResult> getClickedMusicItmes(@Query("offset") int offset,
                                               @Query("cnt") int cnt);

    /**
     * 普通搜索
     * @param keyword
     * @return
     */
    @GET("/v1/playbook/search-items")
    Observable<ApiResult> searchMusicItems(@Query("keyword") String keyword);

    /**
     * 一唱到底搜索
     * @param keyword
     * @return
     */
    @GET("/v1/playbook/search-stand-intro")
    Observable<ApiResult> searchGrabMusicItems(@Query("keyword") String keyword);

    /**
     * 双人房搜歌
     * @param keyword
     * @return
     */
    @GET("/v1/playbook/search-magpie-item")
    Observable<ApiResult> searchDoubleMusicItems(@Query("keyword")String keyword);

    @GET("/v1/playbook/report-not-exist-song")
    Observable<ApiResult> reportNotExistSong(@Query("name") String name,
                                             @Query("artist") String artist);

    @PUT("http://dev.game.inframe.mobi/v1/game/practice-room/enter")
    Observable<ApiResult> reportAuditionSong(@Body RequestBody body);
}

