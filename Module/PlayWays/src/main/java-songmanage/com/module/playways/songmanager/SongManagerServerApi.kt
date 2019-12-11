package com.module.playways.songmanager

import com.common.rxretrofit.ApiResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface SongManagerServerApi {

    /**
     * 获取推荐tag列表(一唱到底)
     *
     * @return
     */
    @get:GET("http://dev.api.inframe.mobi/v1/playbook/stand-billboards")
    val standBillBoards: Observable<ApiResult>

    /**
     * 获取推荐歌曲(一唱到底)
     *
     * @return
     */
    @GET("http://dev.api.inframe.mobi/v1/playbook/list-stand-billboard")
    fun getListStandBoards(@Query("type") type: Int, @Query("offset") offset: Int, @Query("cnt") count: Int): Observable<ApiResult>

    /**
     * 房主添加歌曲(一唱到底)
     * {
     * "playbookItemID": 0
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v2/room/add-music")
    fun addStandMusic(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 房主删除歌曲（一唱到底）
     * {
     * "playbookItemID": 0,
     * "roundReq": 0
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v2/room/del-music")
    fun delStandMusic(@Body body: RequestBody): Observable<ApiResult>


    /**
     * 获取房间内的歌曲(一场到底 已点)
     *
     * @param roomID
     * @param offset
     * @param limit
     * @return
     */
    @GET("http://dev.room.inframe.mobi/v2/room/playbook")
    fun getPlaybook(@Query("roomID") roomID: Int, @Query("offset") offset: Long, @Query("limit") limit: Int): Observable<ApiResult>


    /**
     * 获取推荐tag列表(双人唱聊)
     *
     * @return
     */
    @GET("http://dev.api.inframe.mobi/v1/playbook/magpie-billboards")
    fun getDoubleStandBillBoards(): Observable<ApiResult>

    /**
     * 获取推荐歌曲(双人唱聊)
     *
     * @return
     */
    @GET("http://dev.api.inframe.mobi/v1/playbook/list-magpie-billboard")
    fun getDoubleListStandBoards(@Query("type") type: Int, @Query("offset") offset: Int, @Query("cnt") count: Int): Observable<ApiResult>

    /**
     * 新增音乐数目(双人唱聊)
     */
    @GET("http://dev.game.inframe.mobi/v1/magpie/get-add-music-cnt")
    fun getAddMusicCnt(@Query("roomID") roomID: Int): Observable<ApiResult>

    /**
     * 拉取已点歌曲(双人唱聊)
     *
     * @return
     */
    @GET("http://dev.game.inframe.mobi/v1/magpie/list-music")
    fun getDoubleExistSongList(@Query("roomID") roomID: Int, @Query("offset") offset: Long, @Query("limit") limit: Int): Observable<ApiResult>

    /**
     * 点歌(双人唱聊)
     * {
     * "itemID": 0,
     * "roomID": 0
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/magpie/add-music")
    fun addDoubleSong(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 删歌(双人唱聊)
     * {
     * "roomID": 0,
     * "uniqTag": "string"
     * }
     *
     * @param body
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/magpie/del-music")
    fun deleteDoubleSong(@Body body: RequestBody): Observable<ApiResult>


    /**
     * 非房主申请点歌
     *
     * @param body {
     * "itemID": 0,
     * "roomID": 0
     * }
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v1/room/suggest-music")
    fun suggestMusic(@Body body: RequestBody): Observable<ApiResult>


    /**
     * 房主获取用户点的歌曲
     *
     * @param roomID
     * @param offset
     * @param limit
     * @return
     */
    @GET("http://dev.room.inframe.mobi/v1/room/list-music-suggested")
    fun getListMusicSuggested(@Query("roomID") roomID: Int, @Query("offset") offset: Long, @Query("limit") limit: Int): Observable<ApiResult>

    /**
     * 房主添加用户点的歌曲
     *
     * @param body {
     * "itemID": 0,
     * "roomID": 0
     * }
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v1/room/add-music-suggested")
    fun addSuggestMusic(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 房主删除用户点的歌曲
     *
     * @param body {
     * "itemID": 0,
     * "roomID": 0
     * }
     * @return
     */
    @PUT("http://dev.room.inframe.mobi/v1/room/del-music-suggested")
    fun deleteSuggestMusic(@Body body: RequestBody): Observable<ApiResult>

    /**
     * 获取推荐tag列表(排麦房)
     *
     */
    @GET("http://dev.micgame.inframe.mobi/v1/micgame/song-tabs")
    fun getMicSongTagList(): Call<ApiResult>

    /**
     * 获取推荐歌曲列表(排麦房)
     */
    @GET("http://dev.micgame.inframe.mobi/v1/micgame/song-list")
    fun getMicSongList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Int, @Query("tab") tab: Int): Observable<ApiResult>

    /**
     * 获取已点歌曲列表(排麦房)
     */
    @GET("http://dev.micgame.inframe.mobi/v1/micgame/list-music")
    fun getMicExistSongList(@Query("roomID") roomID: Int, @Query("userID") userID: Int, @Query("offset") offset: Int, @Query("limit") limit: Int): Call<ApiResult>


    /**
     * 相唱歌曲(排麦房)
     * {"itemID": 0,"roomID": 0,"wantSingType": "MWST_UNKNOWN"}
     */
    @PUT("http://dev.micgame.inframe.mobi/v1/micgame/want-sing")
    fun addWantMicSong(@Body body: RequestBody): Call<ApiResult>

    /**
     * 删除已点歌曲(排麦房)
     *    {
     *       "roomID": 0,
     *       "uniqTag": "string"
     *     }
     */
    @PUT("http://dev.micgame.inframe.mobi/v1/micgame/del-music")
    fun deleteMicSong(@Body body: RequestBody): Call<ApiResult>

    /**
     * 置顶已点歌曲(排麦房)
     *    {
     *       "roomID": 0,
     *       "uniqTag": "string"
     *     }
     */
    @PUT("http://dev.micgame.inframe.mobi/v1/micgame/up-music")
    fun stickMicSong(@Body body: RequestBody): Call<ApiResult>


    /**
     * 获取推荐tag列表(接唱房)
     *
     */
    @GET("http://dev.game.inframe.mobi/v1/relaygame/song-tabs")
    fun getRelaySongTagList(): Call<ApiResult>

    /**
     * 获取推荐歌曲列表(接唱房)
     */
    @GET("http://dev.game.inframe.mobi/v1/relaygame/song-list")
    fun getRelaySongList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Int, @Query("tab") tab: Int): Observable<ApiResult>

    /**
     * 获取已点歌曲列表(接唱房)
     */
    @GET("http://dev.game.inframe.mobi/v1/relaygame/list-music")
    fun getRelayExistSongList(@Query("roomID") roomID: Int, @Query("userID") userID: Int, @Query("offset") offset: Int, @Query("limit") limit: Int): Call<ApiResult>


    /**
     * 想唱歌曲(接唱房)
     * {"itemID": 0,"roomID": 0}
     */
    @PUT("http://dev.game.inframe.mobi/v1/relaygame/want-sing")
    fun addWantRelaySong(@Body body: RequestBody): Call<ApiResult>

    /**
     * 删除已点歌曲(接唱房)
     *    {
     *       "roomID": 0,
     *       "uniqTag": "string"
     *     }
     */
    @PUT("http://dev.game.inframe.mobi/v1/relaygame/del-music")
    fun deleteRelaySong(@Body body: RequestBody): Call<ApiResult>

    /**
     * 置顶已点歌曲(接唱房)
     *    {
     *       "roomID": 0,
     *       "uniqTag": "string"
     *     }
     */
    @PUT("http://dev.game.inframe.mobi/v1/relaygame/up-music")
    fun stickRelaySong(@Body body: RequestBody): Call<ApiResult>


    /**
     * 获取推荐tag列表(剧场)
     *
     */
    @GET("http://dev.game.inframe.mobi/v1/partygame/music-tabs")
    fun getPartySongTagList(): Call<ApiResult>

    /**
     * 获取推荐歌曲列表(剧场)
     */
    @GET("http://dev.game.inframe.mobi/v1/partygame/list-music")
    fun getPartySongList(@Query("offset") offset: Int, @Query("cnt") cnt: Int, @Query("userID") userID: Int, @Query("tab") tab: Int): Observable<ApiResult>

    /**
     * 获取已点歌曲列表(剧场)
     */
    @GET("http://dev.game.inframe.mobi/v1/partygame/list-add-music")
    fun getPartyExistSongList(@Query("roomID") roomID: Int, @Query("userID") userID: Int, @Query("offset") offset: Int, @Query("limit") limit: Int): Call<ApiResult>



    /**
     * 点歌(剧场)
     * {"itemID": 0,"roomID": 0,"wantSingType": 0}
     */
    @PUT("http://dev.game.inframe.mobi/v1/partygame/add-music")
    fun addPartySong(@Body body: RequestBody): Call<ApiResult>

    /**
     * 删除已点歌曲(剧场)
     *    {
     *       "roomID": 0,
     *       "uniqTag": "string"
     *     }
     */
    @PUT("http://dev.game.inframe.mobi/v1/partygame/del-music")
    fun deletePartySong(@Body body: RequestBody): Call<ApiResult>

    /**
     * 置顶已点歌曲(剧场)
     *    {
     *       "roomID": 0,
     *       "uniqTag": "string"
     *     }
     */
    @PUT("http://dev.game.inframe.mobi/v1/partygame/up-music")
    fun stickPartySong(@Body body: RequestBody): Call<ApiResult>



}