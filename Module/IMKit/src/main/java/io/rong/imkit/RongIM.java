//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.rong.common.RLog;
import io.rong.common.SystemUtils;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imageloader.core.assist.FailReason;
import io.rong.imageloader.core.assist.ImageSize;
import io.rong.imageloader.core.listener.ImageLoadingListener;
import io.rong.imageloader.core.listener.ImageLoadingProgressListener;
import io.rong.imkit.manager.AudioRecordManager;
import io.rong.imkit.manager.IUnReadMessageObserver;
import io.rong.imkit.manager.InternalModuleManager;
import io.rong.imkit.manager.SendImageManager;
import io.rong.imkit.manager.UnReadMessageManager;
import io.rong.imkit.mention.RongMentionManager;
import io.rong.imkit.model.ConversationKey;
import io.rong.imkit.model.Event.AddMemberToDiscussionEvent;
import io.rong.imkit.model.Event.AddToBlacklistEvent;
import io.rong.imkit.model.Event.ClearConversationEvent;
import io.rong.imkit.model.Event.ConnectEvent;
import io.rong.imkit.model.Event.ConversationNotificationEvent;
import io.rong.imkit.model.Event.ConversationRemoveEvent;
import io.rong.imkit.model.Event.ConversationTopEvent;
import io.rong.imkit.model.Event.ConversationUnreadEvent;
import io.rong.imkit.model.Event.CreateDiscussionEvent;
import io.rong.imkit.model.Event.DiscussionInviteStatusEvent;
import io.rong.imkit.model.Event.FileMessageEvent;
import io.rong.imkit.model.Event.JoinChatRoomEvent;
import io.rong.imkit.model.Event.JoinGroupEvent;
import io.rong.imkit.model.Event.MediaFileEvent;
import io.rong.imkit.model.Event.MessageDeleteEvent;
import io.rong.imkit.model.Event.MessageLeftEvent;
import io.rong.imkit.model.Event.MessageRecallEvent;
import io.rong.imkit.model.Event.MessageSentStatusEvent;
import io.rong.imkit.model.Event.MessageSentStatusUpdateEvent;
import io.rong.imkit.model.Event.MessagesClearEvent;
import io.rong.imkit.model.Event.OnMessageSendErrorEvent;
import io.rong.imkit.model.Event.OnReceiveMessageEvent;
import io.rong.imkit.model.Event.OnReceiveMessageProgressEvent;
import io.rong.imkit.model.Event.QuitChatRoomEvent;
import io.rong.imkit.model.Event.QuitDiscussionEvent;
import io.rong.imkit.model.Event.QuitGroupEvent;
import io.rong.imkit.model.Event.ReadReceiptEvent;
import io.rong.imkit.model.Event.ReadReceiptRequestEvent;
import io.rong.imkit.model.Event.ReadReceiptResponseEvent;
import io.rong.imkit.model.Event.RemoteMessageRecallEvent;
import io.rong.imkit.model.Event.RemoveFromBlacklistEvent;
import io.rong.imkit.model.Event.RemoveMemberFromDiscussionEvent;
import io.rong.imkit.model.Event.SyncGroupEvent;
import io.rong.imkit.model.Event.SyncReadStatusEvent;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.notification.MessageNotificationManager;
import io.rong.imkit.plugin.image.AlbumBitmapCacheHelper;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.provider.CSPullLeaveMsgItemProvider;
import io.rong.imkit.widget.provider.DiscussionNotificationMessageItemProvider;
import io.rong.imkit.widget.provider.FileMessageItemProvider;
import io.rong.imkit.widget.provider.GroupNotificationMessageItemProvider;
import io.rong.imkit.widget.provider.HandshakeMessageItemProvider;
import io.rong.imkit.widget.provider.HistoryDividerMessageProvider;
import io.rong.imkit.widget.provider.IContainerItemProvider.ConversationProvider;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imkit.widget.provider.ImageMessageItemProvider;
import io.rong.imkit.widget.provider.InfoNotificationMsgItemProvider;
import io.rong.imkit.widget.provider.PublicServiceMultiRichContentMessageProvider;
import io.rong.imkit.widget.provider.PublicServiceRichContentMessageProvider;
import io.rong.imkit.widget.provider.RecallMessageItemProvider;
import io.rong.imkit.widget.provider.RichContentMessageItemProvider;
import io.rong.imkit.widget.provider.TextMessageItemProvider;
import io.rong.imkit.widget.provider.UnknownMessageItemProvider;
import io.rong.imkit.widget.provider.VoiceMessageItemProvider;
import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.IRongCallback.IDownloadMediaFileCallback;
import io.rong.imlib.IRongCallback.IDownloadMediaMessageCallback;
import io.rong.imlib.IRongCallback.ISendMediaMessageCallback;
import io.rong.imlib.IRongCallback.ISendMediaMessageCallbackWithUploader;
import io.rong.imlib.IRongCallback.ISendMessageCallback;
import io.rong.imlib.IRongCallback.MediaMessageUploader;
import io.rong.imlib.MessageTag;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.BlacklistStatus;
import io.rong.imlib.RongIMClient.ConnectCallback;
import io.rong.imlib.RongIMClient.ConnectionStatusListener;
import io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus;
import io.rong.imlib.RongIMClient.CreateDiscussionCallback;
import io.rong.imlib.RongIMClient.DiscussionInviteStatus;
import io.rong.imlib.RongIMClient.DownloadMediaCallback;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.GetBlacklistCallback;
import io.rong.imlib.RongIMClient.GetNotificationQuietHoursCallback;
import io.rong.imlib.RongIMClient.MediaType;
import io.rong.imlib.RongIMClient.OnRecallMessageListener;
import io.rong.imlib.RongIMClient.OnReceiveMessageListener;
import io.rong.imlib.RongIMClient.OnReceiveMessageWrapperListener;
import io.rong.imlib.RongIMClient.OperationCallback;
import io.rong.imlib.RongIMClient.ReadReceiptListener;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.RongIMClient.ResultCallback.Result;
import io.rong.imlib.RongIMClient.SearchType;
import io.rong.imlib.RongIMClient.SendImageMessageCallback;
import io.rong.imlib.RongIMClient.SendImageMessageWithUploadListenerCallback;
import io.rong.imlib.RongIMClient.SendMessageCallback;
import io.rong.imlib.RongIMClient.SyncConversationReadStatusListener;
import io.rong.imlib.RongIMClient.UploadImageStatusListener;
import io.rong.imlib.model.CSCustomServiceInfo;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Conversation.ConversationNotificationStatus;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Conversation.PublicServiceType;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.Message.ReceivedStatus;
import io.rong.imlib.model.Message.SentStatus;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.PublicServiceProfile;
import io.rong.imlib.model.PublicServiceProfileList;
import io.rong.imlib.model.UserData;
import io.rong.imlib.model.UserInfo;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.RecallNotificationMessage;
import io.rong.push.RongPushClient;

public class RongIM {
    private static final String TAG = io.rong.imkit.RongIM.class.getSimpleName();
    private static final int ON_SUCCESS_CALLBACK = 100;
    private static final int ON_PROGRESS_CALLBACK = 101;
    private static final int ON_CANCEL_CALLBACK = 102;
    private static final int ON_ERROR_CALLBACK = 103;
    private static Context mContext;
    static OnReceiveMessageListener sMessageListener;
    static ConnectionStatusListener sConnectionStatusListener;
    private static io.rong.imkit.RongIM.MessageInterceptor messageInterceptor;
    private RongIMClientWrapper mClientWrapper;
    private static boolean notificationQuiteHoursConfigured;
    private static ConnectionStatusListener mConnectionStatusListener = new ConnectionStatusListener() {
        public void onChanged(ConnectionStatus status) {
            if (status != null) {
                RLog.d(io.rong.imkit.RongIM.TAG, "ConnectionStatusListener onChanged : " + status.toString());
                if (io.rong.imkit.RongIM.sConnectionStatusListener != null) {
                    io.rong.imkit.RongIM.sConnectionStatusListener.onChanged(status);
                }

                if (status.equals(ConnectionStatus.DISCONNECTED)) {
                    SendImageManager.getInstance().reset();
                }

                if (status.equals(ConnectionStatus.CONNECTED) && !io.rong.imkit.RongIM.notificationQuiteHoursConfigured) {
                    RLog.d(io.rong.imkit.RongIM.TAG, "ConnectionStatusListener not get notificationQuietHours, get again");
                    io.rong.imkit.RongIM.SingletonHolder.sRongIM.getNotificationQuietHours((GetNotificationQuietHoursCallback) null);
                }

                RongContext.getInstance().getEventBus().post(status);
            }

        }
    };

    private RongIM() {
        this.mClientWrapper = new RongIMClientWrapper();
    }

    private static void saveToken(String token) {
        SharedPreferences preferences = mContext.getSharedPreferences("RongKitConfig", 0);
        Editor editor = preferences.edit();
        editor.putString("token", token);
        editor.commit();
    }

    private void initSDK(Context context, String appKey, boolean enablePush) {
        String current = SystemUtils.getCurrentProcessName(context);
        String mainProcessName = context.getPackageName();
        if (!mainProcessName.equals(current)) {
            RLog.w(TAG, "Init. Current process : " + current);
        } else {
            RLog.i(TAG, "init : " + current);
            mContext = context;
            RongContext.init(context);
            RongConfigurationManager.init(context);
            RongMessageItemLongClickActionManager.getInstance().init();
            initListener();
            if (TextUtils.isEmpty(appKey)) {
                try {
                    ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                    appKey = applicationInfo.metaData.getString("RONG_CLOUD_APP_KEY");
                    if (TextUtils.isEmpty(appKey)) {
                        throw new IllegalArgumentException("can't find RONG_CLOUD_APP_KEY in AndroidManifest.xml.");
                    }
                } catch (NameNotFoundException var7) {
                    var7.printStackTrace();
                    throw new ExceptionInInitializerError("can't find packageName!");
                }
            }

            RongUserInfoManager.getInstance().init(mContext, appKey, new RongUserCacheListener());
            RongIMClient.init(context, appKey, enablePush);
            registerMessageTemplate(new TextMessageItemProvider());
            registerMessageTemplate(new ImageMessageItemProvider());
            registerMessageTemplate(new VoiceMessageItemProvider(context));
            registerMessageTemplate(new DiscussionNotificationMessageItemProvider());
            registerMessageTemplate(new InfoNotificationMsgItemProvider());
            registerMessageTemplate(new RichContentMessageItemProvider());
            registerMessageTemplate(new PublicServiceMultiRichContentMessageProvider());
            registerMessageTemplate(new PublicServiceRichContentMessageProvider());
            registerMessageTemplate(new HandshakeMessageItemProvider());
            registerMessageTemplate(new RecallMessageItemProvider());
            registerMessageTemplate(new FileMessageItemProvider());
            registerMessageTemplate(new GroupNotificationMessageItemProvider());
            registerMessageTemplate(new UnknownMessageItemProvider());
            registerMessageTemplate(new CSPullLeaveMsgItemProvider());
            registerMessageTemplate(new HistoryDividerMessageProvider());
            RongExtensionManager.init(context, appKey);
            RongExtensionManager.getInstance().registerExtensionModule(new DefaultExtensionModule());
            InternalModuleManager.init(context);
            InternalModuleManager.getInstance().onInitialized(appKey);
            AlbumBitmapCacheHelper.init(context);
        }
    }

    public static void init(Application application, String appKey) {
        io.rong.imkit.RongIM.SingletonHolder.sRongIM.initSDK(application, appKey, true);
    }

    public static void init(Context context, String appKey) {
        io.rong.imkit.RongIM.SingletonHolder.sRongIM.initSDK(context, appKey, true);
    }

    public static void init(Context context, String appKey, boolean enablePush) {
        io.rong.imkit.RongIM.SingletonHolder.sRongIM.initSDK(context, appKey, enablePush);
    }

    public static void init(Context context) {
        io.rong.imkit.RongIM.SingletonHolder.sRongIM.initSDK(context, (String) null, true);
    }

    public static void registerMessageType(Class<? extends MessageContent> messageContentClass) {
        if (RongContext.getInstance() != null) {
            try {
                RongIMClient.registerMessageType(messageContentClass);
            } catch (AnnotationNotFoundException var2) {
                var2.printStackTrace();
            }
        }

    }

