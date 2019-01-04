package com.module.msg;

import android.app.Application;
import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.fastjson.JSONObject;
import com.module.RouterConstants;
import com.module.common.ICallback;

import com.module.msg.fragment.MessageFragment;

@Route(path = RouterConstants.SERVICE_MSG, name = "消息服务")
public class RongMsgServiceImpl implements IMsgService {

    @Override
    public void initRongIM(Application application) {
        RongMsgManager.getInstance().init(application);
    }

    @Override
    public void connectRongIM(String token, ICallback callback) {
        RongMsgManager.getInstance().connectRongIM(token, callback);
    }

    @Override
    public void disconnect() {
        RongMsgManager.getInstance().disconnect();
    }

    @Override
    public void logout() {
        RongMsgManager.getInstance().logout();
    }

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
    public void addMsgProcessor(IPushMsgProcess processor) {
        RongMsgManager.getInstance().addMsgProcessor(processor);
    }

    @Override
    public IMessageFragment getMessageFragment() {
        return new MessageFragment();
    }

    @Override
    public void startPrivateChat(Context context, String targetId, String title) {
        RongMsgManager.getInstance().startPrivateChat(context, targetId, title);
    }


    @Override
    public void init(Context context) {

    }
}
