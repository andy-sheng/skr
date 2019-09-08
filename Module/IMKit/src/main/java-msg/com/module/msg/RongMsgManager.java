package com.module.msg;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Pair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.ResultCallback;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.cache.BuddyCache;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.jiguang.JiGuangPush;
import com.common.log.MyLog;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.LogUploadUtils;
import com.common.utils.U;
import com.module.common.ICallback;
import com.module.msg.activity.ConversationActivity;
import com.module.msg.custom.MyPrivateConversationProvider;
import com.module.msg.listener.MyConversationClickListener;
import com.module.msg.model.BroadcastRoomMsg;
import com.module.msg.model.CustomChatCombineRoomLowLevelMsg;
import com.module.msg.model.CustomChatCombineRoomMsg;
import com.module.msg.model.CustomChatRoomLowLevelMsg;
import com.module.msg.model.CustomChatRoomMsg;
import com.module.msg.model.CustomNotificationMsg;
import com.module.msg.model.RaceRoomHighMsg;
import com.module.msg.model.RaceRoomLowMsg;
import com.module.msg.model.SpecailOpMsg;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;
import io.rong.imkit.manager.IUnReadMessageObserver;
import io.rong.imkit.widget.provider.PrivateConversationProvider;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.push.RongPushClient;
import io.rong.push.pushconfig.PushConfig;

import static com.module.msg.CustomMsgType.MSG_TYPE_BROADCAST;
import static com.module.msg.CustomMsgType.MSG_TYPE_COMBINE_ROOM;
import static com.module.msg.CustomMsgType.MSG_TYPE_NOTIFICATION;
import static com.module.msg.CustomMsgType.MSG_TYPE_RACE_ROOM;
import static com.module.msg.CustomMsgType.MSG_TYPE_ROOM;

public class RongMsgManager implements RongIM.UserInfoProvider {

    public static final int MSG_RECONNECT = 11;

    public final String TAG = "RongMsgManager";

    private static class RongMsgAdapterHolder {
        private static final RongMsgManager INSTANCE = new RongMsgManager();
    }

    private RongMsgManager() {

    }

    public static final RongMsgManager getInstance() {
        return RongMsgAdapterHolder.INSTANCE;
    }


