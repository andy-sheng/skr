package com.module.msg;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.module.RouterConstants;
import com.module.common.ICallback;

@Route(path = RouterConstants.SERVICE_MSG, name = "消息服务")
public class MsgServiceImpl implements IMsgService {

    @Override
    public void joinChatRoom(String roomId, ICallback callback) {
        RongMsgAdapter.getInstance().joinChatRoom(roomId, callback);
    }

    @Override
    public void leaveChatRoom(String roomId) {
        RongMsgAdapter.getInstance().leaveChatRoom(roomId);
    }

    @Override
    public void init(Context context) {

    }
}
