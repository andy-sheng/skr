package com.module.home.feedback;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FeedbackServerApi {
    /**
     * 用户反馈
     *
     * @param body   createAt	int64 反馈时间戳(必传)
     *               updatedAt	int64 服务器使用
     *               userID	uint32 用户编号
     *               nickName	string昵称
     *               appVer	string app版本 (必传)
     *               platform	.user.EPlatform 系统类型
     *               channel	string渠道 (必传)
     *               source	ESource 反馈来源:个人中心，房间反馈 (必传)   1房间反馈 2个人中心
     *               type	EType	repeated反馈类型 (必传) 1问题	2功能
     *               content	string 反馈内容 (必传)
     *               appLog	string app日志url (必传)
     *               status	EStatus 状态 服务器使用
     * @return
     */
    @POST("/v1/feedbacks")
    Observable<ApiResult> feedback(@Body RequestBody body);
}
