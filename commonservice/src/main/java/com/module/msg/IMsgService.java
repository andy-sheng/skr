package com.module.msg;

import android.app.Application;
import android.content.Context;
import android.util.Pair;

import com.alibaba.android.arouter.facade.template.IProvider;
import com.alibaba.fastjson.JSONObject;
import com.module.common.ICallback;

public interface IMsgService extends IProvider {

    /**
     * 初始化融云
     */
    void initRongIM(Application application);

    /**
     * 得到当前融云链接状态以及描述
     *
     * @return
     */
    Pair<Integer, String> getConnectStatus();

    /**
     * 与融云服务器建立连接
     *
     * @param token
     * @param callback
     */
    void connectRongIM(String token, ICallback callback);

    void disconnect();

    void logout();

    void addUnReadMessageCountChangedObserver(ICallback callback);

    /**
     * 加入融云聊天室
     *
     * @param roomId
     * @param callback
     */
    void joinChatRoom(String roomId, ICallback callback);

    void leaveChatRoom(String roomId);

    /**
     * 通过融云发送聊天室消息
     *
     * @param roomId
     * @param messageType
     * @param contentJson
     * @param callback
     */
    void sendChatRoomMessage(String roomId, int messageType, JSONObject contentJson, ICallback callback);

    void sendChatRoomMessage(String roomId, int messageType, String content, ICallback callback);

    void syncHistoryFromChatRoom(String roomId, int count, boolean reverse, ICallback callback);

    /**
     * 其他module设置自己的push处理模块
     *
     * @param processor
     */
    void addMsgProcessor(IPushMsgProcess processor);

    IMessageFragment getMessageFragment();

    /**
     * 打开私聊页面
     *
     * @param context
     * @param targetId
     * @param title
     */
    void startPrivateChat(Context context, String targetId, String title);

    /**
     * 在融云服务器上更新当前用户信息
     */
    void updateCurrentUserInfo();

    /**
     * 更新融云缓存中个人信息
     *
     * @param userId
     * @param nickName
     * @param avatar
     */
    void refreshUserInfoCache(int userId, String nickName, String avatar);


    /**
     * 融云聊天黑名单
     *
     * @param userId
     * @param callback
     */
    void addToBlacklist(String userId, ICallback callback);

    void removeFromBlacklist(String userId, ICallback callback);

    void getBlacklist(ICallback callback);

    void getBlacklistStatus(String userId, ICallback callback);
}
