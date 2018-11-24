package io.rong.imkit.messageservice;

import com.alibaba.fastjson.JSONObject;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface MessageSendServerApi {

    /**
     * 发送单聊消息
     * @param fromUserId        发送人用户 Id。（必传）
     * @param toUserId          接收用户 Id，可以实现向多人发送消息，每次上限为 1000 人。（必传）
     * @param objectName        消息类型
     * @param content           发送消息内容
     * @param pushContent       定义显示的 Push 内容
     * @param pushData          iOS 平台(可选)
     * @param count             iOS 平台(可选)
     * @param verifyBlacklist   是否过滤接收用户黑名单列表，0 表示为不过滤、 1 表示为过滤，默认为 0 不过滤。(可选)
     * @param isPersisted       没有注册 objectName 赋值的消息类型时，客户端收到消息后是否进行存储，0 表示为不存储、 1 表示为存储，默认为 1 存储消息。(可选)
     * @param isCounted         没有注册 objectName 赋值的消息类型时，客户端收到消息后是否进行未读消息计数，0 表示为不计数、 1 表示为计数，默认为 1 计数，未读消息数增加 1。(可选)
     * @param isIncludeSender   发送用户自己是否接收消息，0 表示为不接收，1 表示为接收，默认为 0 不接收，只有在 toUserId 为一个用户 Id 的时候有效。（可选）
     * @param contentAvailable  针对 iOS 平台，对 SDK 处于后台暂停状态时为静默推送，是 iOS7 之后推出的一种推送方式。 允许应用在收到通知后在后台运行一段代码，且能够马上执行，查看详细。1 表示为开启，0 表示为关闭，默认为 0（可
     * @return
     */
    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded")
    @POST("user/getToken.json")
    Observable<JSONObject> singleSend(@Field("fromUserId") String fromUserId,
                                    @Field("toUserId") String toUserId,
                                    @Field("objectName") String objectName,
                                    @Field("content") String content,
                                    @Field("pushContent") String pushContent,
                                    @Field("pushData") String pushData,
                                    @Field("count") String count,
                                    @Field("verifyBlacklist") String verifyBlacklist,
                                    @Field("isPersisted") String isPersisted,
                                    @Field("isCounted") String isCounted,
                                    @Field("isIncludeSender") String isIncludeSender,
                                    @Field("contentAvailable") String contentAvailable);
}
