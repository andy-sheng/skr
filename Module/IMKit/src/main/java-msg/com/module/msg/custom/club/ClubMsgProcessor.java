package com.module.msg.custom.club;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

public class ClubMsgProcessor {

    // 0 未处理 1同意 2拒绝
    public static int getHandle(Message msg){
        String extra = msg.getExtra();
        JSONObject jo = JSON.parseObject(extra);
        if(jo!=null){
            return jo.getIntValue("handle");
        }
        return 0;
    }

    public static void process(ClubHandleMsg clubAgreeMsg) {
        String msgUid = clubAgreeMsg.getMsgUid();
        MyLog.d("ClubMsgProcessor","process clubAgreeMsgId=" + msgUid);

        RongIMClient.getInstance().getMessageByUid(msgUid, new RongIMClient.ResultCallback<Message>() {
            @Override
            public void onSuccess(Message message) {
                JSONObject jo = JSON.parseObject(message.getExtra());
                if(jo==null){
                    jo = new JSONObject();
                }
                jo.put("handle",clubAgreeMsg.getHandle());
                message.setExtra(jo.toJSONString());
                RongIM.getInstance().setMessageExtra(message.getMessageId(), message.getExtra());
                RongContext.getInstance().getEventBus().post(message);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }
}
