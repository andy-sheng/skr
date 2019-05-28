package com.common.core.userinfo;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 *
 */
public interface UserInfoServerApi {

    /**
     * 拿到某个人基本的信息
     *
     * @param userID
     * @return
     */
    @GET("v1/uprofile/information")
    Observable<ApiResult> getUserInfo(@Query("userID") int userID);


    /**
     * 拿到某些人的信息
     *
     * @param body body: {
     *             "userIDs": [1982416,1156569]
     *             }
     * @return
     */
    @POST("/v1/query/uprofiles")
    Observable<ApiResult> getUserInfos(@Body RequestBody body);


    /**
     * 拿到某个人的个人主页
     *
     * @param userID
     * @return
     */
    @GET("/v1/skr/homepage")
    Observable<ApiResult> getHomePage(@Query("userID") long userID);


    /**
     * 处理关系
     *
     * @param body "toUserID" : 关系被动接受者id
     *             "action" :  创建关系 or 解除关系
     *             0  未知 RA_UNKNOWN
     *             1  创建关系 RA_BUILD
     *             2  解除关系 RA_UNBUILD
     * @return
     */
    @PUT("/v1/mate/relation")
    Observable<ApiResult> mateRelation(@Body RequestBody body);

    /**
     * 成为好友
     *
     * @param body "toUserID" : 关系被动接受者id
     * @return
     */
    @PUT("/v1/mate/mutual-follow")
    Observable<ApiResult> beFriend(@Body RequestBody body);

    /**
     * 获取指定关系列表
     *
     * @param relation [必选]关系类型   0 未知  RC_UNKNOWN
     *                 1 关注  RC_Follow
     *                 2 粉丝  RC_Fans
     *                 3 好友  RC_Friend
     * @param offset   [必选]偏移
     * @param limit    [必选]限制数量,最大50
     * @return
     */
    @GET("/v1/mate/relation")
    Observable<ApiResult> getRelationList(@Query("relation") int relation,
                                          @Query("offset") int offset,
                                          @Query("limit") int limit);


    /**
     * 搜索好友列表
     *
     * @param searchContent 搜索好友
     * @param offset        [必选]偏移
     * @param limit         [必选]限制数量,最大50
     * @return
     */
    @GET("/v1/user/search-users")
    Observable<ApiResult> searchFriendsList(@Query("searchContent") String searchContent,
                                            @Query("offset") int offset,
                                            @Query("limit") int limit);

    @GET("/v1/mate/contacts")
    Observable<ApiResult> getFriendStatusList(@Query("offset") int offset,
                                              @Query("limit") int limit);

    /**
     * 获取指定用户的关系数量
     *
     * @param userID
     * @return
     */
    @GET("/v1/mate/cnt")
    Observable<ApiResult> getRelationNum(@Query("userID") int userID);


    /**
     * 判断和指定某人的社交关系
     *
     * @param toUserID 指定人的id
     * @return
     */
    @GET("/v1/mate/has-relation")
    Observable<ApiResult> getRelation(@Query("toUserID") int toUserID);


    /**
     * 获取某人具体的分数
     *
     * @param userID
     * @return
     */
    @GET("/v1/score/detail")
    Observable<ApiResult> getScoreDetail(@Query("userID") int userID);


    /**
     * 获取地域排行榜列表
     * (包含自己的)
     *
     * @param category 地域类别 1国家  2省会  3市级  4区级
     * @param offset   偏移量
     * @param limit    数量
     * @return
     */
    @GET("/v1/rank/region-list")
    Observable<ApiResult> getReginRankList(@Query("category") int category,
                                           @Query("offset") int offset,
                                           @Query("limit") int limit);

    /**
     * 获取好友排行榜列表
     * (包含自己的)
     *
     * @return
     */
    @GET("/v1/rank/friend-list")
    Observable<ApiResult> getFriendRankList();


    /**
     * 拿到自己的排名
     *
     * @return
     */
    @GET("/v1/rank/region-mine")
    Observable<ApiResult> getMyRegion(@Query("category") int category);


    /**
     * 获得某人的地域排行榜名次
     *
     * @param userID
     * @return
     */
    @GET("/v1/rank/region-seq")
    Observable<ApiResult> getReginRank(@Query("userID") long userID);


    /**
     * 获取自己的排行榜升降
     *
     * @return
     */
    @GET("/v1/rank/region-diff")
    Observable<ApiResult> getReginDiff();