    public static void registerMessageTemplate(MessageProvider provider) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().registerMessageTemplate(provider);
        }

    }

    public void setCurrentUserInfo(UserInfo userInfo) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().setCurrentUserInfo(userInfo);
        }

    }

    public static io.rong.imkit.RongIM connect(final String token, final ConnectCallback callback) {
        if (RongContext.getInstance() == null) {
            RLog.e(TAG, "connect should be called in main process.");
            return io.rong.imkit.RongIM.SingletonHolder.sRongIM;
        } else {
            saveToken(token);
            RongIMClient.connect(token, new ConnectCallback() {
                public void onSuccess(String userId) {
                    if (callback != null) {
                        callback.onSuccess(userId);
                    }

                    RongContext.getInstance().getEventBus().post(ConnectEvent.obtain(true));
                    RongExtensionManager.getInstance().connect(token);
                    InternalModuleManager.getInstance().onConnected(token);
                    if (!io.rong.imkit.RongIM.notificationQuiteHoursConfigured) {
                        io.rong.imkit.RongIM.SingletonHolder.sRongIM.getNotificationQuietHours((GetNotificationQuietHoursCallback) null);
                    }

                }

                public void onError(ErrorCode e) {
                    if (callback != null) {
                        callback.onError(e);
                    }

                    RongExtensionManager.getInstance().connect(token);
                    InternalModuleManager.getInstance().onConnected(token);
                    RongContext.getInstance().getEventBus().post(ConnectEvent.obtain(false));
                }

                public void onTokenIncorrect() {
                    if (callback != null) {
                        callback.onTokenIncorrect();
                    }

                }
            });
            return io.rong.imkit.RongIM.SingletonHolder.sRongIM;
        }
    }

    private static void initListener() {
        RongIMClient.setOnReceiveMessageListener(new OnReceiveMessageWrapperListener() {
            public boolean onReceived(Message message, int left, boolean hasPackage, boolean offline) {
                boolean isProcess = false;
                if (io.rong.imkit.RongIM.sMessageListener != null) {
                    isProcess = io.rong.imkit.RongIM.sMessageListener.onReceived(message, left);
                }

                MessageTag msgTag = (MessageTag) message.getContent().getClass().getAnnotation(MessageTag.class);
                if (msgTag == null || msgTag.flag() != 3 && msgTag.flag() != 1) {
                    if (!isProcess) {
                        if (message.getMessageId() > 0) {
                            RongContext.getInstance().getEventBus().post(new OnReceiveMessageEvent(message, left));
                        } else {
                            RongContext.getInstance().getEventBus().post(new MessageLeftEvent(left));
                        }
                    }
                } else {
                    if (io.rong.imkit.RongIM.messageInterceptor != null) {
                        boolean msgRemove = io.rong.imkit.RongIM.messageInterceptor.intercept(message);
                        if (msgRemove) {
                            return true;
                        }
                    }

                    RongContext.getInstance().getEventBus().post(new OnReceiveMessageEvent(message, left));
                    if (message.getContent() != null && message.getContent().getUserInfo() != null) {
                        RongUserInfoManager.getInstance().setUserInfo(message.getContent().getUserInfo());
                    }

                    if (isProcess || message.getSenderUserId().equals(io.rong.imkit.RongIM.getInstance().getCurrentUserId())) {
                        return true;
                    }

                    MessageNotificationManager.getInstance().notifyIfNeed(RongContext.getInstance(), message, left);
                    if (left == 0 && !io.rong.imkit.RongIM.notificationQuiteHoursConfigured) {
                        RLog.d(io.rong.imkit.RongIM.TAG, "OnReceiveMessageListener not get notificationQuietHours get again");
                        io.rong.imkit.RongIM.SingletonHolder.sRongIM.getNotificationQuietHours((GetNotificationQuietHoursCallback) null);
                    }
                }

                RongExtensionManager.getInstance().onReceivedMessage(message);
                return false;
            }
        });
        boolean readRec = false;

        try {
            readRec = RongContext.getInstance().getResources().getBoolean(R.bool.rc_read_receipt);
        } catch (NotFoundException var4) {
            RLog.e(TAG, "rc_read_receipt not configure in rc_config.xml");
            var4.printStackTrace();
        }

        if (readRec) {
            RongIMClient.setReadReceiptListener(new ReadReceiptListener() {
                public void onReadReceiptReceived(Message message) {
                    RongContext.getInstance().getEventBus().post(new ReadReceiptEvent(message));
                }

                public void onMessageReceiptRequest(ConversationType type, String targetId, String messageUId) {
                    RongContext.getInstance().getEventBus().post(new ReadReceiptRequestEvent(type, targetId, messageUId));
                }

                public void onMessageReceiptResponse(ConversationType type, String targetId, String messageUId, HashMap<String, Long> respondUserIdList) {
                    RongContext.getInstance().getEventBus().post(new ReadReceiptResponseEvent(type, targetId, messageUId, respondUserIdList));
                }
            });
        }

        boolean syncReadStatus = false;

        try {
            syncReadStatus = RongContext.getInstance().getResources().getBoolean(R.bool.rc_enable_sync_read_status);
        } catch (NotFoundException var3) {
            RLog.e(TAG, "rc_enable_sync_read_status not configure in rc_config.xml");
            var3.printStackTrace();
        }

        if (syncReadStatus) {
            RongIMClient.getInstance().setSyncConversationReadStatusListener(new SyncConversationReadStatusListener() {
                public void onSyncConversationReadStatus(ConversationType type, String targetId) {
                    RongContext.getInstance().getEventBus().post(new SyncReadStatusEvent(type, targetId));
                }
            });
        }

        RongIMClient.setOnRecallMessageListener(new OnRecallMessageListener() {
            public boolean onMessageRecalled(Message message, RecallNotificationMessage recallNotificationMessage) {
                RongContext.getInstance().getEventBus().post(new RemoteMessageRecallEvent(message.getMessageId(), message.getConversationType(), recallNotificationMessage, true, message.getTargetId()));
                MessageTag msgTag = (MessageTag) recallNotificationMessage.getClass().getAnnotation(MessageTag.class);
                if (msgTag != null && (msgTag.flag() == 3 || msgTag.flag() == 1) && io.rong.imkit.RongIM.notificationQuiteHoursConfigured) {
                    MessageNotificationManager.getInstance().notifyIfNeed(RongContext.getInstance(), message, 0);
                }

                return true;
            }
        });
        RongIMClient.setConnectionStatusListener(mConnectionStatusListener);
    }

    public static void setOnReceiveMessageListener(OnReceiveMessageListener listener) {
        RLog.i(TAG, "RongIM setOnReceiveMessageListener");
        sMessageListener = listener;
    }

    public static void setConnectionStatusListener(ConnectionStatusListener listener) {
        sConnectionStatusListener = listener;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public RongIMClientWrapper getRongIMClient() {
        return this.mClientWrapper;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void disconnect(boolean isReceivePush) {
        RongIMClient.getInstance().disconnect(isReceivePush);
    }

    public void logout() {
        String current = SystemUtils.getCurrentProcessName(mContext);
        String mainProcessName = mContext.getPackageName();
        if (!mainProcessName.equals(current)) {
            RLog.w(TAG, "only can logout in main progress! current process is:" + current);
        } else {
            RongContext.getInstance().clearConversationNotifyStatusCache();
            RongIMClient.getInstance().logout();
            RongUserInfoManager.getInstance().uninit();
            UnReadMessageManager.getInstance().clearObserver();
            RongExtensionManager.getInstance().disconnect();
            notificationQuiteHoursConfigured = false;
            MessageNotificationManager.getInstance().clearNotificationQuietHours();
        }
    }

    public void setGroupMembersProvider(io.rong.imkit.RongIM.IGroupMembersProvider groupMembersProvider) {
        RongMentionManager.getInstance().setGroupMembersProvider(groupMembersProvider);
    }

    public void disconnect() {
        RongIMClient.getInstance().disconnect();
        RongExtensionManager.getInstance().disconnect();
    }

    public static io.rong.imkit.RongIM getInstance() {
        return io.rong.imkit.RongIM.SingletonHolder.sRongIM;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void startConversationList(Context context) {
        if (context == null) {
            throw new IllegalArgumentException();
        } else if (RongContext.getInstance() == null) {
            throw new ExceptionInInitializerError("RongCloud SDK not init");
        } else {
            Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon().appendPath("conversationlist").build();
            context.startActivity(new Intent("android.intent.action.VIEW", uri));
        }
    }

    public void startConversationList(Context context, Map<String, Boolean> supportedConversation) {
        if (context == null) {
            throw new IllegalArgumentException();
        } else if (RongContext.getInstance() == null) {
            throw new ExceptionInInitializerError("RongCloud SDK not init");
        } else {
            Builder builder = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon().appendPath("conversationlist");
            if (supportedConversation != null && supportedConversation.size() > 0) {
                Set<String> keys = supportedConversation.keySet();
                Iterator var5 = keys.iterator();

                while (var5.hasNext()) {
                    String key = (String) var5.next();
                    builder.appendQueryParameter(key, (Boolean) supportedConversation.get(key) ? "true" : "false");
                }
            }

            context.startActivity(new Intent("android.intent.action.VIEW", builder.build()));
        }
    }

    public void startSubConversationList(Context context, ConversationType conversationType) {
        if (context == null) {
            throw new IllegalArgumentException();
        } else if (RongContext.getInstance() == null) {
            throw new ExceptionInInitializerError("RongCloud SDK not init");
        } else {
            Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon().appendPath("subconversationlist").appendQueryParameter("type", conversationType.getName()).build();
            context.startActivity(new Intent("android.intent.action.VIEW", uri));
        }
    }

    /**
     * @deprecated
     */
    public static void setConversationBehaviorListener(io.rong.imkit.RongIM.ConversationBehaviorListener listener) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().setConversationBehaviorListener(listener);
        }

    }

    public static void setConversationClickListener(io.rong.imkit.RongIM.ConversationClickListener listener) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().setConversationClickListener(listener);
        }

    }

    public static void setConversationListBehaviorListener(io.rong.imkit.RongIM.ConversationListBehaviorListener listener) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().setConversationListBehaviorListener(listener);
        }

    }

    public static void setPublicServiceBehaviorListener(io.rong.imkit.RongIM.PublicServiceBehaviorListener listener) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().setPublicServiceBehaviorListener(listener);
        }

    }

    public void startPrivateChat(Context context, String targetUserId, String title) {
        if (context != null && !TextUtils.isEmpty(targetUserId)) {
            if (RongContext.getInstance() == null) {
                throw new ExceptionInInitializerError("RongCloud SDK not init");
            } else {
                Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon().appendPath("conversation").appendPath(ConversationType.PRIVATE.getName().toLowerCase(Locale.US)).appendQueryParameter("targetId", targetUserId).appendQueryParameter("title", title).build();
                context.startActivity(new Intent("android.intent.action.VIEW", uri));
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void startConversation(Context context, ConversationType conversationType, String targetId, String title) {
        if (context != null && !TextUtils.isEmpty(targetId) && conversationType != null) {
            Uri uri = Uri.parse("rong://" + context.getApplicationInfo().processName).buildUpon().appendPath("conversation").appendPath(conversationType.getName().toLowerCase(Locale.US)).appendQueryParameter("targetId", targetId).appendQueryParameter("title", title).build();
            context.startActivity(new Intent("android.intent.action.VIEW", uri));
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void startConversation(Context context, ConversationType conversationType, String targetId, String title, Bundle bundle) {
        if (context != null && !TextUtils.isEmpty(targetId) && conversationType != null) {
            Uri uri = Uri.parse("rong://" + context.getApplicationInfo().processName).buildUpon().appendPath("conversation").appendPath(conversationType.getName().toLowerCase(Locale.US)).appendQueryParameter("targetId", targetId).appendQueryParameter("title", title).build();
            Intent intent = new Intent("android.intent.action.VIEW", uri);
            if (bundle != null) {
                intent.putExtras(bundle);
            }

            context.startActivity(intent);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void startConversation(Context context, ConversationType conversationType, String targetId, String title, long fixedMsgSentTime) {
        if (context != null && !TextUtils.isEmpty(targetId) && conversationType != null) {
            Uri uri = Uri.parse("rong://" + context.getApplicationInfo().processName).buildUpon().appendPath("conversation").appendPath(conversationType.getName().toLowerCase(Locale.US)).appendQueryParameter("targetId", targetId).appendQueryParameter("title", title).build();
            Intent intent = new Intent("android.intent.action.VIEW", uri);
            intent.putExtra("indexMessageTime", fixedMsgSentTime);
            context.startActivity(intent);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void createDiscussionChat(final Context context, final List<String> targetUserIds, final String title) {
        if (context != null && targetUserIds != null && targetUserIds.size() != 0) {
            RongIMClient.getInstance().createDiscussion(title, targetUserIds, new CreateDiscussionCallback() {
                public void onSuccess(String targetId) {
                    Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon().appendPath("conversation").appendPath(ConversationType.DISCUSSION.getName().toLowerCase(Locale.US)).appendQueryParameter("targetIds", TextUtils.join(",", targetUserIds)).appendQueryParameter("delimiter", ",").appendQueryParameter("targetId", targetId).appendQueryParameter("title", title).build();
                    context.startActivity(new Intent("android.intent.action.VIEW", uri));
                }

                public void onError(ErrorCode e) {
                    RLog.d(io.rong.imkit.RongIM.TAG, "createDiscussionChat createDiscussion not success." + e);
                }
            });
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void createDiscussionChat(final Context context, final List<String> targetUserIds, final String title, final CreateDiscussionCallback callback) {
        if (context != null && targetUserIds != null && targetUserIds.size() != 0) {
            RongIMClient.getInstance().createDiscussion(title, targetUserIds, new CreateDiscussionCallback() {
                public void onSuccess(String targetId) {
                    Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon().appendPath("conversation").appendPath(ConversationType.DISCUSSION.getName().toLowerCase(Locale.US)).appendQueryParameter("targetIds", TextUtils.join(",", targetUserIds)).appendQueryParameter("delimiter", ",").appendQueryParameter("targetId", targetId).appendQueryParameter("title", title).build();
                    context.startActivity(new Intent("android.intent.action.VIEW", uri));
                    if (callback != null) {
                        callback.onSuccess(targetId);
                    }

                }

                public void onError(ErrorCode e) {
                    RLog.d(io.rong.imkit.RongIM.TAG, "createDiscussionChat createDiscussion not success." + e);
                    if (callback != null) {
                        callback.onError(e);
                    }

                }
            });
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void startDiscussionChat(Context context, String targetDiscussionId, String title) {
        if (context != null && !TextUtils.isEmpty(targetDiscussionId)) {
            Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon().appendPath("conversation").appendPath(ConversationType.DISCUSSION.getName().toLowerCase(Locale.US)).appendQueryParameter("targetId", targetDiscussionId).appendQueryParameter("title", title).build();
            context.startActivity(new Intent("android.intent.action.VIEW", uri));
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void startGroupChat(Context context, String targetGroupId, String title) {
        if (context != null && !TextUtils.isEmpty(targetGroupId)) {
            if (RongContext.getInstance() == null) {
                throw new ExceptionInInitializerError("RongCloud SDK not init");
            } else {
                Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon().appendPath("conversation").appendPath(ConversationType.GROUP.getName().toLowerCase(Locale.US)).appendQueryParameter("targetId", targetGroupId).appendQueryParameter("title", title).build();
                context.startActivity(new Intent("android.intent.action.VIEW", uri));
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void startChatRoomChat(Context context, String chatRoomId, boolean createIfNotExist) {
        if (context != null && !TextUtils.isEmpty(chatRoomId)) {
            if (RongContext.getInstance() == null) {
                throw new ExceptionInInitializerError("RongCloud SDK not init");
            } else {
                Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon().appendPath("conversation").appendPath(ConversationType.CHATROOM.getName().toLowerCase(Locale.US)).appendQueryParameter("targetId", chatRoomId).build();
                Intent intent = new Intent("android.intent.action.VIEW", uri);
                intent.putExtra("createIfNotExist", createIfNotExist);
                context.startActivity(intent);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void startCustomerServiceChat(Context context, String customerServiceId, String title, CSCustomServiceInfo customServiceInfo) {
        if (context != null && !TextUtils.isEmpty(customerServiceId)) {
            if (RongContext.getInstance() == null) {
                throw new ExceptionInInitializerError("RongCloud SDK not init");
            } else {
                Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon().appendPath("conversation").appendPath(ConversationType.CUSTOMER_SERVICE.getName().toLowerCase(Locale.US)).appendQueryParameter("targetId", customerServiceId).appendQueryParameter("title", title).build();
                Intent intent = new Intent("android.intent.action.VIEW", uri);
                intent.putExtra("customServiceInfo", customServiceInfo);
                context.startActivity(intent);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static void setUserInfoProvider(io.rong.imkit.RongIM.UserInfoProvider userInfoProvider, boolean isCacheUserInfo) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().setGetUserInfoProvider(userInfoProvider, isCacheUserInfo);
        }

    }

    public static void setPublicServiceProfileProvider(io.rong.imkit.RongIM.PublicServiceProfileProvider publicServiceProfileProvider) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().setPublicServiceProfileProvider(publicServiceProfileProvider);
        }

    }

    public static void setGroupUserInfoProvider(io.rong.imkit.RongIM.GroupUserInfoProvider userInfoProvider, boolean isCacheUserInfo) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().setGroupUserInfoProvider(userInfoProvider, isCacheUserInfo);
        }

    }

    public static void setGroupInfoProvider(io.rong.imkit.RongIM.GroupInfoProvider groupInfoProvider, boolean isCacheGroupInfo) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().setGetGroupInfoProvider(groupInfoProvider, isCacheGroupInfo);
        }

    }

    public void refreshDiscussionCache(Discussion discussion) {
        if (discussion != null) {
            RongUserInfoManager.getInstance().setDiscussionInfo(discussion);
        }
    }

    public void refreshUserInfoCache(UserInfo userInfo) {
        if (userInfo != null) {
            RongUserInfoManager.getInstance().setUserInfo(userInfo);
            UserInfo currentUserInfo = RongContext.getInstance().getCurrentUserInfo();
            if (currentUserInfo != null && currentUserInfo.getUserId().equals(userInfo.getUserId())) {
                RongContext.getInstance().setCurrentUserInfo(userInfo);
            }

            Uri portrait = userInfo.getPortraitUri();
            RongIMClient.getInstance().updateConversationInfo(ConversationType.PRIVATE, userInfo.getUserId(), userInfo.getName(), portrait != null ? portrait.toString() : "", (ResultCallback) null);
        }
    }

    public void refreshGroupUserInfoCache(GroupUserInfo groupUserInfo) {
        if (groupUserInfo != null) {
            RongUserInfoManager.getInstance().setGroupUserInfo(groupUserInfo);
        }
    }

    public void refreshGroupInfoCache(Group group) {
        if (group != null) {
            RongUserInfoManager.getInstance().setGroupInfo(group);
            Uri groupPortrait = group.getPortraitUri();
            RongIMClient.getInstance().updateConversationInfo(ConversationType.GROUP, group.getId(), group.getName(), groupPortrait != null ? groupPortrait.toString() : "", (ResultCallback) null);
        }
    }

    public void refreshPublicServiceProfile(PublicServiceProfile publicServiceProfile) {
        if (publicServiceProfile != null) {
            RongUserInfoManager.getInstance().setPublicServiceProfile(publicServiceProfile);
        }
    }

    public void setSendMessageListener(io.rong.imkit.RongIM.OnSendMessageListener listener) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().setOnSendMessageListener(listener);
        }

    }

    public void setMessageAttachedUserInfo(boolean state) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().setUserInfoAttachedState(state);
        }

    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setOnReceiveUnreadCountChangedListener(final io.rong.imkit.RongIM.OnReceiveUnreadCountChangedListener listener, ConversationType... conversationTypes) {
        if (listener != null && conversationTypes != null && conversationTypes.length != 0) {
            UnReadMessageManager.getInstance().addObserver(conversationTypes, new IUnReadMessageObserver() {
                public void onCountChanged(int count) {
                    listener.onMessageIncreased(count);
                }
            });
        } else {
            RLog.w(TAG, "setOnReceiveUnreadCountChangedListener Illegal argument");
        }
    }

    public void addUnReadMessageCountChangedObserver(IUnReadMessageObserver observer, ConversationType... conversationTypes) {
        if (observer != null && conversationTypes != null && conversationTypes.length != 0) {
            UnReadMessageManager.getInstance().addObserver(conversationTypes, observer);
        } else {
            RLog.w(TAG, "addOnReceiveUnreadCountChangedListener Illegal argument");
            throw new IllegalArgumentException("observer must not be null and must include at least one conversationType");
        }
    }

    public void removeUnReadMessageCountChangedObserver(IUnReadMessageObserver observer) {
        if (observer == null) {
            RLog.w(TAG, "removeOnReceiveUnreadCountChangedListener Illegal argument");
        } else {
            UnReadMessageManager.getInstance().removeObserver(observer);
        }
    }

    public void startPublicServiceProfile(Context context, ConversationType conversationType, String targetId) {
        if (context != null && conversationType != null && !TextUtils.isEmpty(targetId)) {
            if (RongContext.getInstance() == null) {
                throw new ExceptionInInitializerError("RongCloud SDK not init");
            } else {
                Uri uri = Uri.parse("rong://" + context.getApplicationInfo().processName).buildUpon().appendPath("publicServiceProfile").appendPath(conversationType.getName().toLowerCase(Locale.US)).appendQueryParameter("targetId", targetId).build();
                Intent intent = new Intent("android.intent.action.VIEW", uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void registerConversationTemplate(ConversationProvider provider) {
        if (RongContext.getInstance() != null) {
            if (provider == null) {
                throw new IllegalArgumentException();
            }

            RongContext.getInstance().registerConversationTemplate(provider);
        }

    }

    public void enableNewComingMessageIcon(boolean state) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().showNewMessageIcon(state);
        }

    }

    public void enableUnreadMessageIcon(boolean state) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().showUnreadMessageIcon(state);
        }

    }

    public void setMaxVoiceDurationg(int sec) {
        AudioRecordManager.getInstance().setMaxVoiceDuration(sec);
    }

    public ConnectionStatus getCurrentConnectionStatus() {
        return RongIMClient.getInstance().getCurrentConnectionStatus();
    }

    public void getConversationList(ResultCallback<List<Conversation>> callback) {
        RongIMClient.getInstance().getConversationList(callback);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public List<Conversation> getConversationList() {
        return RongIMClient.getInstance().getConversationList();
    }

    public void getConversationList(ResultCallback<List<Conversation>> callback, ConversationType... types) {
        RongIMClient.getInstance().getConversationList(callback, types);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public List<Conversation> getConversationList(ConversationType... types) {
        return RongIMClient.getInstance().getConversationList(types);
    }

    public void getConversation(ConversationType type, String targetId, ResultCallback<Conversation> callback) {
        RongIMClient.getInstance().getConversation(type, targetId, callback);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public Conversation getConversation(ConversationType type, String targetId) {
        return RongIMClient.getInstance().getConversation(type, targetId);
    }

    public void removeConversation(final ConversationType type, final String targetId, final ResultCallback<Boolean> callback) {
        RongIMClient.getInstance().removeConversation(type, targetId, new ResultCallback<Boolean>() {
            public void onSuccess(Boolean bool) {
                if (callback != null) {
                    callback.onSuccess(bool);
                }

                if (bool) {
                    RongContext.getInstance().getEventBus().post(new ConversationRemoveEvent(type, targetId));
                }

            }

            public void onError(ErrorCode e) {
                if (callback != null) {
                    callback.onError(e);
                }

            }
        });
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean removeConversation(ConversationType type, String targetId) {
        boolean result = RongIMClient.getInstance().removeConversation(type, targetId);
        if (result) {
            RongContext.getInstance().getEventBus().post(new ConversationRemoveEvent(type, targetId));
        }

        return result;
    }

    public void setConversationToTop(final ConversationType type, final String id, final boolean isTop, final ResultCallback<Boolean> callback) {
        RongIMClient.getInstance().setConversationToTop(type, id, isTop, new ResultCallback<Boolean>() {
            public void onSuccess(Boolean bool) {
                if (callback != null) {
                    callback.onSuccess(bool);
                }

                if (bool) {
                    RongContext.getInstance().getEventBus().post(new ConversationTopEvent(type, id, isTop));
                }

            }

            public void onError(ErrorCode e) {
                if (callback != null) {
                    callback.onError(e);
                }

            }
        });
    }

    public void setConversationToTop(final ConversationType type, final String id, final boolean isTop, final boolean needCreate, final ResultCallback<Boolean> callback) {
        RongIMClient.getInstance().setConversationToTop(type, id, isTop, needCreate, new ResultCallback<Boolean>() {
            public void onSuccess(Boolean bool) {
                if (callback != null) {
                    callback.onSuccess(bool);
                }

                if (bool && (!isTop || needCreate)) {
                    RongContext.getInstance().getEventBus().post(new ConversationTopEvent(type, id, isTop));
                }

            }

            public void onError(ErrorCode e) {
                if (callback != null) {
                    callback.onError(e);
                }

            }
        });
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean setConversationToTop(ConversationType conversationType, String targetId, boolean isTop) {
        boolean result = RongIMClient.getInstance().setConversationToTop(conversationType, targetId, isTop);
        if (result) {
            RongContext.getInstance().getEventBus().post(new ConversationTopEvent(conversationType, targetId, isTop));
        }

        return result;
    }

    public void getTotalUnreadCount(final ResultCallback<Integer> callback) {
        RongIMClient.getInstance().getTotalUnreadCount(new ResultCallback<Integer>() {
            public void onSuccess(Integer integer) {
                if (callback != null) {
                    callback.onSuccess(integer);
                }

            }

            public void onError(ErrorCode e) {
                if (callback != null) {
                    callback.onError(e);
                }

            }
        });
    }

    /**
     * @deprecated
     */
    @Deprecated
    public int getTotalUnreadCount() {
        return RongIMClient.getInstance().getTotalUnreadCount();
    }

    public void getUnreadCount(ConversationType conversationType, String targetId, ResultCallback<Integer> callback) {
        RongIMClient.getInstance().getUnreadCount(conversationType, targetId, callback);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public int getUnreadCount(ConversationType conversationType, String targetId) {
        return RongIMClient.getInstance().getUnreadCount(conversationType, targetId);
    }

    public void getUnreadCount(ResultCallback<Integer> callback, ConversationType... conversationTypes) {
        RongIMClient.getInstance().getUnreadCount(callback, conversationTypes);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public int getUnreadCount(ConversationType... conversationTypes) {
        return RongIMClient.getInstance().getUnreadCount(conversationTypes);
    }

    public void getUnreadCount(ConversationType[] conversationTypes, ResultCallback<Integer> callback) {
        RongIMClient.getInstance().getUnreadCount(conversationTypes, callback);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public List<Message> getLatestMessages(ConversationType conversationType, String targetId, int count) {
        return RongIMClient.getInstance().getLatestMessages(conversationType, targetId, count);
    }

    public void getLatestMessages(ConversationType conversationType, String targetId, int count, ResultCallback<List<Message>> callback) {
        RongIMClient.getInstance().getLatestMessages(conversationType, targetId, count, callback);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public List<Message> getHistoryMessages(ConversationType conversationType, String targetId, int oldestMessageId, int count) {
        return RongIMClient.getInstance().getHistoryMessages(conversationType, targetId, oldestMessageId, count);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public List<Message> getHistoryMessages(ConversationType conversationType, String targetId, String objectName, int oldestMessageId, int count) {
        return RongIMClient.getInstance().getHistoryMessages(conversationType, targetId, objectName, oldestMessageId, count);
    }

    public void getHistoryMessages(ConversationType conversationType, String targetId, String objectName, int oldestMessageId, int count, ResultCallback<List<Message>> callback) {
        RongIMClient.getInstance().getHistoryMessages(conversationType, targetId, objectName, oldestMessageId, count, callback);
    }

    public void getHistoryMessages(ConversationType conversationType, String targetId, int oldestMessageId, int count, ResultCallback<List<Message>> callback) {
        RongIMClient.getInstance().getHistoryMessages(conversationType, targetId, oldestMessageId, count, callback);
    }

    public void getRemoteHistoryMessages(ConversationType conversationType, String targetId, long dataTime, int count, ResultCallback<List<Message>> callback) {
        RongIMClient.getInstance().getRemoteHistoryMessages(conversationType, targetId, dataTime, count, callback);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean deleteMessages(int[] messageIds) {
        Boolean bool = RongIMClient.getInstance().deleteMessages(messageIds);
        if (bool) {
            RongContext.getInstance().getEventBus().post(new MessageDeleteEvent(messageIds));
        }

        return bool;
    }

    public void deleteMessages(final int[] messageIds, final ResultCallback<Boolean> callback) {
        RongIMClient.getInstance().deleteMessages(messageIds, new ResultCallback<Boolean>() {
            public void onSuccess(Boolean bool) {
                if (bool) {
                    RongContext.getInstance().getEventBus().post(new MessageDeleteEvent(messageIds));
                }

                if (callback != null) {
                    callback.onSuccess(bool);
                }

            }

            public void onError(ErrorCode e) {
                if (callback != null) {
                    callback.onError(e);
                }

            }
        });
    }

    public void deleteMessages(final ConversationType conversationType, final String targetId, final ResultCallback<Boolean> callback) {
        RongIMClient.getInstance().deleteMessages(conversationType, targetId, new ResultCallback<Boolean>() {
            public void onSuccess(Boolean bool) {
                if (bool) {
                    RongContext.getInstance().getEventBus().post(new MessagesClearEvent(conversationType, targetId));
                }

                if (callback != null) {
                    callback.onSuccess(bool);
                }

            }

            public void onError(ErrorCode e) {
                if (callback != null) {
                    callback.onError(e);
                }

            }
        });
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean clearMessages(ConversationType conversationType, String targetId) {
        boolean bool = RongIMClient.getInstance().clearMessages(conversationType, targetId);
        if (bool) {
            RongContext.getInstance().getEventBus().post(new MessagesClearEvent(conversationType, targetId));
        }

        return bool;
    }

    public void clearMessages(final ConversationType conversationType, final String targetId, final ResultCallback<Boolean> callback) {
        RongIMClient.getInstance().clearMessages(conversationType, targetId, new ResultCallback<Boolean>() {
            public void onSuccess(Boolean bool) {
                if (bool) {
                    RongContext.getInstance().getEventBus().post(new MessagesClearEvent(conversationType, targetId));
                }

                if (callback != null) {
                    callback.onSuccess(bool);
                }

            }

            public void onError(ErrorCode e) {
                if (callback != null) {
                    callback.onError(e);
                }

            }
        });
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean clearMessagesUnreadStatus(ConversationType conversationType, String targetId) {
        boolean result = RongIMClient.getInstance().clearMessagesUnreadStatus(conversationType, targetId);
        if (result) {
            RongContext.getInstance().getEventBus().post(new ConversationUnreadEvent(conversationType, targetId));
        }

        return result;
    }

    public void clearMessagesUnreadStatus(final ConversationType conversationType, final String targetId, final ResultCallback<Boolean> callback) {
        RongIMClient.getInstance().clearMessagesUnreadStatus(conversationType, targetId, new ResultCallback<Boolean>() {
            public void onSuccess(Boolean bool) {
                if (callback != null) {
                    callback.onSuccess(bool);
                }

                RongContext.getInstance().getEventBus().post(new ConversationUnreadEvent(conversationType, targetId));
            }

            public void onError(ErrorCode e) {
                if (callback != null) {
                    callback.onError(e);
                }

            }
        });
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean setMessageExtra(int messageId, String value) {
        return RongIMClient.getInstance().setMessageExtra(messageId, value);
    }

    public void setMessageExtra(int messageId, String value, ResultCallback<Boolean> callback) {
        RongIMClient.getInstance().setMessageExtra(messageId, value, callback);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean setMessageReceivedStatus(int messageId, ReceivedStatus receivedStatus) {
        return RongIMClient.getInstance().setMessageReceivedStatus(messageId, receivedStatus);
    }

    public void setMessageReceivedStatus(int messageId, ReceivedStatus receivedStatus, ResultCallback<Boolean> callback) {
        RongIMClient.getInstance().setMessageReceivedStatus(messageId, receivedStatus, callback);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean setMessageSentStatus(int messageId, SentStatus sentStatus) {
        boolean result = RongIMClient.getInstance().setMessageSentStatus(messageId, sentStatus);
        if (result) {
            RongContext.getInstance().getEventBus().post(new MessageSentStatusEvent(messageId, sentStatus));
        }

        return result;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setMessageSentStatus(final int messageId, final SentStatus sentStatus, final ResultCallback<Boolean> callback) {
        RongIMClient.getInstance().setMessageSentStatus(messageId, sentStatus, new ResultCallback<Boolean>() {
            public void onSuccess(Boolean bool) {
                if (callback != null) {
                    callback.onSuccess(bool);
                }

                if (bool) {
                    RongContext.getInstance().getEventBus().post(new MessageSentStatusEvent(messageId, sentStatus));
                }

            }

            public void onError(ErrorCode e) {
                if (callback != null) {
                    callback.onError(e);
                }

            }
        });
    }

    public void setMessageSentStatus(final Message message, final ResultCallback<Boolean> callback) {
        if (message != null && message.getMessageId() > 0) {
            RongIMClient.getInstance().setMessageSentStatus(message, new ResultCallback<Boolean>() {
                public void onSuccess(Boolean bool) {
                    if (callback != null) {
                        callback.onSuccess(bool);
                    }

                    if (bool) {
                        RongContext.getInstance().getEventBus().post(new MessageSentStatusUpdateEvent(message, message.getSentStatus()));
                    }

                }

                public void onError(ErrorCode e) {
                    if (callback != null) {
                        callback.onError(e);
                    }

                }
            });
        } else {
            RLog.e(TAG, "setMessageSentStatus message is null or messageId <= 0");
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public String getTextMessageDraft(ConversationType conversationType, String targetId) {
        return RongIMClient.getInstance().getTextMessageDraft(conversationType, targetId);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean saveTextMessageDraft(ConversationType conversationType, String targetId, String content) {
        return RongIMClient.getInstance().saveTextMessageDraft(conversationType, targetId, content);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean clearTextMessageDraft(ConversationType conversationType, String targetId) {
        return RongIMClient.getInstance().clearTextMessageDraft(conversationType, targetId);
    }

    public void getTextMessageDraft(ConversationType conversationType, String targetId, ResultCallback<String> callback) {
        RongIMClient.getInstance().getTextMessageDraft(conversationType, targetId, callback);
    }

    public void saveTextMessageDraft(ConversationType conversationType, String targetId, String content, ResultCallback<Boolean> callback) {
        RongIMClient.getInstance().saveTextMessageDraft(conversationType, targetId, content, callback);
    }

    public void clearTextMessageDraft(ConversationType conversationType, String targetId, ResultCallback<Boolean> callback) {
        RongIMClient.getInstance().clearTextMessageDraft(conversationType, targetId, callback);
    }

    public void getDiscussion(String discussionId, ResultCallback<Discussion> callback) {
        RongIMClient.getInstance().getDiscussion(discussionId, callback);
    }

    public void setDiscussionName(final String discussionId, final String name, final OperationCallback callback) {
        RongIMClient.getInstance().setDiscussionName(discussionId, name, new OperationCallback() {
            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }

            public void onSuccess() {
                if (callback != null) {
                    RongUserInfoManager.getInstance().setDiscussionInfo(new Discussion(discussionId, name));
                    callback.onSuccess();
                }

            }
        });
    }

    public void createDiscussion(final String name, final List<String> userIdList, final CreateDiscussionCallback callback) {
        RongIMClient.getInstance().createDiscussion(name, userIdList, new CreateDiscussionCallback() {
            public void onSuccess(String discussionId) {
                RongContext.getInstance().getEventBus().post(new CreateDiscussionEvent(discussionId, name, userIdList));
                if (callback != null) {
                    callback.onSuccess(discussionId);
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    public void addMemberToDiscussion(final String discussionId, final List<String> userIdList, final OperationCallback callback) {
        RongIMClient.getInstance().addMemberToDiscussion(discussionId, userIdList, new OperationCallback() {
            public void onSuccess() {
                RongContext.getInstance().getEventBus().post(new AddMemberToDiscussionEvent(discussionId, userIdList));
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    public void removeMemberFromDiscussion(final String discussionId, final String userId, final OperationCallback callback) {
        RongIMClient.getInstance().removeMemberFromDiscussion(discussionId, userId, new OperationCallback() {
            public void onSuccess() {
                RongContext.getInstance().getEventBus().post(new RemoveMemberFromDiscussionEvent(discussionId, userId));
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    public void quitDiscussion(final String discussionId, final OperationCallback callback) {
        RongIMClient.getInstance().quitDiscussion(discussionId, new OperationCallback() {
            public void onSuccess() {
                RongContext.getInstance().getEventBus().post(new QuitDiscussionEvent(discussionId));
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    /**
     * @deprecated
     */
    public void insertMessage(ConversationType type, String targetId, String senderUserId, MessageContent content, long sentTime, final ResultCallback<Message> callback) {
        MessageTag tag = (MessageTag) content.getClass().getAnnotation(MessageTag.class);
        if (tag != null && (tag.flag() & 1) == 1) {
            RongIMClient.getInstance().insertMessage(type, targetId, senderUserId, content, sentTime, new ResultCallback<Message>() {
                public void onSuccess(Message message) {
                    if (callback != null) {
                        callback.onSuccess(message);
                    }

                    RongContext.getInstance().getEventBus().post(message);
                }

                public void onError(ErrorCode e) {
                    if (callback != null) {
                        callback.onError(e);
                    }

                    RongContext.getInstance().getEventBus().post(e);
                }
            });
        } else {
            RLog.e(TAG, "insertMessage Message is missing MessageTag.ISPERSISTED");
        }

    }

    /**
     * @deprecated
     */
    public void insertMessage(ConversationType conversationType, String targetId, String senderUserId, MessageContent content, ResultCallback<Message> callback) {
        this.insertMessage(conversationType, targetId, senderUserId, content, System.currentTimeMillis(), callback);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public Message insertMessage(ConversationType type, String targetId, String senderUserId, MessageContent content) {
        MessageTag tag = (MessageTag) content.getClass().getAnnotation(MessageTag.class);
        Message message;
        if (tag != null && (tag.flag() & 1) == 1) {
            message = RongIMClient.getInstance().insertMessage(type, targetId, senderUserId, content);
        } else {
            message = Message.obtain(targetId, type, content);
            RLog.e(TAG, "insertMessage Message is missing MessageTag.ISPERSISTED");
        }

        RongContext.getInstance().getEventBus().post(message);
        return message;
    }

    public void insertIncomingMessage(ConversationType type, String targetId, String senderUserId, ReceivedStatus receivedStatus, MessageContent content, ResultCallback<Message> callback) {
        this.insertIncomingMessage(type, targetId, senderUserId, receivedStatus, content, System.currentTimeMillis(), callback);
    }

    public void insertIncomingMessage(ConversationType type, String targetId, String senderUserId, ReceivedStatus receivedStatus, MessageContent content, long sentTime, final ResultCallback<Message> callback) {
        MessageTag tag = (MessageTag) content.getClass().getAnnotation(MessageTag.class);
        if (tag != null && (tag.flag() & 1) == 1) {
            RongIMClient.getInstance().insertIncomingMessage(type, targetId, senderUserId, receivedStatus, content, sentTime, new ResultCallback<Message>() {
                public void onSuccess(Message message) {
                    if (callback != null) {
                        callback.onSuccess(message);
                    }

                    RongContext.getInstance().getEventBus().post(message);
                }

                public void onError(ErrorCode e) {
                    if (callback != null) {
                        callback.onError(e);
                    }

                    RongContext.getInstance().getEventBus().post(e);
                }
            });
        } else {
            RLog.e(TAG, "insertMessage Message is missing MessageTag.ISPERSISTED");
        }

    }

    public void insertOutgoingMessage(ConversationType type, String targetId, SentStatus sentStatus, MessageContent content, ResultCallback<Message> callback) {
        this.insertOutgoingMessage(type, targetId, sentStatus, content, System.currentTimeMillis(), callback);
    }

    public void insertOutgoingMessage(ConversationType type, String targetId, SentStatus sentStatus, MessageContent content, long sentTime, final ResultCallback<Message> callback) {
        MessageTag tag = (MessageTag) content.getClass().getAnnotation(MessageTag.class);
        if (tag != null && (tag.flag() & 1) == 1) {
            RongIMClient.getInstance().insertOutgoingMessage(type, targetId, sentStatus, content, sentTime, new ResultCallback<Message>() {
                public void onSuccess(Message message) {
                    if (callback != null) {
                        callback.onSuccess(message);
                    }

                    RongContext.getInstance().getEventBus().post(message);
                }

                public void onError(ErrorCode e) {
                    if (callback != null) {
                        callback.onError(e);
                    }

                    RongContext.getInstance().getEventBus().post(e);
                }
            });
        } else {
            RLog.e(TAG, "insertMessage Message is missing MessageTag.ISPERSISTED");
        }

    }

    /**
     * @deprecated
     */
    @Deprecated
    public Message sendMessage(ConversationType type, String targetId, MessageContent content, String pushContent, String pushData, final SendMessageCallback callback) {
        final Result<Message> result = new Result();
        Message messageTemp = Message.obtain(targetId, type, content);
        Message temp = this.filterSendMessage(messageTemp);
        if (temp == null) {
            return null;
        } else {
            if (temp != messageTemp) {
                messageTemp = temp;
            }

            content = messageTemp.getContent();
            content = this.setMessageAttachedUserInfo(content);
            Message message = RongIMClient.getInstance().sendMessage(type, targetId, content, pushContent, pushData, new SendMessageCallback() {
                public void onSuccess(Integer messageId) {
                    if (result.t != null) {
                        ((Message) result.t).setSentStatus(SentStatus.SENT);
                        long tt = RongIMClient.getInstance().getSendTimeByMessageId(messageId);
                        if (tt != 0L) {
                            ((Message) result.t).setSentTime(tt);
                        }

                        io.rong.imkit.RongIM.this.filterSentMessage((Message) result.t, (ErrorCode) null);
                        if (callback != null) {
                            callback.onSuccess(messageId);
                        }

                    }
                }

                public void onError(Integer messageId, ErrorCode errorCode) {
                    if (result.t != null) {
                        ((Message) result.t).setSentStatus(SentStatus.FAILED);
                        io.rong.imkit.RongIM.this.filterSentMessage((Message) result.t, errorCode);
                        if (callback != null) {
                            callback.onError(messageId, errorCode);
                        }

                    }
                }
            });
            MessageTag tag = (MessageTag) content.getClass().getAnnotation(MessageTag.class);
            if (tag != null && (tag.flag() & 1) == 1) {
                RongContext.getInstance().getEventBus().post(message);
            }

            result.t = message;
            return message;
        }
    }

    /**
     * @deprecated
     */
    public void sendMessage(ConversationType type, String targetId, MessageContent content, String pushContent, String pushData, final SendMessageCallback callback, final ResultCallback<Message> resultCallback) {
        final Result<Message> result = new Result();
        Message message = Message.obtain(targetId, type, content);
        Message temp = this.filterSendMessage(message);
        if (temp != null) {
            if (temp != message) {
                message = temp;
            }

            content = message.getContent();
            content = this.setMessageAttachedUserInfo(content);
            RongIMClient.getInstance().sendMessage(type, targetId, content, pushContent, pushData, new SendMessageCallback() {
                public void onSuccess(Integer messageId) {
                    if (result.t != null) {
                        ((Message) result.t).setSentStatus(SentStatus.SENT);
                        long tt = RongIMClient.getInstance().getSendTimeByMessageId(messageId);
                        if (tt != 0L) {
                            ((Message) result.t).setSentTime(tt);
                        }

                        io.rong.imkit.RongIM.this.filterSentMessage((Message) result.t, (ErrorCode) null);
                        if (callback != null) {
                            callback.onSuccess(messageId);
                        }

                    }
                }

                public void onError(Integer messageId, ErrorCode errorCode) {
                    if (result.t != null) {
                        ((Message) result.t).setSentStatus(SentStatus.FAILED);
                        io.rong.imkit.RongIM.this.filterSentMessage((Message) result.t, errorCode);
                        if (callback != null) {
                            callback.onError(messageId, errorCode);
                        }

                    }
                }
            }, new ResultCallback<Message>() {
                public void onSuccess(Message message) {
                    MessageTag tag = (MessageTag) message.getContent().getClass().getAnnotation(MessageTag.class);
                    if (tag != null && (tag.flag() & 1) == 1) {
                        RongContext.getInstance().getEventBus().post(message);
                    }

                    result.t = message;
                    if (resultCallback != null) {
                        resultCallback.onSuccess(message);
                    }

                }

                public void onError(ErrorCode e) {
                    RongContext.getInstance().getEventBus().post(e);
                    if (resultCallback != null) {
                        resultCallback.onError(e);
                    }

                }
            });
        }
    }

    /**
     * @deprecated
     */
    public void sendMessage(Message message, String pushContent, String pushData, final SendMessageCallback callback, final ResultCallback<Message> resultCallback) {
        final Result<Message> result = new Result();
        Message temp = this.filterSendMessage(message);
        if (temp != null) {
            if (temp != message) {
                message = temp;
            }

            message.setContent(this.setMessageAttachedUserInfo(message.getContent()));
            RongIMClient.getInstance().sendMessage(message, pushContent, pushData, new SendMessageCallback() {
                public void onSuccess(Integer messageId) {
                    if (result.t != null) {
                        ((Message) result.t).setSentStatus(SentStatus.SENT);
                        long tt = RongIMClient.getInstance().getSendTimeByMessageId(messageId);
                        if (tt != 0L) {
                            ((Message) result.t).setSentTime(tt);
                        }

                        io.rong.imkit.RongIM.this.filterSentMessage((Message) result.t, (ErrorCode) null);
                        if (callback != null) {
                            callback.onSuccess(messageId);
                        }

                    }
                }

                public void onError(Integer messageId, ErrorCode errorCode) {
                    if (result.t != null) {
                        ((Message) result.t).setSentStatus(SentStatus.FAILED);
                        io.rong.imkit.RongIM.this.filterSentMessage((Message) result.t, errorCode);
                        if (callback != null) {
                            callback.onError(messageId, errorCode);
                        }

                    }
                }
            }, new ResultCallback<Message>() {
                public void onSuccess(Message message) {
                    result.t = message;
                    MessageTag tag = (MessageTag) message.getContent().getClass().getAnnotation(MessageTag.class);
                    if (tag != null && (tag.flag() & 1) == 1) {
                        RongContext.getInstance().getEventBus().post(message);
                    }

                    if (resultCallback != null) {
                        resultCallback.onSuccess(message);
                    }

                }

                public void onError(ErrorCode e) {
                    RongContext.getInstance().getEventBus().post(e);
                    if (resultCallback != null) {
                        resultCallback.onError(e);
                    }

                }
            });
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public Message sendMessage(Message message, String pushContent, String pushData, final SendMessageCallback callback) {
        final Result<Message> result = new Result();
        Message temp = this.filterSendMessage(message);
        if (temp == null) {
            return null;
        } else {
            if (temp != message) {
                message = temp;
            }

            message.setContent(this.setMessageAttachedUserInfo(message.getContent()));
            Message msg = RongIMClient.getInstance().sendMessage(message, pushContent, pushData, new SendMessageCallback() {
                public void onSuccess(Integer messageId) {
                    if (result.t != null) {
                        ((Message) result.t).setSentStatus(SentStatus.SENT);
                        long tt = RongIMClient.getInstance().getSendTimeByMessageId(messageId);
                        if (tt != 0L) {
                            ((Message) result.t).setSentTime(tt);
                        }

                        io.rong.imkit.RongIM.this.filterSentMessage((Message) result.t, (ErrorCode) null);
                        if (callback != null) {
                            callback.onSuccess(messageId);
                        }

                    }
                }

                public void onError(Integer messageId, ErrorCode errorCode) {
                    if (result.t != null) {
                        ((Message) result.t).setSentStatus(SentStatus.FAILED);
                        io.rong.imkit.RongIM.this.filterSentMessage((Message) result.t, errorCode);
                        if (callback != null) {
                            callback.onError(messageId, errorCode);
                        }

                    }
                }
            });
            MessageTag tag = (MessageTag) message.getContent().getClass().getAnnotation(MessageTag.class);
            if (tag != null && (tag.flag() & 1) == 1) {
                EventBus.getDefault().post(msg);
            }

            result.t = msg;
            return msg;
        }
    }

    public void sendMessage(Message message, String pushContent, String pushData, final ISendMessageCallback callback) {
        Message filterMsg = this.filterSendMessage(message);
        if (filterMsg == null) {
            RLog.w(TAG, "sendMessage: 因在 onSend 中消息被过滤为 null，取消发送。");
        } else {
            if (filterMsg != message) {
                message = filterMsg;
            }

            message.setContent(this.setMessageAttachedUserInfo(message.getContent()));
            RongIMClient.getInstance().sendMessage(message, pushContent, pushData, new ISendMessageCallback() {
                public void onAttached(Message message) {
                    MessageTag tag = (MessageTag) message.getContent().getClass().getAnnotation(MessageTag.class);
                    if (tag != null && (tag.flag() & 1) == 1) {
                        RongContext.getInstance().getEventBus().post(message);
                    }

                    if (callback != null) {
                        callback.onAttached(message);
                    }

                }

                public void onSuccess(Message message) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, (ErrorCode) null);
                    if (callback != null) {
                        callback.onSuccess(message);
                    }

                }

                public void onError(Message message, ErrorCode errorCode) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, errorCode);
                    if (callback != null) {
                        callback.onError(message, errorCode);
                    }

                }
            });
        }
    }

    public void sendLocationMessage(Message message, String pushContent, String pushData, final ISendMessageCallback sendMessageCallback) {
        Message filterMsg = this.filterSendMessage(message);
        if (filterMsg == null) {
            RLog.w(TAG, "sendLocationMessage: 因在 onSend 中消息被过滤为 null，取消发送。");
        } else {
            if (filterMsg != message) {
                message = filterMsg;
            }

            message.setContent(this.setMessageAttachedUserInfo(message.getContent()));
            RongIMClient.getInstance().sendLocationMessage(message, pushContent, pushData, new ISendMessageCallback() {
                public void onAttached(Message message) {
                    MessageTag tag = (MessageTag) message.getContent().getClass().getAnnotation(MessageTag.class);
                    if (tag != null && (tag.flag() & 1) == 1) {
                        RongContext.getInstance().getEventBus().post(message);
                    }

                    if (sendMessageCallback != null) {
                        sendMessageCallback.onAttached(message);
                    }

                }

                public void onSuccess(Message message) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, (ErrorCode) null);
                    if (sendMessageCallback != null) {
                        sendMessageCallback.onSuccess(message);
                    }

                }

                public void onError(Message message, ErrorCode errorCode) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, errorCode);
                    if (sendMessageCallback != null) {
                        sendMessageCallback.onError(message, errorCode);
                    }

                }
            });
        }
    }

    public void sendImageMessage(ConversationType type, String targetId, MessageContent content, String pushContent, String pushData, final SendImageMessageCallback callback) {
        Message message = Message.obtain(targetId, type, content);
        Message temp = this.filterSendMessage(message);
        if (temp != null) {
            if (temp != message) {
                message = temp;
            }

            content = message.getContent();
            content = this.setMessageAttachedUserInfo(content);
            final Result<OnReceiveMessageProgressEvent> result = new Result();
            result.t = new OnReceiveMessageProgressEvent();
            SendImageMessageCallback sendMessageCallback = new SendImageMessageCallback() {
                public void onAttached(Message message) {
                    RongContext.getInstance().getEventBus().post(message);
                    if (callback != null) {
                        callback.onAttached(message);
                    }

                }

                public void onProgress(Message message, int progress) {
                    if (result.t != null) {
                        ((OnReceiveMessageProgressEvent) result.t).setMessage(message);
                        ((OnReceiveMessageProgressEvent) result.t).setProgress(progress);
                        RongContext.getInstance().getEventBus().post(result.t);
                        if (callback != null) {
                            callback.onProgress(message, progress);
                        }

                    }
                }

                public void onError(Message message, ErrorCode errorCode) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, errorCode);
                    if (callback != null) {
                        callback.onError(message, errorCode);
                    }

                }

                public void onSuccess(Message message) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, (ErrorCode) null);
                    if (callback != null) {
                        callback.onSuccess(message);
                    }

                }
            };
            RongIMClient.getInstance().sendImageMessage(type, targetId, content, pushContent, pushData, sendMessageCallback);
        }
    }

    public void sendImageMessage(Message message, String pushContent, String pushData, boolean filter, final SendImageMessageCallback callback) {
        if (filter) {
            Message temp = this.filterSendMessage(message);
            if (temp == null) {
                return;
            }

            if (temp != message) {
                message = temp;
            }
        }

        this.setMessageAttachedUserInfo(message.getContent());
        final Result<OnReceiveMessageProgressEvent> result = new Result();
        result.t = new OnReceiveMessageProgressEvent();
        SendImageMessageCallback sendMessageCallback = new SendImageMessageCallback() {
            public void onAttached(Message message) {
                RongContext.getInstance().getEventBus().post(message);
                if (callback != null) {
                    callback.onAttached(message);
                }

            }

            public void onProgress(Message message, int progress) {
                if (result.t != null) {
                    ((OnReceiveMessageProgressEvent) result.t).setMessage(message);
                    ((OnReceiveMessageProgressEvent) result.t).setProgress(progress);
                    RongContext.getInstance().getEventBus().post(result.t);
                    if (callback != null) {
                        callback.onProgress(message, progress);
                    }

                }
            }

            public void onError(Message message, ErrorCode errorCode) {
                io.rong.imkit.RongIM.this.filterSentMessage(message, errorCode);
                if (callback != null) {
                    callback.onError(message, errorCode);
                }

            }

            public void onSuccess(Message message) {
                io.rong.imkit.RongIM.this.filterSentMessage(message, (ErrorCode) null);
                if (callback != null) {
                    callback.onSuccess(message);
                }

            }
        };
        RongIMClient.getInstance().sendImageMessage(message, pushContent, pushData, sendMessageCallback);
    }

    public void sendImageMessage(Message message, String pushContent, String pushData, SendImageMessageCallback callback) {
        this.sendImageMessage(message, pushContent, pushData, true, callback);
    }

    public void sendImageMessage(Message message, String pushContent, String pushData, final SendImageMessageWithUploadListenerCallback callback) {
        Message temp = this.filterSendMessage(message);
        if (temp != null) {
            if (temp != message) {
                message = temp;
            }

            final Result<OnReceiveMessageProgressEvent> result = new Result();
            result.t = new OnReceiveMessageProgressEvent();
            SendImageMessageWithUploadListenerCallback sendMessageCallback = new SendImageMessageWithUploadListenerCallback() {
                public void onAttached(Message message, UploadImageStatusListener listener) {
                    RongContext.getInstance().getEventBus().post(message);
                    if (callback != null) {
                        callback.onAttached(message, listener);
                    }

                }

                public void onProgress(Message message, int progress) {
                    if (result.t != null) {
                        ((OnReceiveMessageProgressEvent) result.t).setMessage(message);
                        ((OnReceiveMessageProgressEvent) result.t).setProgress(progress);
                        RongContext.getInstance().getEventBus().post(result.t);
                        if (callback != null) {
                            callback.onProgress(message, progress);
                        }

                    }
                }

                public void onError(Message message, ErrorCode errorCode) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, errorCode);
                    if (callback != null) {
                        callback.onError(message, errorCode);
                    }

                }

                public void onSuccess(Message message) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, (ErrorCode) null);
                    if (callback != null) {
                        callback.onSuccess(message);
                    }

                }
            };
            RongIMClient.getInstance().sendImageMessage(message, pushContent, pushData, sendMessageCallback);
        }
    }

    public void downloadMedia(ConversationType conversationType, String targetId, MediaType mediaType, String imageUrl, DownloadMediaCallback callback) {
        RongIMClient.getInstance().downloadMedia(conversationType, targetId, mediaType, imageUrl, callback);
    }

    public void downloadMediaMessage(Message message, final IDownloadMediaMessageCallback callback) {
        RongIMClient.getInstance().downloadMediaMessage(message, new IDownloadMediaMessageCallback() {
            public void onSuccess(Message message) {
                EventBus.getDefault().post(new FileMessageEvent(message, 100, 100, (ErrorCode) null));
                if (callback != null) {
                    callback.onSuccess(message);
                }

            }

            public void onProgress(Message message, int progress) {
                EventBus.getDefault().post(new FileMessageEvent(message, progress, 101, (ErrorCode) null));
                if (callback != null) {
                    callback.onProgress(message, progress);
                }

            }

            public void onError(Message message, ErrorCode code) {
                EventBus.getDefault().post(new FileMessageEvent(message, 0, 103, code));
                if (callback != null) {
                    callback.onError(message, code);
                }

            }

            public void onCanceled(Message message) {
                EventBus.getDefault().post(new FileMessageEvent(message, 0, 102, (ErrorCode) null));
                if (callback != null) {
                    callback.onCanceled(message);
                }

            }
        });
    }

    public void downloadMediaFile(final String uid, String fileUrl, String fileName, String path, final IDownloadMediaFileCallback callback) {
        RongIMClient.getInstance().downloadMediaFile(uid, fileUrl, fileName, path, new IDownloadMediaFileCallback() {
            public void onSuccess() {
                EventBus.getDefault().post(new MediaFileEvent(uid, 100, 100, (ErrorCode) null));
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onProgress(int progress) {
                EventBus.getDefault().post(new MediaFileEvent(uid, progress, 101, (ErrorCode) null));
                if (callback != null) {
                    callback.onProgress(progress);
                }

            }

            public void onError(ErrorCode code) {
                EventBus.getDefault().post(new MediaFileEvent(uid, 0, 103, code));
                if (callback != null) {
                    callback.onError(code);
                }

            }

            public void onCanceled() {
                EventBus.getDefault().post(new MediaFileEvent(uid, 0, 102, (ErrorCode) null));
                if (callback != null) {
                    callback.onCanceled();
                }

            }
        });
    }

    public void downloadMedia(String imageUrl, final DownloadMediaCallback callback) {
        ImageLoader.getInstance().loadImage(imageUrl, (ImageSize) null, (DisplayImageOptions) null, new ImageLoadingListener() {
            public void onLoadingStarted(String imageUri, View view) {
            }

            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (callback != null) {
                    callback.onError(ErrorCode.RC_NET_UNAVAILABLE);
                }

            }

            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (callback != null) {
                    callback.onSuccess(imageUri);
                }

            }

            public void onLoadingCancelled(String imageUri, View view) {
            }
        }, new ImageLoadingProgressListener() {
            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                if (callback != null) {
                    callback.onProgress(current * 100 / total);
                }

            }
        });
    }

    public void getConversationNotificationStatus(final ConversationType conversationType, final String targetId, final ResultCallback<ConversationNotificationStatus> callback) {
        ConversationNotificationStatus status = RongContext.getInstance().getConversationNotifyStatusFromCache(ConversationKey.obtain(targetId, conversationType));
        if (status != null) {
            callback.onSuccess(status);
        } else {
            RongIMClient.getInstance().getConversationNotificationStatus(conversationType, targetId, new ResultCallback<ConversationNotificationStatus>() {
                public void onSuccess(ConversationNotificationStatus status) {
                    RongContext.getInstance().setConversationNotifyStatusToCache(ConversationKey.obtain(targetId, conversationType), status);
                    if (callback != null) {
                        callback.onSuccess(status);
                    }

                }

                public void onError(ErrorCode e) {
                    if (callback != null) {
                        callback.onError(e);
                    }

                }
            });
        }

    }

    public void setConversationNotificationStatus(final ConversationType conversationType, final String targetId, final ConversationNotificationStatus notificationStatus, final ResultCallback<ConversationNotificationStatus> callback) {
        RongIMClient.getInstance().setConversationNotificationStatus(conversationType, targetId, notificationStatus, new ResultCallback<ConversationNotificationStatus>() {
            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }

            public void onSuccess(ConversationNotificationStatus status) {
                RongContext.getInstance().setConversationNotifyStatusToCache(ConversationKey.obtain(targetId, conversationType), status);
                RongContext.getInstance().getEventBus().post(new ConversationNotificationEvent(targetId, conversationType, notificationStatus));
                if (callback != null) {
                    callback.onSuccess(status);
                }

            }
        });
    }

    public void syncConversationNotificationStatus(final ConversationType conversationType, final String targetId, final ConversationNotificationStatus notificationStatus, final ResultCallback<Boolean> callback) {
        RongIMClient.getInstance().syncConversationNotificationStatus(conversationType, targetId, notificationStatus, new ResultCallback<Boolean>() {
            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }

            public void onSuccess(Boolean aBoolean) {
                RongContext.getInstance().setConversationNotifyStatusToCache(ConversationKey.obtain(targetId, conversationType), notificationStatus);
                RongContext.getInstance().getEventBus().post(new ConversationNotificationEvent(targetId, conversationType, notificationStatus));
                if (callback != null) {
                    callback.onSuccess(aBoolean);
                }

            }
        });
    }

    public void setDiscussionInviteStatus(final String discussionId, final DiscussionInviteStatus status, final OperationCallback callback) {
        RongIMClient.getInstance().setDiscussionInviteStatus(discussionId, status, new OperationCallback() {
            public void onSuccess() {
                RongContext.getInstance().getEventBus().post(new DiscussionInviteStatusEvent(discussionId, status));
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void syncGroup(final List<Group> groups, final OperationCallback callback) {
        RongIMClient.getInstance().syncGroup(groups, new OperationCallback() {
            public void onSuccess() {
                RongContext.getInstance().getEventBus().post(new SyncGroupEvent(groups));
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void joinGroup(final String groupId, final String groupName, final OperationCallback callback) {
        RongIMClient.getInstance().joinGroup(groupId, groupName, new OperationCallback() {
            public void onSuccess() {
                RongContext.getInstance().getEventBus().post(new JoinGroupEvent(groupId, groupName));
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void quitGroup(final String groupId, final OperationCallback callback) {
        RongIMClient.getInstance().quitGroup(groupId, new OperationCallback() {
            public void onSuccess() {
                RongContext.getInstance().getEventBus().post(new QuitGroupEvent(groupId));
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    public String getCurrentUserId() {
        return RongIMClient.getInstance().getCurrentUserId();
    }

    public long getDeltaTime() {
        return RongIMClient.getInstance().getDeltaTime();
    }

    public void joinChatRoom(final String chatroomId, final int defMessageCount, final OperationCallback callback) {
        RongIMClient.getInstance().joinChatRoom(chatroomId, defMessageCount, new OperationCallback() {
            public void onSuccess() {
                RongContext.getInstance().getEventBus().post(new JoinChatRoomEvent(chatroomId, defMessageCount));
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    public void joinExistChatRoom(final String chatroomId, final int defMessageCount, final OperationCallback callback) {
        RongIMClient.getInstance().joinExistChatRoom(chatroomId, defMessageCount, new OperationCallback() {
            public void onSuccess() {
                RongContext.getInstance().getEventBus().post(new JoinChatRoomEvent(chatroomId, defMessageCount));
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    public void quitChatRoom(final String chatroomId, final OperationCallback callback) {
        RongIMClient.getInstance().quitChatRoom(chatroomId, new OperationCallback() {
            public void onSuccess() {
                RongContext.getInstance().getEventBus().post(new QuitChatRoomEvent(chatroomId));
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    public void clearConversations(final ResultCallback callback, final ConversationType... conversationTypes) {
        RongIMClient.getInstance().clearConversations(new ResultCallback() {
            public void onSuccess(Object o) {
                RongContext.getInstance().getEventBus().post(ClearConversationEvent.obtain(conversationTypes));
                if (callback != null) {
                    callback.onSuccess(o);
                }

            }

            public void onError(ErrorCode e) {
                if (callback != null) {
                    callback.onError(e);
                }

            }
        }, conversationTypes);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean clearConversations(ConversationType... conversationTypes) {
        return RongIMClient.getInstance().clearConversations(conversationTypes);
    }

    public void addToBlacklist(final String userId, final OperationCallback callback) {
        RongIMClient.getInstance().addToBlacklist(userId, new OperationCallback() {
            public void onSuccess() {
                RongContext.getInstance().getEventBus().post(new AddToBlacklistEvent(userId));
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    public void removeFromBlacklist(final String userId, final OperationCallback callback) {
        RongIMClient.getInstance().removeFromBlacklist(userId, new OperationCallback() {
            public void onSuccess() {
                RongContext.getInstance().getEventBus().post(new RemoveFromBlacklistEvent(userId));
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    public void getBlacklistStatus(String userId, ResultCallback<BlacklistStatus> callback) {
        RongIMClient.getInstance().getBlacklistStatus(userId, callback);
    }

    public void getBlacklist(GetBlacklistCallback callback) {
        RongIMClient.getInstance().getBlacklist(callback);
    }

    public void setNotificationQuietHours(final String startTime, final int spanMinutes, final OperationCallback callback) {
        RongIMClient.getInstance().setNotificationQuietHours(startTime, spanMinutes, new OperationCallback() {
            public void onSuccess() {
                MessageNotificationManager.getInstance().setNotificationQuietHours(startTime, spanMinutes);
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    public void removeNotificationQuietHours(final OperationCallback callback) {
        RongIMClient.getInstance().removeNotificationQuietHours(new OperationCallback() {
            public void onSuccess() {
                MessageNotificationManager.getInstance().setNotificationQuietHours((String) null, 0);
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    public void getNotificationQuietHours(final GetNotificationQuietHoursCallback callback) {
        RongIMClient.getInstance().getNotificationQuietHours(new GetNotificationQuietHoursCallback() {
            public void onSuccess(String startTime, int spanMinutes) {
                MessageNotificationManager.getInstance().setNotificationQuietHours(startTime, spanMinutes);
                io.rong.imkit.RongIM.notificationQuiteHoursConfigured = true;
                if (callback != null) {
                    callback.onSuccess(startTime, spanMinutes);
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    public void getPublicServiceProfile(PublicServiceType publicServiceType, String publicServiceId, ResultCallback<PublicServiceProfile> callback) {
        RongIMClient.getInstance().getPublicServiceProfile(publicServiceType, publicServiceId, callback);
    }

    public void searchPublicService(SearchType searchType, String keywords, ResultCallback<PublicServiceProfileList> callback) {
        RongIMClient.getInstance().searchPublicService(searchType, keywords, callback);
    }

    public void searchPublicServiceByType(PublicServiceType publicServiceType, SearchType searchType, String keywords, ResultCallback<PublicServiceProfileList> callback) {
        RongIMClient.getInstance().searchPublicServiceByType(publicServiceType, searchType, keywords, callback);
    }

    public void subscribePublicService(PublicServiceType publicServiceType, String publicServiceId, OperationCallback callback) {
        RongIMClient.getInstance().subscribePublicService(publicServiceType, publicServiceId, callback);
    }

    public void unsubscribePublicService(PublicServiceType publicServiceType, String publicServiceId, OperationCallback callback) {
        RongIMClient.getInstance().unsubscribePublicService(publicServiceType, publicServiceId, callback);
    }

    public void getPublicServiceList(ResultCallback<PublicServiceProfileList> callback) {
        RongIMClient.getInstance().getPublicServiceList(callback);
    }

    public void syncUserData(UserData userData, final OperationCallback callback) {
        RongIMClient.getInstance().syncUserData(userData, new OperationCallback() {
            public void onSuccess() {
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            public void onError(ErrorCode errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }

            }
        });
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setRequestPermissionListener(io.rong.imkit.RongIM.RequestPermissionsListener listener) {
        RongContext.getInstance().setRequestPermissionListener(listener);
    }

    public void recordNotificationEvent(String pushId) {
        RongPushClient.recordNotificationEvent(pushId);
    }

    private MessageContent setMessageAttachedUserInfo(MessageContent content) {
        if (RongContext.getInstance().getUserInfoAttachedState() && content.getUserInfo() == null) {
            String userId = getInstance().getCurrentUserId();
            UserInfo info = RongContext.getInstance().getCurrentUserInfo();
            if (info == null) {
                info = RongUserInfoManager.getInstance().getUserInfo(userId);
            }

            if (info != null) {
                content.setUserInfo(info);
            }
        }

        return content;
    }

    private Message filterSendMessage(ConversationType conversationType, String targetId, MessageContent messageContent) {
        Message message = new Message();
        message.setConversationType(conversationType);
        message.setTargetId(targetId);
        message.setContent(messageContent);
        if (RongContext.getInstance().getOnSendMessageListener() != null) {
            message = RongContext.getInstance().getOnSendMessageListener().onSend(message);
        }

        return message;
    }

    private Message filterSendMessage(Message message) {
        if (RongContext.getInstance().getOnSendMessageListener() != null) {
            message = RongContext.getInstance().getOnSendMessageListener().onSend(message);
        }

        return message;
    }

    private void filterSentMessage(Message message, ErrorCode errorCode) {
        io.rong.imkit.RongIM.SentMessageErrorCode sentMessageErrorCode = null;
        boolean isExecute = false;
        if (RongContext.getInstance().getOnSendMessageListener() != null) {
            if (errorCode != null) {
                sentMessageErrorCode = io.rong.imkit.RongIM.SentMessageErrorCode.setValue(errorCode.getValue());
            }

            isExecute = RongContext.getInstance().getOnSendMessageListener().onSent(message, sentMessageErrorCode);
        }

        MessageContent content;
        MessageTag tag;
        if (errorCode != null && !isExecute) {
            if (errorCode.equals(ErrorCode.NOT_IN_DISCUSSION) || errorCode.equals(ErrorCode.NOT_IN_GROUP) || errorCode.equals(ErrorCode.NOT_IN_CHATROOM) || errorCode.equals(ErrorCode.REJECTED_BY_BLACKLIST) || errorCode.equals(ErrorCode.FORBIDDEN_IN_GROUP) || errorCode.equals(ErrorCode.FORBIDDEN_IN_CHATROOM) || errorCode.equals(ErrorCode.KICKED_FROM_CHATROOM)) {
                InformationNotificationMessage informationMessage = null;
                if (errorCode.equals(ErrorCode.NOT_IN_DISCUSSION)) {
                    informationMessage = InformationNotificationMessage.obtain(mContext.getString(R.string.rc_info_not_in_discussion));
                } else if (errorCode.equals(ErrorCode.NOT_IN_GROUP)) {
                    informationMessage = InformationNotificationMessage.obtain(mContext.getString(R.string.rc_info_not_in_group));
                } else if (errorCode.equals(ErrorCode.NOT_IN_CHATROOM)) {
                    informationMessage = InformationNotificationMessage.obtain(mContext.getString(R.string.rc_info_not_in_chatroom));
                } else if (errorCode.equals(ErrorCode.REJECTED_BY_BLACKLIST)) {
                    informationMessage = InformationNotificationMessage.obtain(mContext.getString(R.string.rc_rejected_by_blacklist_prompt));
                } else if (errorCode.equals(ErrorCode.FORBIDDEN_IN_GROUP)) {
                    informationMessage = InformationNotificationMessage.obtain(mContext.getString(R.string.rc_info_forbidden_to_talk));
                } else if (errorCode.equals(ErrorCode.FORBIDDEN_IN_CHATROOM)) {
                    informationMessage = InformationNotificationMessage.obtain(mContext.getString(R.string.rc_forbidden_in_chatroom));
                } else if (errorCode.equals(ErrorCode.KICKED_FROM_CHATROOM)) {
                    informationMessage = InformationNotificationMessage.obtain(mContext.getString(R.string.rc_kicked_from_chatroom));
                }

                this.insertMessage(message.getConversationType(), message.getTargetId(), message.getSenderUserId(), informationMessage, (ResultCallback) null);
            }

            content = message.getContent();
            if (content == null) {
                RLog.e(TAG, "filterSentMessage content is null");
                return;
            }

            tag = (MessageTag) content.getClass().getAnnotation(MessageTag.class);
            if (tag != null && (tag.flag() & 1) == 1) {
                RongContext.getInstance().getEventBus().post(new OnMessageSendErrorEvent(message, errorCode));
            }
        } else if (message != null) {
            content = message.getContent();
            tag = (MessageTag) content.getClass().getAnnotation(MessageTag.class);
            if (tag != null && (tag.flag() & 1) == 1) {
                RongContext.getInstance().getEventBus().post(message);
            }
        }

    }

    public static void setServerInfo(String naviServer, String fileServer) {
        if (TextUtils.isEmpty(naviServer)) {
            RLog.e(TAG, "setServerInfo naviServer should not be null.");
            throw new IllegalArgumentException("naviServer should not be null.");
        } else {
            RongIMClient.setServerInfo(naviServer, fileServer);
        }
    }

    public static void setStatisticDomain(String domain) {
        RongIMClient.setStatisticDomain(domain);
    }

    public void setPublicServiceMenuClickListener(IPublicServiceMenuClickListener menuClickListener) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().setPublicServiceMenuClickListener(menuClickListener);
        }

    }

    /**
     * @deprecated
     */
    @Deprecated
    public void recallMessage(Message message) {
        this.recallMessage(message, "");
    }

    public void recallMessage(final Message message, String pushContent) {
        RongIMClient.getInstance().recallMessage(message, pushContent, new ResultCallback<RecallNotificationMessage>() {
            public void onSuccess(RecallNotificationMessage recallNotificationMessage) {
                RongContext.getInstance().getEventBus().post(new MessageRecallEvent(message.getMessageId(), recallNotificationMessage, true));
            }

            public void onError(ErrorCode errorCode) {
                RLog.d(io.rong.imkit.RongIM.TAG, "recallMessage errorCode = " + errorCode.getValue());
            }
        });
    }

    public void sendMediaMessage(Message message, String pushContent, String pushData, final ISendMediaMessageCallback callback) {
        Message temp = this.filterSendMessage(message);
        if (temp != null) {
            if (temp != message) {
                message = temp;
            }

            this.setMessageAttachedUserInfo(message.getContent());
            final Result<OnReceiveMessageProgressEvent> result = new Result();
            result.t = new OnReceiveMessageProgressEvent();
            ISendMediaMessageCallback sendMessageCallback = new ISendMediaMessageCallback() {
                public void onProgress(Message message, int progress) {
                    if (result.t != null) {
                        ((OnReceiveMessageProgressEvent) result.t).setMessage(message);
                        ((OnReceiveMessageProgressEvent) result.t).setProgress(progress);
                        RongContext.getInstance().getEventBus().post(result.t);
                        if (callback != null) {
                            callback.onProgress(message, progress);
                        }

                    }
                }

                public void onAttached(Message message) {
                    RongContext.getInstance().getEventBus().post(message);
                    if (callback != null) {
                        callback.onAttached(message);
                    }

                }

                public void onSuccess(Message message) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, (ErrorCode) null);
                    if (callback != null) {
                        callback.onSuccess(message);
                    }

                }

                public void onError(Message message, ErrorCode errorCode) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, errorCode);
                    if (callback != null) {
                        callback.onError(message, errorCode);
                    }

                }

                public void onCanceled(Message message) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, (ErrorCode) null);
                    if (callback != null) {
                        callback.onCanceled(message);
                    }

                }
            };
            RongIMClient.getInstance().sendMediaMessage(message, pushContent, pushData, sendMessageCallback);
        }
    }

    public void sendDirectionalMediaMessage(Message message, String[] userIds, String pushContent, String pushData, final ISendMediaMessageCallback callback) {
        Message temp = this.filterSendMessage(message);
        if (temp != null) {
            if (temp != message) {
                message = temp;
            }

            this.setMessageAttachedUserInfo(message.getContent());
            final Result<OnReceiveMessageProgressEvent> result = new Result();
            result.t = new OnReceiveMessageProgressEvent();
            ISendMediaMessageCallback sendMessageCallback = new ISendMediaMessageCallback() {
                public void onProgress(Message message, int progress) {
                    if (result.t != null) {
                        ((OnReceiveMessageProgressEvent) result.t).setMessage(message);
                        ((OnReceiveMessageProgressEvent) result.t).setProgress(progress);
                        RongContext.getInstance().getEventBus().post(result.t);
                        if (callback != null) {
                            callback.onProgress(message, progress);
                        }

                    }
                }

                public void onAttached(Message message) {
                    RongContext.getInstance().getEventBus().post(message);
                    if (callback != null) {
                        callback.onAttached(message);
                    }

                }

                public void onSuccess(Message message) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, (ErrorCode) null);
                    if (callback != null) {
                        callback.onSuccess(message);
                    }

                }

                public void onError(Message message, ErrorCode errorCode) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, errorCode);
                    if (callback != null) {
                        callback.onError(message, errorCode);
                    }

                }

                public void onCanceled(Message message) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, (ErrorCode) null);
                    if (callback != null) {
                        callback.onCanceled(message);
                    }

                }
            };
            RongIMClient.getInstance().sendDirectionalMediaMessage(message, userIds, pushContent, pushData, sendMessageCallback);
        }
    }

    public void sendMediaMessage(Message message, String pushContent, String pushData, final ISendMediaMessageCallbackWithUploader callback) {
        Message temp = this.filterSendMessage(message);
        if (temp != null) {
            if (temp != message) {
                message = temp;
            }

            this.setMessageAttachedUserInfo(message.getContent());
            ISendMediaMessageCallbackWithUploader sendMediaMessageCallbackWithUploader = new ISendMediaMessageCallbackWithUploader() {
                public void onAttached(Message message, MediaMessageUploader uploader) {
                    MessageTag tag = (MessageTag) message.getContent().getClass().getAnnotation(MessageTag.class);
                    if (tag != null && (tag.flag() & 1) == 1) {
                        RongContext.getInstance().getEventBus().post(message);
                    }

                    if (callback != null) {
                        callback.onAttached(message, uploader);
                    }

                }

                public void onProgress(Message message, int progress) {
                    Result<OnReceiveMessageProgressEvent> result = new Result();
                    result.t = new OnReceiveMessageProgressEvent();
                    ((OnReceiveMessageProgressEvent) result.t).setMessage(message);
                    ((OnReceiveMessageProgressEvent) result.t).setProgress(progress);
                    RongContext.getInstance().getEventBus().post(result.t);
                    if (callback != null) {
                        callback.onProgress(message, progress);
                    }

                }

                public void onSuccess(Message message) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, (ErrorCode) null);
                    if (callback != null) {
                        callback.onSuccess(message);
                    }

                }

                public void onError(Message message, ErrorCode errorCode) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, errorCode);
                    if (callback != null) {
                        callback.onError(message, errorCode);
                    }

                }

                public void onCanceled(Message message) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, (ErrorCode) null);
                    if (callback != null) {
                        callback.onCanceled(message);
                    }

                }
            };
            RongIMClient.getInstance().sendMediaMessage(message, pushContent, pushData, sendMediaMessageCallbackWithUploader);
        }
    }

    public void cancelDownloadMediaMessage(Message message, OperationCallback callback) {
        RongIMClient.getInstance().cancelDownloadMediaMessage(message, callback);
    }

    public void pauseDownloadMediaMessage(Message message, OperationCallback callback) {
        RongIMClient.getInstance().pauseDownloadMediaMessage(message, callback);
    }

    public void cancelSendMediaMessage(Message message, OperationCallback callback) {
        RongIMClient.getInstance().cancelSendMediaMessage(message, callback);
    }

    public void setReadReceiptConversationTypeList(ConversationType... types) {
        if (RongContext.getInstance() != null) {
            RongContext.getInstance().setReadReceiptConversationTypeList(types);
        }

    }

    public void sendDirectionalMessage(ConversationType type, String targetId, MessageContent content, String[] userIds, String pushContent, String pushData, final ISendMessageCallback callback) {
        Message message = Message.obtain(targetId, type, content);
        Message filterMsg = this.filterSendMessage(message);
        if (filterMsg == null) {
            RLog.w(TAG, "sendDirectionalMessage: 因在 onSend 中消息被过滤为 null，取消发送。");
        } else {
            if (filterMsg != message) {
                message = filterMsg;
            }

            message.setContent(this.setMessageAttachedUserInfo(message.getContent()));
            RongIMClient.getInstance().sendDirectionalMessage(type, targetId, content, userIds, pushContent, pushData, new ISendMessageCallback() {
                public void onAttached(Message message) {
                    MessageTag tag = (MessageTag) message.getContent().getClass().getAnnotation(MessageTag.class);
                    if (tag != null && (tag.flag() & 1) == 1) {
                        RongContext.getInstance().getEventBus().post(message);
                    }

                    if (callback != null) {
                        callback.onAttached(message);
                    }

                }

                public void onSuccess(Message message) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, (ErrorCode) null);
                    if (callback != null) {
                        callback.onSuccess(message);
                    }

                }

                public void onError(Message message, ErrorCode errorCode) {
                    io.rong.imkit.RongIM.this.filterSentMessage(message, errorCode);
                    if (callback != null) {
                        callback.onError(message, errorCode);
                    }

                }
            });
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setNotificationQuiteHoursConfigured(boolean notificationQuiteHoursConfigured) {
        io.rong.imkit.RongIM var10000 = io.rong.imkit.RongIM.SingletonHolder.sRongIM;
        notificationQuiteHoursConfigured = notificationQuiteHoursConfigured;
    }

    public boolean isNotificationQuiteHoursConfigured() {
        return notificationQuiteHoursConfigured;
    }

    public void setMessageInterceptor(io.rong.imkit.RongIM.MessageInterceptor messageInterceptor) {
        messageInterceptor = messageInterceptor;
    }

    public void supportResumeBrokenTransfer(String url, final ResultCallback<Boolean> callback) {
        RongIMClient.getInstance().supportResumeBrokenTransfer(url, new ResultCallback<Boolean>() {
            public void onSuccess(Boolean aBoolean) {
                if (callback != null) {
                    callback.onSuccess(aBoolean);
                }

            }

            public void onError(ErrorCode e) {
                if (callback != null) {
                    callback.onError(e);
                }

            }
        });
    }

    public interface MessageInterceptor {
        boolean intercept(Message var1);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public interface RequestPermissionsListener {
        /**
         * @deprecated
         */
        @Deprecated
        void onPermissionRequest(String[] var1, int var2);
    }

    public interface OnReceiveUnreadCountChangedListener {
        void onMessageIncreased(int var1);
    }

    public static enum SentMessageErrorCode {
        UNKNOWN(-1, "Unknown error."),
        NOT_IN_DISCUSSION(21406, "not_in_discussion"),
        NOT_IN_GROUP(22406, "not_in_group"),
        FORBIDDEN_IN_GROUP(22408, "forbidden_in_group"),
        NOT_IN_CHATROOM(23406, "not_in_chatroom"),
        REJECTED_BY_BLACKLIST(405, "rejected by blacklist"),
        NOT_FOLLOWED(29106, "not followed");

        private int code;
        private String msg;

        private SentMessageErrorCode(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getValue() {
            return this.code;
        }

        public String getMessage() {
            return this.msg;
        }

        public static io.rong.imkit.RongIM.SentMessageErrorCode setValue(int code) {
            io.rong.imkit.RongIM.SentMessageErrorCode[] var1 = values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                io.rong.imkit.RongIM.SentMessageErrorCode c = var1[var3];
                if (code == c.getValue()) {
                    return c;
                }
            }

            RLog.d("RongIMClient", "SentMessageErrorCode---ErrorCode---code:" + code);
            return UNKNOWN;
        }
    }

    public interface OnSendMessageListener {
        Message onSend(Message var1);

        boolean onSent(Message var1, io.rong.imkit.RongIM.SentMessageErrorCode var2);
    }

    public interface OnSelectMemberListener {
        void startSelectMember(Context var1, ConversationType var2, String var3);
    }

    public interface PublicServiceProfileProvider {
        PublicServiceProfile getPublicServiceProfile(PublicServiceType var1, String var2);
    }

    public interface GroupInfoProvider {
        Group getGroupInfo(String var1);
    }

    public interface GroupUserInfoProvider {
        GroupUserInfo getGroupUserInfo(String var1, String var2);
    }

    public interface UserInfoProvider {
        UserInfo getUserInfo(String var1);
    }

    public interface ConversationListBehaviorListener {
        boolean onConversationPortraitClick(Context var1, ConversationType var2, String var3);

        boolean onConversationPortraitLongClick(Context var1, ConversationType var2, String var3);

        boolean onConversationLongClick(Context var1, View var2, UIConversation var3);

        boolean onConversationClick(Context var1, View var2, UIConversation var3);
    }

    public interface ConversationClickListener {
        boolean onUserPortraitClick(Context var1, ConversationType var2, UserInfo var3, String var4);

        boolean onUserPortraitLongClick(Context var1, ConversationType var2, UserInfo var3, String var4);

        boolean onMessageClick(Context var1, View var2, Message var3);

        boolean onMessageLinkClick(Context var1, String var2, Message var3);

        boolean onMessageLongClick(Context var1, View var2, Message var3);
    }

    /**
     * @deprecated
     */
    public interface ConversationBehaviorListener {
        boolean onUserPortraitClick(Context var1, ConversationType var2, UserInfo var3);

        boolean onUserPortraitLongClick(Context var1, ConversationType var2, UserInfo var3);

        boolean onMessageClick(Context var1, View var2, Message var3);

        boolean onMessageLinkClick(Context var1, String var2);

        boolean onMessageLongClick(Context var1, View var2, Message var3);
    }

    public interface PublicServiceBehaviorListener {
        boolean onFollowClick(Context var1, PublicServiceProfile var2);

        boolean onUnFollowClick(Context var1, PublicServiceProfile var2);

        boolean onEnterConversationClick(Context var1, PublicServiceProfile var2);
    }

    public interface IGroupMemberCallback {
        void onGetGroupMembersResult(List<UserInfo> var1);
    }

    public interface IGroupMembersProvider {
        void getGroupMembers(String var1, io.rong.imkit.RongIM.IGroupMemberCallback var2);
    }

    static class SingletonHolder {
        static io.rong.imkit.RongIM sRongIM = new io.rong.imkit.RongIM();

        SingletonHolder() {
        }
    }
}
