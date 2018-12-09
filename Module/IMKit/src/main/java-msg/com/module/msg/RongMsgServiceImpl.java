package com.module.msg;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.fastjson.JSONObject;
import com.module.RouterConstants;
import com.module.common.ICallback;

import io.rong.test.fragment.MessageFragment;

@Route(path = RouterConstants.SERVICE_MSG, name = "消息服务")
public class RongMsgServiceImpl implements IMsgService {

    @Override
    public void joinChatRoom(String roomId, ICallback callback) {
        RongMsgManager.getInstance().joinChatRoom(roomId, callback);
    }

    @Override
    public void leaveChatRoom(String roomId) {
        RongMsgManager.getInstance().leaveChatRoom(roomId);
    }

    @Override
    public void sendChatRoomMessage(String roomId, int messageType, JSONObject contentJson, ICallback callback) {
        RongMsgManager.getInstance().sendChatRoomMessage(roomId, messageType, contentJson, callback);
    }

    @Override
    public void sendChatRoomMessage(String roomId, int messageType, byte[] data, ICallback callback) {
        RongMsgManager.getInstance().sendChatRoomMessage(roomId, messageType, data, callback);
    }

    @Override
    public void addMsgProcessor(IPushMsgProcess processor) {
        RongMsgManager.getInstance().addMsgProcessor(processor);
    }

    @Override
    public IMessageFragment getMessageFragment() {
        return new MessageFragment();
    }

    @Override
    public void init(Context context) {

    }
}