    /**
     * 举报
     *
     * @param body
     * @return
     */
    @PUT("v1/report/upload")
    Observable<ApiResult> report(@Body RequestBody body);

    /**
     * 获取某人的金币
     *
     * @param userID
     * @return
     */
    @GET("http://dev.stand.inframe.mobi/v1/stand/coin-cnt")
    Observable<ApiResult> getCoinNum(@Query("userID") long userID);


    /**
     * 查询照片墙
     *
     * @param userID
     * @param offset
     * @param cnt
     * @return
     */
    @GET("/v1/profile/query-pic")
    Observable<ApiResult> getPhotos(@Query("userID") long userID,
                                    @Query("offset") int offset,
                                    @Query("cnt") int cnt);

    /**
     * 查询照片墙
     *
     * @param userID
     * @param offset
     * @param cnt
     * @return
     */
    @GET("/v1/profile/query-pic")
    Call<ApiResult> getPhotosSync(@Query("userID") long userID,
                                  @Query("offset") int offset,
                                  @Query("cnt") int cnt);

    /**
     * 新增照片墙
     *
     * @param body "picPath": "string"
     * @return
     */
    @PUT("/v1/profile/add-pic")
    Observable<ApiResult> addPhoto(@Body RequestBody body);


    /**
     * 删除照片墙
     *
     * @param body "picID": 0
     * @return
     */
    @PUT("/v1/profile/del-pic")
    Observable<ApiResult> deletePhoto(@Body RequestBody body);

    /**
     * @param cnt 获取第一条传1 列表最大值100
     * @return
     */
    @GET("/v1/mate/latest-relation")
    Observable<ApiResult> getLatestRelation(@Query("cnt") int cnt);


    @GET("/v1/mate/list-follows-by-page")
    Call<ApiResult> listFollowsByPage(@Query("offset") int offset, @Query("cnt") int cnt);

    @GET("/v1/mate/list-follows-by-index-id")
    Call<ApiResult> listFollowsByIndexId(@Query("lastIndexID") long lastIndexID);

    @GET("/v1/mate/list-fans-by-page")
    Observable<ApiResult> listFansByPage(@Query("offset") int offset, @Query("cnt") int cnt);


    @GET("/v1/mate/list-remark-by-index-id")
    Call<ApiResult> listRemarkByIndexId(@Query("lastIndexID") long lastIndexID);

    @GET("/v1/mate/list-remark-by-page")
    Call<ApiResult> listRemarkByPage(@Query("offset") int offset, @Query("cnt") int cnt);

    @PUT("/v1/mate/write-user-remark")
    Observable<ApiResult> writeUserRemark(@Body RequestBody body);


    /**
     * 新增作品
     *
     * @param body "category": EPC_Stand_Normal	1一唱到底
     *                         EPC_Stand_Highlight	2一唱到底高光时刻
     *                         EPC_Practice	3 练歌房
     *                         EPC_Team	4 团队赛
     *             "duration": "string",
     *             "songID": 0,
     *             "worksURL": "string"
     * @return
     */
    @PUT("/v1/profile/add-works")
    Observable<ApiResult> addWorks(@Body RequestBody body);

    /**
     * 删除作品
     *
     * @param body  "worksID": 0
     * @return
     */
    @PUT("/v1/profile/del-works")
    Observable<ApiResult> deleWorks(@Body RequestBody body);

    /**
     * 播放作品
     *
     * @param body  "toUserID": 0,
     *              "worksID": 0
     * @return
     */
    @PUT("/v1/profile/play-works")
    Observable<ApiResult> playWorks(@Body RequestBody body);


    /**
     * 查看作品
     * @param toUserID  查看用户ID（别人必填，自己选填）
     * @param offset
     * @param cnt
     * @return
     */
    @GET("/v1/profile/query-works")
    Observable<ApiResult> getWorks(@Query("toUserID") int toUserID,
                                   @Query("offset") int offset,
                                   @Query("cnt") int cnt);


    @POST("/v2/mate/multi-check-user-online-status")
    Observable<ApiResult> checkUserOnlineStatus(@Body RequestBody body);

    @POST("/v2/mate/multi-check-user-game-status")
    Observable<ApiResult> checkUserGameStatus(@Body RequestBody body);

    @GET("v1/mate/search-fans")
    Observable<ApiResult> searchFans(@Query("searchContent") String searchContent);
}
