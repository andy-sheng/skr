package com.module.msg;

import android.app.Application;
import android.content.Context;
import android.util.Pair;

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
    public Pair<Integer, String> getConnectStatus() {
        return RongMsgManager.getInstance().getConnectStatus();
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
    public void addUnReadMessageCountChangedObserver(ICallback callback) {
        RongMsgManager.getInstance().addUnReadMessageCountChangedObserver(callback);
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
    public void refreshUserInfoCache(int userId, String nickName, String avatar) {
        RongMsgManager.getInstance().refreshUserInfoCache(userId, nickName, avatar);
    }

    @Override
    public void sendChatRoomMessage(String roomId, int messageType, JSONObject contentJson, ICallback callback) {
        RongMsgManager.getInstance().sendChatRoomMessage(roomId, messageType, contentJson, callback);
    }

    @Override
    public void sendChatRoomMessage(String roomId, int messageType, String content, ICallback callback) {
        RongMsgManager.getInstance().sendChatRoomMessage(roomId, messageType, content, callback);
    }

    @Override
    public void syncHistoryFromChatRoom(String roomId, int count, boolean reverse, ICallback callback) {
        RongMsgManager.getInstance().syncHistoryFromChatRoom(roomId, count, reverse, callback);
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
    public void updateCurrentUserInfo() {
        RongMsgManager.getInstance().updateCurrentUserInfo();
    }

    @Override
    public void addToBlacklist(String userId, ICallback callback) {
        RongMsgManager.getInstance().addToBlacklist(userId, callback);
    }

    @Override
    public void removeFromBlacklist(String userId, ICallback callback) {
        RongMsgManager.getInstance().removeFromBlacklist(userId, callback);
    }

    @Override
    public void getBlacklist(ICallback callback) {
        RongMsgManager.getInstance().getBlacklist(callback);
    }

    @Override
    public void getBlacklistStatus(String userId, ICallback callback) {
        RongMsgManager.getInstance().getBlacklistStatus(userId, callback);
    }

    @Override
    public void init(Context context) {

    }
}
