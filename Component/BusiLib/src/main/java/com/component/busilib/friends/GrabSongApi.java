package com.component.busilib.friends;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GrabSongApi {

    /**
     * 得到专场列表
     * @param offset
     * @param count
     * @return
     * {
     *     "errno": 0,
     *     "errmsg": "",
     *     "data": {
     *         "tags": [
     *             {
     *                 "tagID": 3,
     *                 "tagName": "怀旧"
     *             },
     *             {
     *                 "tagID": 2,
     *                 "tagName": "流行"
     *             }
     *         ],
     *         "offset": 2
     *     },
     *     "traceId": "5c4429cf53a42b1c5900001b"
     * }
     */
    @GET("http://dev.api.inframe.mobi/v1/playbook/list-stand-tags")
    Observable<ApiResult> getSepcialList(@Query("offset") int offset,@Query("cnt") int count);

    /**
     * 获取一唱到底匹配中播放的音乐
     *
     * @return
     */
    @GET("http://dev.game.inframe.mobi/v1/game/on-match-player-music")
    Observable<ApiResult> getSepcialBgVoice();

    /**
     * 获取在线好友房间
     * @param offset
     * @param count
     * @return
     */
    @GET("http://dev.api.inframe.mobi/v1/mate/room-online-friends")
    Observable<ApiResult> getOnlineFriendsRoom(@Query("offset") int offset,@Query("cnt") int count);


    @GET("http://dev.api.inframe.mobi/v2/mate/recommend-room-list")
    Observable<ApiResult> getRecommendRoomList(@Query("offset") int offset,@Query("cnt") int count);
}