    Handler mUiHanlder = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_RECONNECT) {
                // 重连
                mUiHanlder.removeMessages(MSG_RECONNECT);
                UserAccountManager.getInstance().tryConnectRongIM(true);
            }
        }
    };

    private RongIMClient.OperationCallback mOperationCallback = new RongIMClient.OperationCallback() {
        @Override
        public void onSuccess() {
            if (mOneTimeJoinroomCallback != null) {
                MyLog.d(TAG, "join rc room onSuccess");
                StatisticsAdapter.recordCountEvent("rc", "join_room_success", null);
                mOneTimeJoinroomCallback.onSucess(null);
                mOneTimeJoinroomCallback = null;
            }
        }

        @Override
        public void onError(RongIMClient.ErrorCode errorCode) {
            if (mOneTimeJoinroomCallback != null) {
                MyLog.d(TAG, "join rc room error,code:" + errorCode);
                StatisticsAdapter.recordCountEvent("rc", "join_room_failed", null);
                mOneTimeJoinroomCallback.onFailed(null, errorCode.getValue(), errorCode.getMessage());
                mOneTimeJoinroomCallback = null;
            }
        }
    };

    private ICallback mOneTimeJoinroomCallback;

    /**
     * 消息类型-->消息处理器的映射
     */
    private HashMap<Integer, HashSet<IPushMsgProcess>> mProcessorMap = new HashMap<>();

    RongIMClient.OnReceiveMessageListener mReceiveMessageListener = new RongIMClient.OnReceiveMessageListener() {

        /**
         * 收到消息的处理。
         *
         * @param message 收到的消息实体。
         * @param left    剩余未拉取消息数目。
         * @return 收到消息是否处理完成，true 表示自己处理铃声和后台通知，false 走融云默认处理方式。
         */
        @Override
        public boolean onReceived(Message message, int left) {
            MyLog.d(TAG, "onReceived" + " message=" + message + " left=" + left);
            if (message == null) {
                return false;
            }

            // 针对融云的系统消息增加打点，统计下
            if (message.getConversationType() == Conversation.ConversationType.SYSTEM) {
                long delay = message.getReceivedTime() - message.getSentTime();
                if (delay > 0) {
                    StatisticsAdapter.recordCalculateEvent("rc", "system_msg_delay", delay, null);
                }
            }

            if (message.getContent() instanceof CustomChatRoomMsg) {
                // 是自定义消息 其content即整个RoomMsg
                CustomChatRoomMsg customChatRoomMsg = (CustomChatRoomMsg) message.getContent();
                dispatchCustomRoomMsg(customChatRoomMsg);

                return true;
            } else if (message.getContent() instanceof CustomChatRoomLowLevelMsg) {

                CustomChatRoomLowLevelMsg customChatRoomMsg = (CustomChatRoomLowLevelMsg) message.getContent();
                dispatchCustomRoomMsg(customChatRoomMsg);

                return true;
            } else if (message.getContent() instanceof CustomChatCombineRoomMsg) {

                CustomChatCombineRoomMsg customChatRoomMsg = (CustomChatCombineRoomMsg) message.getContent();
                dispatchCustomCombineRoomMsg(customChatRoomMsg);

                return true;
            } else if (message.getContent() instanceof CustomChatCombineRoomLowLevelMsg) {

                CustomChatCombineRoomLowLevelMsg customChatRoomMsg = (CustomChatCombineRoomLowLevelMsg) message.getContent();
                dispatchCustomCombineRoomMsg(customChatRoomMsg);

                return true;
            } else if (message.getContent() instanceof RaceRoomHighMsg) {

                RaceRoomHighMsg customChatRoomMsg = (RaceRoomHighMsg) message.getContent();
                dispatchRaceRoomMsg(customChatRoomMsg);

                return true;
            } else if (message.getContent() instanceof RaceRoomLowMsg) {

                RaceRoomHighMsg customChatRoomMsg = (RaceRoomHighMsg) message.getContent();
                dispatchRaceRoomMsg(customChatRoomMsg);

                return true;
            } else if (message.getContent() instanceof CustomNotificationMsg) {
                CustomNotificationMsg notificationMsg = (CustomNotificationMsg) message.getContent();
                byte[] data = U.getBase64Utils().decode(notificationMsg.getContentJsonStr());

                HashSet<IPushMsgProcess> processors = mProcessorMap.get(MSG_TYPE_NOTIFICATION);
                if (processors != null) {
                    for (IPushMsgProcess process : processors) {
                        process.process(MSG_TYPE_NOTIFICATION, data);
                    }
                }

                return true;
            } else if(message.getContent() instanceof BroadcastRoomMsg){
                BroadcastRoomMsg notificationMsg = (BroadcastRoomMsg) message.getContent();
                byte[] data = U.getBase64Utils().decode(notificationMsg.getContentJsonStr());
                HashSet<IPushMsgProcess> processors = mProcessorMap.get(MSG_TYPE_BROADCAST);
                if (processors != null) {
                    for (IPushMsgProcess process : processors) {
                        process.process(MSG_TYPE_BROADCAST, data);
                    }
                }
                return true;
            }else if (message.getContent() instanceof SpecailOpMsg) {
                /**
                 * 要求别人上传日志，并将结果返回
                 */
                SpecailOpMsg specailOpMsg = (SpecailOpMsg) message.getContent();
                if (specailOpMsg.getMessageType() == 1) {
                    U.getLogUploadUtils().upload(MyUserInfoManager.getInstance().getUid(), new LogUploadUtils.Callback() {
                        @Override
                        public void onSuccess(String url) {
                            //上传日志成功
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("uploaderId", MyUserInfoManager.getInstance().getUid());
                            jsonObject.put("uploaderName", MyUserInfoManager.getInstance().getNickName());
                            jsonObject.put("uploaderAvatar", MyUserInfoManager.getInstance().getAvatar());
                            jsonObject.put("url", url);
                            jsonObject.put("date", U.getDateTimeUtils().formatDetailTimeStringNow());

                            StringBuilder sb = new StringBuilder();
                            sb.append(" version:").append(U.getAppInfoUtils().getVersionName())
                                    .append(" 渠道号:").append(U.getChannelUtils().getChannel());
                            sb.append(" 手机型号:").append(U.getDeviceUtils().getProductModel());
                            sb.append(" 手机厂商:").append(U.getDeviceUtils().getProductBrand());
                            jsonObject.put("extra", sb.toString());
                            MyLog.d(TAG, "上传日志 " + jsonObject.toJSONString());
                            sendSpecialDebugMessage(message.getSenderUserId(), 2, jsonObject.toJSONString(), null);
                        }

                        @Override
                        public void onFailed() {

                        }
                    }, false);
                } else if (specailOpMsg.getMessageType() == 2) {
                    JSONObject jsonObject = JSON.parseObject(specailOpMsg.getContentJsonStr());
                    LogUploadUtils.RequestOthersUploadLogSuccess event = new LogUploadUtils.RequestOthersUploadLogSuccess();
                    event.uploaderId = jsonObject.getString("uploaderId");
                    event.uploaderName = jsonObject.getString("uploaderName");
                    event.uploaderAvatar = jsonObject.getString("uploaderAvatar");
                    event.mLogUrl = jsonObject.getString("url");
                    event.date = jsonObject.getString("date");
                    event.extra = jsonObject.getString("extra");
                    EventBus.getDefault().post(event);
                }
            }
            // TODO: 2019/5/19  收到消息是否处理完成，true 表示自己处理铃声和后台通知，false 走融云默认处理方式。
            if (U.getActivityUtils().isAppForeground()) {
                return true;
            } else {
                return false;
            }
        }
    };

    private void dispatchCustomRoomMsg(MessageContent messageContent) {
        if (messageContent instanceof CustomChatRoomMsg) {
            CustomChatRoomMsg customChatRoomMsg = (CustomChatRoomMsg) messageContent;
            byte[] data = U.getBase64Utils().decode(customChatRoomMsg.getContentJsonStr());

            HashSet<IPushMsgProcess> processors = mProcessorMap.get(MSG_TYPE_ROOM);
            if (processors != null) {
                for (IPushMsgProcess process : processors) {
                    process.process(MSG_TYPE_ROOM, data);
                }
            }
        } else if (messageContent instanceof CustomChatRoomLowLevelMsg) {
            CustomChatRoomLowLevelMsg customChatRoomMsg = (CustomChatRoomLowLevelMsg) messageContent;
            byte[] data = U.getBase64Utils().decode(customChatRoomMsg.getContentJsonStr());

            HashSet<IPushMsgProcess> processors = mProcessorMap.get(MSG_TYPE_ROOM);
            if (processors != null) {
                for (IPushMsgProcess process : processors) {
                    process.process(MSG_TYPE_ROOM, data);
                }
            }
        }
    }

    private void dispatchCustomCombineRoomMsg(MessageContent messageContent) {
        if (messageContent instanceof CustomChatCombineRoomMsg) {
            CustomChatCombineRoomMsg customChatRoomMsg = (CustomChatCombineRoomMsg) messageContent;
            byte[] data = U.getBase64Utils().decode(customChatRoomMsg.getContentJsonStr());

            HashSet<IPushMsgProcess> processors = mProcessorMap.get(MSG_TYPE_COMBINE_ROOM);
            if (processors != null) {
                for (IPushMsgProcess process : processors) {
                    process.process(MSG_TYPE_COMBINE_ROOM, data);
                }
            }
        } else if (messageContent instanceof CustomChatCombineRoomLowLevelMsg) {
            CustomChatCombineRoomLowLevelMsg customChatRoomMsg = (CustomChatCombineRoomLowLevelMsg) messageContent;
            byte[] data = U.getBase64Utils().decode(customChatRoomMsg.getContentJsonStr());

            HashSet<IPushMsgProcess> processors = mProcessorMap.get(MSG_TYPE_COMBINE_ROOM);
            if (processors != null) {
                for (IPushMsgProcess process : processors) {
                    process.process(MSG_TYPE_COMBINE_ROOM, data);
                }
            }
        }
    }

    private void dispatchRaceRoomMsg(MessageContent messageContent) {
        if (messageContent instanceof RaceRoomHighMsg) {
            RaceRoomHighMsg msg = (RaceRoomHighMsg) messageContent;
            byte[] data = U.getBase64Utils().decode(msg.getContentJsonStr());

            HashSet<IPushMsgProcess> processors = mProcessorMap.get(MSG_TYPE_RACE_ROOM);
            if (processors != null) {
                for (IPushMsgProcess process : processors) {
                    process.process(MSG_TYPE_RACE_ROOM, data);
                }
            }
        } else if (messageContent instanceof RaceRoomLowMsg) {
            RaceRoomLowMsg msg = (RaceRoomLowMsg) messageContent;
            byte[] data = U.getBase64Utils().decode(msg.getContentJsonStr());

            HashSet<IPushMsgProcess> processors = mProcessorMap.get(MSG_TYPE_RACE_ROOM);
            if (processors != null) {
                for (IPushMsgProcess process : processors) {
                    process.process(MSG_TYPE_RACE_ROOM, data);
                }
            }
        }
    }

    // 是否初始化
    private boolean mIsInit = false;

    private RongIMClient.ConnectionStatusListener.ConnectionStatus mConnectionStatus = RongIMClient.ConnectionStatusListener.ConnectionStatus.DISCONNECTED;

    public void init(Application application) {
        if (!mIsInit) {
            // 生产环境
            String rongKey;
            if (U.getChannelUtils().isStaging()) {
                rongKey = "0vnjpoad03v7z";
            } else {
                rongKey = "e5t4ouvpec57a";
            }
            RongIM.getInstance().disconnect();

            PushConfig config = new PushConfig.Builder()
                    .enableHWPush(true)  // 配置华为推送
                    .enableMiPush("2882303761517932750", "5701793259750")
                    .enableOppoPush("b6476d0350bf448da381e589e232add8", "1b038723163d485b9d739ad9ee0fbe8e")
                    .enableVivoPush(true)
                    .build();
            RongPushClient.setPushConfig(config);
            //RongPushClient.init(U.app(),rongKey);

            RongIM.init(application, rongKey);
            RongIM.setUserInfoProvider(this, true);
            mIsInit = true;
            RongIM.registerMessageType(CustomChatRoomMsg.class);
            RongIM.registerMessageType(CustomChatRoomLowLevelMsg.class);
            RongIM.registerMessageType(CustomChatCombineRoomMsg.class);
            RongIM.registerMessageType(CustomChatCombineRoomLowLevelMsg.class);
            RongIM.registerMessageType(CustomNotificationMsg.class);
            RongIM.registerMessageType(RaceRoomHighMsg.class);
            RongIM.registerMessageType(RaceRoomLowMsg.class);
            RongIM.registerMessageType(BroadcastRoomMsg.class);
            RongIM.registerMessageType(SpecailOpMsg.class);

            RongIM.getInstance().registerConversationTemplate(new MyPrivateConversationProvider());

            RongIM.getInstance().setConversationClickListener(new MyConversationClickListener());
            RongIM.setConnectionStatusListener(new RongIMClient.ConnectionStatusListener() {
                @Override
                public void onChanged(ConnectionStatus connectionStatus) {
                    MyLog.w(TAG, "onChanged" + " connectionStatus=" + connectionStatus);
                    mConnectionStatus = connectionStatus;
                    switch (connectionStatus) {
                        case CONNECTED://连接成功。

                            break;
                        case DISCONNECTED://断开连接。
                            MyLog.d(TAG, "融云链接为断开状态，15s后尝试重连");
                            mUiHanlder.sendEmptyMessageDelayed(MSG_RECONNECT, 15 * 1000);
                            break;
                        case CONNECTING://连接中。

                            break;
                        case NETWORK_UNAVAILABLE://网络不可用。
                            MyLog.d(TAG, "融云链接为网络不可用，15s后尝试重连");
                            mUiHanlder.sendEmptyMessageDelayed(MSG_RECONNECT, 15 * 1000);
                            break;
                        case KICKED_OFFLINE_BY_OTHER_CLIENT://用户账户在其他设备登录，本机会被踢掉线
                            UserAccountManager.getInstance().rcKickedByOthers(0);
                            break;
                    }
                }
            });
            RongIM.setOnReceiveMessageListener(mReceiveMessageListener);
            /**
             * 融云服务不可用时，用极光来补一下
             */
            JiGuangPush.setCustomMsgListener(new JiGuangPush.JPushCustomMsgListener() {
                @Override
                public void onReceive(String contentType, byte[] data) {
                    if ("SKR:CustomMsg".equals(contentType) || "SKR:CustomMsgLow".equals(contentType)) {
                        HashSet<IPushMsgProcess> processors = mProcessorMap.get(MSG_TYPE_ROOM);
                        if (processors != null) {
                            for (IPushMsgProcess process : processors) {
                                process.process(MSG_TYPE_ROOM, data);
                            }
                        }
                    } else if ("SKR:NotificationMsg".equals(contentType)) {
                        HashSet<IPushMsgProcess> processors = mProcessorMap.get(MSG_TYPE_NOTIFICATION);
                        if (processors != null) {
                            for (IPushMsgProcess process : processors) {
                                process.process(MSG_TYPE_NOTIFICATION, data);
                            }
                        }
                    } else if ("SKR:CombineRoomMsgHigh".equals(contentType) || "SKR:CombineRoomMsgLow".equals(contentType)) {
                        HashSet<IPushMsgProcess> processors = mProcessorMap.get(MSG_TYPE_COMBINE_ROOM);
                        if (processors != null) {
                            for (IPushMsgProcess process : processors) {
                                process.process(MSG_TYPE_COMBINE_ROOM, data);
                            }
                        }
                    }
                }
            });
            setInputProvider();
        }
    }

    private void setInputProvider() {
        List<IExtensionModule> moduleList = RongExtensionManager.getInstance().getExtensionModules();
        IExtensionModule defaultModule = null;
        if (moduleList != null) {
            for (IExtensionModule module : moduleList) {
                if (module instanceof DefaultExtensionModule) {
                    defaultModule = module;
                    break;
                }
            }
            if (defaultModule != null) {
                RongExtensionManager.getInstance().unregisterExtensionModule(defaultModule);
                RongExtensionManager.getInstance().registerExtensionModule(new MyExtensionModule());
            }
        }
    }

    @Override
    public UserInfo getUserInfo(String useId) {
        MyLog.d(TAG, "getUserInfo" + " useId = " + useId);
        if (MyUserInfoManager.getInstance().getUid() == Integer.valueOf(useId)) {
            UserInfo userInfo = toRongUserInfo(MyUserInfo.toUserInfoModel(MyUserInfoManager.getInstance().getMyUserInfo()));
            RongIM.getInstance().refreshUserInfoCache(userInfo);
            return userInfo;
        }


        BuddyCache.BuddyCacheEntry buddyCacheEntry = BuddyCache.getInstance().getBuddyNormal(Integer.valueOf(useId), true, new ResultCallback<UserInfoModel>() {
            @Override
            public boolean onGetLocalDB(UserInfoModel userInfoModel) {
                if (userInfoModel != null) {
                    RongIM.getInstance().refreshUserInfoCache(toRongUserInfo(userInfoModel));
                }
                return false;
            }

            @Override
            public boolean onGetServer(UserInfoModel userInfoModel) {
                if (userInfoModel != null) {
                    RongIM.getInstance().refreshUserInfoCache(toRongUserInfo(userInfoModel));
                }
                return false;
            }
        });

        if (buddyCacheEntry != null) {
            return new UserInfo(String.valueOf(buddyCacheEntry.getUuid()), UserInfoManager.getInstance().getRemarkName(buddyCacheEntry.getUuid(), buddyCacheEntry.getName()), Uri.parse(buddyCacheEntry.getAvatar()));
        } else {
            // TODO: 2019/4/16 此时靠 RongIM.getInstance().refreshUserInfoCache去更新
            return null;
        }
    }

    private UserInfo toRongUserInfo(UserInfoModel userInfoModel) {
        UserInfo userInfo = new UserInfo(String.valueOf(userInfoModel.getUserId()), userInfoModel.getNicknameRemark(), Uri.parse(userInfoModel.getAvatar()));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("vipInfo", userInfoModel.getVipInfo());
        userInfo.setExtra(jsonObject.toJSONString());
        return userInfo;
    }

    public synchronized void addMsgProcessor(IPushMsgProcess processor) {
        MyLog.d(TAG, "addMsgProcessor" + " processor=" + processor);
        for (int type : processor.acceptType()) {
            HashSet<IPushMsgProcess> processorSet = mProcessorMap.get(type);
            if (processorSet == null) {
                processorSet = new HashSet<>();
                mProcessorMap.put(type, processorSet);
            }
            processorSet.add(processor);
        }
    }

    public void connectRongIM(String token, ICallback callback) {
        RongIM.connect(token, new RongIMClient.ConnectCallback() {
            /**
             * 连接融云成功
             * @param userid 当前 token 对应的用户 id
             */
            @Override
            public void onSuccess(String userid) {
                MyLog.w(TAG, "ConnectCallback connect Success userid is " + userid);
                if (callback != null) {
                    callback.onSucess(userid);
                }
            }

            /**x
             * 连接融云失败
             * @param errorCode 错误码，可到官网 查看错误码对应的注释
             *                  https://www.rongcloud.cn/docs/status_code.html#android_ios_code
             */
            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                MyLog.w(TAG, "ConnectCallback " + "onError errorMsg=" + errorCode.getMessage() + " code=" + errorCode.getValue());
                if (callback != null) {
                    callback.onFailed(true, errorCode.getValue(), errorCode.getMessage());
                }
            }

            /**Token 错误。可以从下面两点检查
             * 1.  Token 是否过期，如果过期您需要向 App Server 重新请求一个新的 Token
             * 2.  token 对应的 appKey 和工程里设置的 appKey 是否一致
             */
            @Override
            public void onTokenIncorrect() {
                MyLog.w(TAG, "ConnectCallback connect onTokenIncorrect");
                if (callback != null) {
                    callback.onFailed(false, 0, "");
                }
            }
        });
    }

    HashSet<ICallback> mUnreadCallbacks = new HashSet<>();

    IUnReadMessageObserver mIUnReadMessageObserver = new IUnReadMessageObserver() {
        @Override
        public void onCountChanged(int unReadNum) {
            MyLog.d(TAG, "onCountChanged" + " unReadNum=" + unReadNum);
            for (ICallback callback : mUnreadCallbacks) {
                callback.onSucess(unReadNum);
            }
        }
    };

    public void addUnReadMessageCountChangedObserver(ICallback callback) {
        mUnreadCallbacks.add(callback);
        RongIM.getInstance().addUnReadMessageCountChangedObserver(mIUnReadMessageObserver, Conversation.ConversationType.PRIVATE);
    }

    public void removeUnReadMessageCountChangedObserver(ICallback callback) {
        mUnreadCallbacks.remove(callback);
        RongIM.getInstance().removeUnReadMessageCountChangedObserver(mIUnReadMessageObserver);
    }

    public Pair<Integer, String> getConnectStatus() {
        return new Pair<Integer, String>(mConnectionStatus.getValue(), mConnectionStatus.getMessage());
    }

    /**
     * <p>断开与融云服务器的连接。当调用此接口断开连接后，仍然可以接收 Push 消息。</p>
     * <p>若想断开连接后不接受 Push 消息，可以调用{@link #logout()}</p>
     */
    public void disconnect() {
        RongIM.getInstance().disconnect();
    }

    /**
     * <p>断开与融云服务器的连接，并且不再接收 Push 消息。</p>
     * <p>若想断开连接后仍然接受 Push 消息，可以调用 {@link #disconnect()}</p>
     */
    public void logout() {
        RongIM.getInstance().logout();
    }

    /**
     * 加入聊天室。
     * <p>如果聊天室不存在，sdk 会创建聊天室并加入，如果已存在，则直接加入</p>
     * <p>加入聊天室时，可以选择拉取聊天室消息数目。</p>
     *
     * @param defMessageCount 进入聊天室拉取消息数目，-1 时不拉取任何消息，0 时拉取 10 条消息，最多只能拉取 50 条。
     * @param callback        状态回调。
     * @param roomId
     */
    public void joinChatRoom(String roomId, int defMessageCount, ICallback callback) {
        mOneTimeJoinroomCallback = callback;
        /**
         * 不拉之前的消息
         */
        RongIM.getInstance().joinChatRoom(roomId, defMessageCount, mOperationCallback);
    }

    public void refreshUserInfoCache(int userId, String nickName, String avatar, String extra) {
        UserInfo userInfo = new UserInfo(String.valueOf(userId), nickName, Uri.parse(avatar));
        userInfo.setExtra(extra);
        RongIM.getInstance().refreshUserInfoCache(userInfo);
    }

    public void leaveChatRoom(String roomId) {
        MyLog.d(TAG, "leaveChatRoom" + " roomId=" + roomId);
        mOneTimeJoinroomCallback = null;
        RongIM.getInstance().quitChatRoom(roomId, mOperationCallback);
    }

    public void sendChatRoomMessage(String roomId, int messageType, JSONObject contentJson, ICallback callback) {
        sendChatRoomMessage(roomId, messageType, contentJson.toJSONString(), callback);
    }

    public void sendChatRoomMessage(String roomId, int messageType, String content, ICallback callback) {
        CustomChatRoomMsg customChatRoomMsg = new CustomChatRoomMsg();
        customChatRoomMsg.setMessageType(messageType);
        customChatRoomMsg.setContentJsonStr(content);
        Message msg = Message.obtain(roomId, Conversation.ConversationType.CHATROOM, customChatRoomMsg);
        RongIM.getInstance().sendMessage(msg, "pushContent", "pushData", new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                if (callback != null) {
                    callback.onSucess(message);
                }
            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                MyLog.d(TAG, "send msg onError errorCode=" + errorCode);
                if (callback != null) {
                    callback.onFailed(message, errorCode.getValue(), errorCode.getMessage());
                }
            }
        });
    }

    public void sendSpecialDebugMessage(String targetId, int messageType, String content, ICallback callback) {
        SpecailOpMsg customChatRoomMsg = new SpecailOpMsg();
        customChatRoomMsg.setMessageType(messageType);
        customChatRoomMsg.setContentJsonStr(content);
        Message msg = Message.obtain(targetId, Conversation.ConversationType.NONE, customChatRoomMsg);
        RongIM.getInstance().sendMessage(msg, "pushContent", "pushData", new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                if (callback != null) {
                    callback.onSucess(message);
                }
            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                MyLog.d(TAG, "send msg onError errorCode=" + errorCode);
                if (callback != null) {
                    callback.onFailed(message, errorCode.getValue(), errorCode.getMessage());
                }
            }
        });
    }


    public void syncHistoryFromChatRoom(String roomId, int count, boolean reverse, ICallback callback) {
        RongIM.getInstance().getHistoryMessages(Conversation.ConversationType.CHATROOM, roomId, Integer.MAX_VALUE, count, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (reverse) {
                    for (int i = messages.size() - 1; i >= 0; i--) {
                        Message message = messages.get(i);
                        mReceiveMessageListener.onReceived(message, 0);
                        if (callback != null) {
                            callback.onSucess(message);
                        }
                    }
                } else {
                    for (Message message : messages) {
                        mReceiveMessageListener.onReceived(message, 0);
                        if (callback != null) {
                            callback.onSucess(message);
                        }
                    }
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                if (callback != null) {
                    callback.onFailed(errorCode, errorCode.getValue(), errorCode.getMessage());
                }
            }
        });
    }

    public boolean startPrivateChat(Context context, String targetUserId, String title, boolean isFriend) {
        if (context != null && !TextUtils.isEmpty(targetUserId)) {
            if (RongContext.getInstance() == null) {
                throw new ExceptionInInitializerError("RongCloud SDK not init");
            } else {
                for (Activity activity : U.getActivityUtils().getActivityList()) {
                    if (activity instanceof ConversationActivity) {
                        // 已经有会话页面了
                        ConversationActivity conversationActivity = (ConversationActivity) activity;
                        if (targetUserId.equals(conversationActivity.getUserId())) {
                            // 正好期望会话的人，已经有一个与这个人的会话Activity存在了
                            return true;
                        } else {
                            // 有一个会话，但是不是与当前人的，强制finish调
                            conversationActivity.finish();
                        }
                        break;
                    }
                }
                Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon()
                        .appendPath("conversation").appendPath(Conversation.ConversationType.PRIVATE.getName().toLowerCase(Locale.US))
                        .appendQueryParameter("targetId", targetUserId)
                        .appendQueryParameter("title", title)
                        .build();
                Intent intent = new Intent("android.intent.action.VIEW", uri);
                intent.putExtra("isFriend", false);
                context.startActivity(intent);

            }
        } else {
            throw new IllegalArgumentException();
        }
        return false;
    }

    public void updateCurrentUserInfo() {
        if (RongContext.getInstance() != null) {
            UserInfo userInfo = toRongUserInfo(MyUserInfo.toUserInfoModel(MyUserInfoManager.getInstance().getMyUserInfo()));
            RongIM.getInstance().setCurrentUserInfo(userInfo);
            RongIM.getInstance().refreshUserInfoCache(userInfo);
        }
    }

    public void addToBlacklist(String userId, ICallback callback) {
        RongIM.getInstance().addToBlacklist(userId, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSucess(null);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                if (callback != null) {
                    callback.onFailed(errorCode, errorCode.getValue(), errorCode.getMessage());
                }
            }
        });
    }

    public void removeFromBlacklist(String userId, ICallback callback) {
        RongIM.getInstance().removeFromBlacklist(userId, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSucess(null);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                if (callback != null) {
                    callback.onFailed(errorCode, errorCode.getValue(), errorCode.getMessage());
                }
            }
        });
    }

    public void getBlacklist(ICallback callback) {
        RongIM.getInstance().getBlacklist(new RongIMClient.GetBlacklistCallback() {
            @Override
            public void onSuccess(String[] strings) {
                if (callback != null) {
                    callback.onSucess(strings);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                if (callback != null) {
                    callback.onFailed(errorCode, errorCode.getValue(), errorCode.getMessage());
                }
            }
        });
    }

    public void getBlacklistStatus(String userId, ICallback callback) {
        RongIM.getInstance().getBlacklistStatus(userId, new RongIMClient.ResultCallback<RongIMClient.BlacklistStatus>() {
            @Override
            public void onSuccess(RongIMClient.BlacklistStatus blacklistStatus) {
                if (blacklistStatus == RongIMClient.BlacklistStatus.IN_BLACK_LIST) {
                    if (callback != null) {
                        callback.onSucess(true);
                    }
                } else if (blacklistStatus == RongIMClient.BlacklistStatus.NOT_IN_BLACK_LIST) {
                    if (callback != null) {
                        callback.onSucess(false);
                    }
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                if (callback != null) {
                    callback.onFailed(errorCode, errorCode.getValue(), errorCode.getMessage());
                }
            }
        });
    }

}
