package com.component.notification.api;

import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 *
 */
public interface NotifyReqApi {

    /**
     * {
     * "peerUserID": 0
     * }
     *
     * @param body
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/magpie/invite-user-enter")
    Observable<ApiResult> enterInvitedDoubleRoom(@Body RequestBody body);

    /**
     * 拒绝邀请
     *
     * @param body {"peerUserID": 0}
     * @return
     */
    @PUT("http://dev.game.inframe.mobi/v1/magpie/refuse-enter")
    Observable<ApiResult> refuseInvitedDoubleRoom(@Body RequestBody body);

    /**
     * 这个是从唱聊房里邀请，收到邀请之后点击进入房间的短链接
     * {
     * "peerUserID": 0,
     * "roomID": 0
     * }
     *
     * @param body
     * @return
     */
    @Headers(ApiManager.ALWAYS_LOG_TAG)
    @PUT("http://dev.game.inframe.mobi/v1/magpie/room-invite-user-enter")
    Observable<ApiResult> enterInvitedDoubleFromCreateRoom(@Body RequestBody body);

}
