package com.module.msg.custom.relation;

import android.os.Looper;

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
import java.util.logging.Handler;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RelationMsgProcessor {

    public final static String TAG = "RelationMsgProcessor";

    // 0 未处理 1同意 2拒绝
    public static int getHandle(Message msg) {
        String extra = msg.getExtra();
        JSONObject jo = JSON.parseObject(extra);
        if (jo != null) {
            return jo.getIntValue("handle");
        }
        return 0;
    }

    public static void handle(String msgUid, String reqID, boolean agree, String targetId) {
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
                    // 请求成功，构造IM消息
                    sendRelationHandleMsg(targetId, msgUid, agree ? 1 : 2, obj.getData().getString("msgContent"));
                } else if (obj.getErrno() == 8428113) {
                    // 消息过期了
                    U.getToastUtil().showShort(obj.getErrmsg());
                    RongIMClient.getInstance().getMessageByUid(msgUid, new RongIMClient.ResultCallback<Message>() {
                        @Override
                        public void onSuccess(Message message) {
                            if (message != null) {
                                RongContext.getInstance().getEventBus().post(message);
                            }
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                } else {
                    U.getToastUtil().showShort(obj.getErrmsg());
                }
            }
        });
    }

    public static void onReceiveHandleMsg(RelationHandleMsg contentMsg) {
        String msgUid = contentMsg.getMsgUid();
        MyLog.d("RelationMsgProcessor", "process msgId=" + msgUid);

        RongIMClient.getInstance().getMessageByUid(msgUid, new RongIMClient.ResultCallback<Message>() {
            @Override
            public void onSuccess(Message message) {
                MyLog.d("RelationMsgProcessor", "onSuccess message=" + message);
                if (message != null) {
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

    public static void sendRelationInviteMsg(String userID, String uniqID, String content, long expireAt, int deep) {
        if (deep > 10) {
            return;
        }
        RelationInviteMsg contentMsg = RelationInviteMsg.obtain();
        contentMsg.setContent(content);
        contentMsg.setUniqID(uniqID);
        contentMsg.setExpireAt(expireAt);
        Message msg = Message.obtain(userID, Conversation.ConversationType.PRIVATE, contentMsg);

        RongIM.getInstance().sendMessage(msg, content, content, new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                // 发成功后 强制存下数据库 不然再进列表又是空的了
                //RongIM.getInstance().setMessageExtra(message.getMessageId(),message.getExtra());
            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                MyLog.e(TAG, "onError" + " message=" + message + " errorCode=" + errorCode);
                new android.os.Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendRelationInviteMsg(userID, uniqID, content, expireAt, deep + 1);
                    }
                }, 1000 * deep);
            }
        });
    }

    public static void sendRelationHandleMsg(String userID, String msgUid, int handle, String content) {
        RelationHandleMsg contentMsg = RelationHandleMsg.obtain();
        contentMsg.setContent(content);
        contentMsg.setMsgUid(msgUid);
        contentMsg.setHandle(handle);
        Message msg = Message.obtain(userID, Conversation.ConversationType.PRIVATE, contentMsg);

        RongIM.getInstance().sendMessage(msg, content, content, new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                // 发成功后 强制存下数据库 不然再进列表又是空的了
                //RongIM.getInstance().setMessageExtra(message.getMessageId(),message.getExtra());
                onReceiveHandleMsg(contentMsg);
            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                MyLog.e(TAG, "onError" + " message=" + message + " errorCode=" + errorCode);
            }
        });
    }
}
