package com.module.msg.custom.relation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.msg.api.IMsgServerApi;

import java.util.HashMap;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RelationMsgProcessor {

    // 0 未处理 1同意 2拒绝
    public static int getHandle(Message msg) {
        String extra = msg.getExtra();
        JSONObject jo = JSON.parseObject(extra);
        if (jo != null) {
            return jo.getIntValue("handle");
        }
        return 0;
    }

    public static void process(RelationHandleMsg contentMsg) {
        String msgUid = contentMsg.getMsgUid();
        MyLog.d("RelationMsgProcessor", "process msgId=" + msgUid);

        RongIMClient.getInstance().getMessageByUid(msgUid, new RongIMClient.ResultCallback<Message>() {
            @Override
            public void onSuccess(Message message) {
                if(message!=null){
                    JSONObject jo = JSON.parseObject(message.getExtra());
                    if (jo == null) {
                        jo = new JSONObject();
                    }
                    jo.put("handle", contentMsg.getHandle());
                    message.setExtra(jo.toJSONString());
                    RongIM.getInstance().setMessageExtra(message.getMessageId(), message.getExtra());
                    RongContext.getInstance().getEventBus().post(message);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }

    public static void agree(String msgUid, String reqID) {
        handle(msgUid, reqID, true);
    }

    public static void reject(String msgUid, String reqID) {
        handle(msgUid, reqID, false);
    }

    private static void handle(String msgUid, String reqID, boolean agree) {
        IMsgServerApi iMsgServerApi = ApiManager.getInstance().createService(IMsgServerApi.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("msgUid", msgUid);
        map.put("reqID", reqID);
        map.put("responseType", agree ? 1 : 2);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(iMsgServerApi.relationApplyResponse(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    // 请求成功，先看消息会不会及时更新
                } else {
                    U.getToastUtil().showShort(obj.getErrmsg());
                }
            }
        });
    }

}
