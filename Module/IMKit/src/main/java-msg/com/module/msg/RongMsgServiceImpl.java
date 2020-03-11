package com.module.msg;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.fastjson.JSONObject;
import com.module.RouterConstants;
import com.module.common.ICallback;
import com.module.msg.custom.club.ClubInviteMsg;
import com.module.msg.custom.club.ClubMsgProcessor;
import com.module.msg.custom.relation.RelationInviteMsg;
import com.module.msg.custom.relation.RelationMsgProcessor;
import com.module.msg.fragment.MessageFragment2;

import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

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
    public void removeUnReadMessageCountChangedObserver(ICallback callback) {
        RongMsgManager.getInstance().removeUnReadMessageCountChangedObserver(callback);
    }

    @Override
    public void joinChatRoom(String roomId, int defMessageCount, ICallback callback) {
        RongMsgManager.getInstance().joinChatRoom(roomId, defMessageCount, callback);
    }

    @Override
    public void leaveChatRoom(String roomId) {
        RongMsgManager.getInstance().leaveChatRoom(roomId);
    }

    @Override
    public void refreshUserInfoCache(int userId, String nickName, String avatar, String extra) {
        RongMsgManager.getInstance().refreshUserInfoCache(userId, nickName, avatar, extra);
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
    public void sendSpecialDebugMessage(String targetId, int messageType, String content, ICallback callback) {
        RongMsgManager.getInstance().sendSpecialDebugMessage(targetId, messageType, content, callback);
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
        return new MessageFragment2();
    }

    @Override
    public boolean startPrivateChat(Context context, String targetId, String title, boolean isFriend) {
        return RongMsgManager.getInstance().startPrivateChat(context, targetId, title, isFriend);
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
    public void sendRelationInviteMsg(String userID, String uniqID,String content,long expireAt) {
        RelationMsgProcessor.sendRelationInviteMsg(userID,uniqID,content,expireAt);
    }

    @Override
    public void sendClubInviteMsg(String userID, String uniqID, long expireAt, String content) {
        ClubMsgProcessor.sendClubInviteMsg(userID,uniqID,expireAt,content);
    }

    @Override
    public void sendTxtMsg(String userID, String content) {
        TextMessage contentMsg =  TextMessage.obtain(content);
        Message msg = Message.obtain(userID, Conversation.ConversationType.PRIVATE, contentMsg);

        RongIM.getInstance().sendMessage(msg, content, "pushData"+content, new IRongCallback.ISendMessageCallback() {
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
            }
        });
    }

    @Override
    public void init(Context context) {

    }
}
