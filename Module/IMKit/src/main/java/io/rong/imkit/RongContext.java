//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.rong.common.FileUtils;
import io.rong.common.RLog;
import io.rong.eventbus.EventBus;
import io.rong.imageloader.cache.disc.impl.ext.LruDiskCache;
import io.rong.imageloader.cache.disc.naming.Md5FileNameGenerator;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imageloader.core.ImageLoaderConfiguration;
import io.rong.imageloader.core.ImageLoaderConfiguration.Builder;
import io.rong.imageloader.core.download.ImageDownloader;
import io.rong.imageloader.utils.L;
import io.rong.imageloader.utils.StorageUtils;
import io.rong.imkit.RongIM.ConversationBehaviorListener;
import io.rong.imkit.RongIM.ConversationClickListener;
import io.rong.imkit.RongIM.ConversationListBehaviorListener;
import io.rong.imkit.RongIM.GroupInfoProvider;
import io.rong.imkit.RongIM.GroupUserInfoProvider;
import io.rong.imkit.RongIM.OnSelectMemberListener;
import io.rong.imkit.RongIM.OnSendMessageListener;
import io.rong.imkit.RongIM.PublicServiceBehaviorListener;
import io.rong.imkit.RongIM.PublicServiceProfileProvider;
import io.rong.imkit.RongIM.RequestPermissionsListener;
import io.rong.imkit.RongIM.UserInfoProvider;
import io.rong.imkit.cache.RongCache;
import io.rong.imkit.cache.RongCacheWrap;
import io.rong.imkit.model.ConversationInfo;
import io.rong.imkit.model.ConversationKey;
import io.rong.imkit.model.ConversationProviderTag;
import io.rong.imkit.model.Event.ConversationNotificationEvent;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.utils.RongAuthImageDownloader;
import io.rong.imkit.utils.StringUtils;
import io.rong.imkit.widget.provider.AppServiceConversationProvider;
import io.rong.imkit.widget.provider.CustomerServiceConversationProvider;
import io.rong.imkit.widget.provider.DiscussionConversationProvider;
import io.rong.imkit.widget.provider.EvaluateTextMessageItemProvider;
import io.rong.imkit.widget.provider.GroupConversationProvider;
import io.rong.imkit.widget.provider.IContainerItemProvider.ConversationProvider;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imkit.widget.provider.PrivateConversationProvider;
import io.rong.imkit.widget.provider.PublicServiceConversationProvider;
import io.rong.imkit.widget.provider.SystemConversationProvider;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.common.WeakValueHashMap;
import io.rong.imlib.model.Conversation.ConversationNotificationStatus;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Conversation.PublicServiceType;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.PublicServiceProfile;
import io.rong.imlib.model.UserInfo;
import io.rong.push.RongPushClient;

public class RongContext extends ContextWrapper {
    private static final String TAG = "RongContext";
    private static final int NOTIFICATION_CACHE_MAX_COUNT = 64;
    private static io.rong.imkit.RongContext sContext;
    private EventBus mBus = EventBus.getDefault();
    private ExecutorService executorService;
    private ConversationBehaviorListener mConversationBehaviorListener;
    private ConversationClickListener mConversationClickListener;
    private ConversationListBehaviorListener mConversationListBehaviorListener;
    private PublicServiceBehaviorListener mPublicServiceBehaviorListener;
    private OnSelectMemberListener mMemberSelectListener;
    private OnSendMessageListener mOnSendMessageListener;
    private RequestPermissionsListener mRequestPermissionsListener;
    private IPublicServiceMenuClickListener mPublicServiceMenuClickListener;
    private UserInfoProvider mUserInfoProvider;
    private GroupInfoProvider mGroupProvider;
    private GroupUserInfoProvider mGroupUserInfoProvider;
    private PublicServiceProfileProvider mPublicServiceProfileProvider;
    private Map<Class<? extends MessageContent>, MessageProvider> mTemplateMap = new HashMap();
    private Map<Class<? extends MessageContent>, MessageProvider> mWeakTemplateMap = new WeakValueHashMap();
    private Map<Class<? extends MessageContent>, ProviderTag> mProviderMap = new HashMap();
    private Map<String, ConversationProvider> mConversationProviderMap = new HashMap();
    private Map<String, ConversationProviderTag> mConversationTagMap = new HashMap();
    private RongCache<String, ConversationNotificationStatus> mNotificationCache;
    private List<ConversationType> mReadReceiptConversationTypeList = new ArrayList();

    private List<String> mCurrentConversationList = new ArrayList();
    Handler mHandler = new Handler(this.getMainLooper());
    private UserInfo mCurrentUserInfo;
    private boolean isUserInfoAttached;
    private boolean isShowUnreadMessageState;
    private boolean isShowNewMessageState;
    private EvaluateTextMessageItemProvider evaluateTextMessageItemProvider;
    private Uri notificationSound;
    private static ImageDownloader imageDownloader;

    public static void init(Context context) {
        if (sContext == null) {
            sContext = new io.rong.imkit.RongContext(context);
            sContext.initRegister();
        }

    }

    public static void setImageLoaderDownloader(ImageDownloader imageLoaderDownloader) {
        imageDownloader = imageLoaderDownloader;
    }

    public static io.rong.imkit.RongContext getInstance() {
        return sContext;
    }

    protected RongContext(Context base) {
        super(base);
        this.mReadReceiptConversationTypeList.add(ConversationType.PRIVATE);
        this.initCache();
        this.executorService = Executors.newSingleThreadExecutor();
        RongNotificationManager.getInstance().init(this);
        ImageLoader.getInstance().init(this.getDefaultConfig(this.getApplicationContext()));
    }

    private ImageLoaderConfiguration getDefaultConfig(Context context) {
        String path = FileUtils.getInternalCachePath(context, "image");
        File cacheDir;
        if (TextUtils.isEmpty(path)) {
            cacheDir = StorageUtils.getOwnCacheDirectory(context, context.getPackageName() + "/cache/image/");
        } else {
            cacheDir = new File(path);
        }

        ImageLoaderConfiguration config;
        try {
            Builder builder = (new Builder(context)).threadPoolSize(3).threadPriority(3).denyCacheImageMultipleSizesInMemory().diskCache(new LruDiskCache(cacheDir, new Md5FileNameGenerator(), 0L)).defaultDisplayImageOptions(DisplayImageOptions.createSimple());
            if (imageDownloader != null) {
                builder.imageDownloader(imageDownloader);
            } else {
                builder.imageDownloader(new RongAuthImageDownloader(this));
            }

            config = builder.build();
            L.writeLogs(false);
            return config;
        } catch (IOException var6) {
            RLog.i("RongContext", "Use default ImageLoader config.");
            config = ImageLoaderConfiguration.createDefault(context);
            return config;
        }
    }

    private void initRegister() {
        this.registerConversationTemplate(new PrivateConversationProvider());
        this.registerConversationTemplate(new GroupConversationProvider());
        this.registerConversationTemplate(new DiscussionConversationProvider());
        this.registerConversationTemplate(new SystemConversationProvider());
        this.registerConversationTemplate(new CustomerServiceConversationProvider());
        this.registerConversationTemplate(new AppServiceConversationProvider());
        this.registerConversationTemplate(new PublicServiceConversationProvider());
    }

    private void initCache() {
        this.mNotificationCache = new RongCacheWrap<String, ConversationNotificationStatus>(this, 64) {
            Vector<String> mRequests = new Vector();

            public ConversationNotificationStatus obtainValue(final String key) {
                if (TextUtils.isEmpty(key)) {
                    return null;
                } else if (this.mRequests.contains(key)) {
                    return null;
                } else {
                    this.mRequests.add(key);
                    io.rong.imkit.RongContext.this.mHandler.post(new Runnable() {
                        public void run() {
                            final ConversationKey conversationKey = ConversationKey.obtain(key);
                            if (conversationKey != null) {
                                RongIM.getInstance().getConversationNotificationStatus(conversationKey.getType(), conversationKey.getTargetId(), new ResultCallback<ConversationNotificationStatus>() {
                                    public void onSuccess(ConversationNotificationStatus status) {
                                        mRequests.remove(key);
                                        put(key, status);
                                        getContext().getEventBus().post(new ConversationNotificationEvent(conversationKey.getTargetId(), conversationKey.getType(), status));
                                    }

                                    public void onError(ErrorCode errorCode) {
                                        mRequests.remove(key);
                                    }
                                });
                            }

                        }
                    });
                    return null;
                }
            }
        };
    }

    public List<ConversationInfo> getCurrentConversationList() {
        ArrayList<ConversationInfo> infos = new ArrayList();
        int size = this.mCurrentConversationList.size();
        if (size > 0) {
            for (int i = 0; i < size; ++i) {
                ConversationKey key = ConversationKey.obtain((String) this.mCurrentConversationList.get(i));
                ConversationInfo info = ConversationInfo.obtain(key.getType(), key.getTargetId());
                infos.add(info);
            }
        }

        return infos;
    }

    public EventBus getEventBus() {
        return this.mBus;
    }

    public void registerConversationTemplate(ConversationProvider provider) {
        ConversationProviderTag tag = (ConversationProviderTag) provider.getClass().getAnnotation(ConversationProviderTag.class);
        if (tag == null) {
            throw new RuntimeException("No ConversationProviderTag added with your provider!");
        } else {
            this.mConversationProviderMap.put(tag.conversationType(), provider);
            this.mConversationTagMap.put(tag.conversationType(), tag);
        }
    }

    public ConversationProvider getConversationTemplate(String conversationType) {
        return (ConversationProvider) this.mConversationProviderMap.get(conversationType);
    }

    public ConversationProviderTag getConversationProviderTag(String conversationType) {
        if (!this.mConversationProviderMap.containsKey(conversationType)) {
            throw new RuntimeException("the conversation type hasn't been registered!");
        } else {
            return (ConversationProviderTag) this.mConversationTagMap.get(conversationType);
        }
    }

    public void registerMessageTemplate(MessageProvider provider) {
        ProviderTag tag = (ProviderTag) provider.getClass().getAnnotation(ProviderTag.class);
        if (tag == null) {
            throw new RuntimeException("ProviderTag not def MessageContent type");
        } else {
            this.mTemplateMap.put(tag.messageContent(), provider);
            this.mProviderMap.put(tag.messageContent(), tag);
        }
    }

    public MessageProvider getMessageTemplate(Class<? extends MessageContent> type) {
        MessageProvider provider = (MessageProvider) this.mWeakTemplateMap.get(type);
        if (provider == null) {
            try {
                if (this.mTemplateMap != null && this.mTemplateMap.get(type) != null) {
                    provider = (MessageProvider) ((MessageProvider) this.mTemplateMap.get(type)).clone();
                    this.mWeakTemplateMap.put(type, provider);
                } else {
                    RLog.e("RongContext", "The template of message can't be null. type :" + type);
                }
            } catch (CloneNotSupportedException var4) {
                var4.printStackTrace();
            }
        }

        return provider;
    }

    public ProviderTag getMessageProviderTag(Class<? extends MessageContent> type) {
        return (ProviderTag) this.mProviderMap.get(type);
    }

    public EvaluateTextMessageItemProvider getEvaluateProvider() {
        if (this.evaluateTextMessageItemProvider == null) {
            this.evaluateTextMessageItemProvider = new EvaluateTextMessageItemProvider();
        }

        return this.evaluateTextMessageItemProvider;
    }

    public void executorBackground(Runnable runnable) {
        if (runnable != null) {
            this.executorService.execute(runnable);
        }
    }

    public UserInfo getUserInfoFromCache(String userId) {
        return userId != null ? RongUserInfoManager.getInstance().getUserInfo(userId) : null;
    }

    public Group getGroupInfoFromCache(String groupId) {
        return groupId != null ? RongUserInfoManager.getInstance().getGroupInfo(groupId) : null;
    }

    public GroupUserInfo getGroupUserInfoFromCache(String groupId, String userId) {
        return RongUserInfoManager.getInstance().getGroupUserInfo(groupId, userId);
    }

    public Discussion getDiscussionInfoFromCache(String discussionId) {
        return RongUserInfoManager.getInstance().getDiscussionInfo(discussionId);
    }

    public PublicServiceProfile getPublicServiceInfoFromCache(String messageKey) {
        String id = StringUtils.getArg1(messageKey);
        String arg2 = StringUtils.getArg2(messageKey);
        int iArg2 = Integer.parseInt(arg2);
        PublicServiceType type = null;
        if (iArg2 == PublicServiceType.PUBLIC_SERVICE.getValue()) {
            type = PublicServiceType.PUBLIC_SERVICE;
        } else if (iArg2 == PublicServiceType.APP_PUBLIC_SERVICE.getValue()) {
            type = PublicServiceType.APP_PUBLIC_SERVICE;
        }

        return RongUserInfoManager.getInstance().getPublicServiceProfile(type, id);
    }

    public ConversationNotificationStatus getConversationNotifyStatusFromCache(ConversationKey key) {
        ConversationNotificationStatus status = null;
        if (key != null && key.getKey() != null) {
            status = (ConversationNotificationStatus) this.mNotificationCache.get(key.getKey());
        }

        return status;
    }

    public void setConversationNotifyStatusToCache(ConversationKey conversationKey, ConversationNotificationStatus status) {
        this.mNotificationCache.put(conversationKey.getKey(), status);
    }

    public void removeConversationNotifyStatusFromCache(ConversationKey conversationKey) {
        this.mNotificationCache.remove(conversationKey.getKey());
    }

    public void clearConversationNotifyStatusCache() {
        this.mNotificationCache.clear();
    }

    public ConversationBehaviorListener getConversationBehaviorListener() {
        return this.mConversationBehaviorListener;
    }

    public void setConversationBehaviorListener(ConversationBehaviorListener conversationBehaviorListener) {
        this.mConversationBehaviorListener = conversationBehaviorListener;
    }

    public ConversationClickListener getConversationClickListener() {
        return this.mConversationClickListener;
    }

    public void setConversationClickListener(ConversationClickListener conversationClickListener) {
        this.mConversationClickListener = conversationClickListener;
    }

    public PublicServiceBehaviorListener getPublicServiceBehaviorListener() {
        return this.mPublicServiceBehaviorListener;
    }

    public void setPublicServiceBehaviorListener(PublicServiceBehaviorListener publicServiceBehaviorListener) {
        this.mPublicServiceBehaviorListener = publicServiceBehaviorListener;
    }

    public void setOnMemberSelectListener(OnSelectMemberListener listener) {
        this.mMemberSelectListener = listener;
    }

    public OnSelectMemberListener getMemberSelectListener() {
        return this.mMemberSelectListener;
    }

    public void setGetUserInfoProvider(UserInfoProvider provider, boolean isCache) {
        this.mUserInfoProvider = provider;
        RongUserInfoManager.getInstance().setIsCacheUserInfo(isCache);
    }

    void setGetGroupInfoProvider(GroupInfoProvider provider, boolean isCacheGroupInfo) {
        this.mGroupProvider = provider;
        RongUserInfoManager.getInstance().setIsCacheGroupInfo(isCacheGroupInfo);
    }

    UserInfoProvider getUserInfoProvider() {
        return this.mUserInfoProvider;
    }

    public GroupInfoProvider getGroupInfoProvider() {
        return this.mGroupProvider;
    }

    public void setGroupUserInfoProvider(GroupUserInfoProvider groupUserInfoProvider, boolean isCache) {
        this.mGroupUserInfoProvider = groupUserInfoProvider;
        RongUserInfoManager.getInstance().setIsCacheGroupUserInfo(isCache);
    }

    public GroupUserInfoProvider getGroupUserInfoProvider() {
        return this.mGroupUserInfoProvider;
    }

    public void setPublicServiceProfileProvider(PublicServiceProfileProvider provider) {
        this.mPublicServiceProfileProvider = provider;
    }

    public PublicServiceProfileProvider getPublicServiceProfileProvider() {
        return this.mPublicServiceProfileProvider;
    }

    public void registerConversationInfo(ConversationInfo info) {
        if (info != null) {
            ConversationKey key = ConversationKey.obtain(info.getTargetId(), info.getConversationType());
            if (key != null && !this.mCurrentConversationList.contains(key.getKey())) {
                this.mCurrentConversationList.add(key.getKey());
            }
        }

    }

    public void unregisterConversationInfo(ConversationInfo info) {
        if (info != null) {
            ConversationKey key = ConversationKey.obtain(info.getTargetId(), info.getConversationType());
            if (key != null && this.mCurrentConversationList.size() > 0) {
                this.mCurrentConversationList.remove(key.getKey());
            }
        }

    }

    public OnSendMessageListener getOnSendMessageListener() {
        return this.mOnSendMessageListener;
    }

    public void setOnSendMessageListener(OnSendMessageListener onSendMessageListener) {
        this.mOnSendMessageListener = onSendMessageListener;
    }

    public void setCurrentUserInfo(UserInfo userInfo) {
        this.mCurrentUserInfo = userInfo;
        if (userInfo != null && !TextUtils.isEmpty(userInfo.getUserId())) {
            RongUserInfoManager.getInstance().setUserInfo(userInfo);
        }

    }

    public UserInfo getCurrentUserInfo() {
        return this.mCurrentUserInfo != null ? this.mCurrentUserInfo : null;
    }

    public String getToken() {
        return this.getSharedPreferences("RongKitConfig", 0).getString("token", "");
    }

    public void setUserInfoAttachedState(boolean state) {
        this.isUserInfoAttached = state;
    }

    public boolean getUserInfoAttachedState() {
        return this.isUserInfoAttached;
    }

    public void setPublicServiceMenuClickListener(IPublicServiceMenuClickListener menuClickListener) {
        this.mPublicServiceMenuClickListener = menuClickListener;
    }

    public IPublicServiceMenuClickListener getPublicServiceMenuClickListener() {
        return this.mPublicServiceMenuClickListener;
    }

    public ConversationListBehaviorListener getConversationListBehaviorListener() {
        return this.mConversationListBehaviorListener;
    }

    public void setConversationListBehaviorListener(ConversationListBehaviorListener conversationListBehaviorListener) {
        this.mConversationListBehaviorListener = conversationListBehaviorListener;
    }

    public void setRequestPermissionListener(RequestPermissionsListener listener) {
        this.mRequestPermissionsListener = listener;
    }

    public RequestPermissionsListener getRequestPermissionListener() {
        return this.mRequestPermissionsListener;
    }

    public void showUnreadMessageIcon(boolean state) {
        this.isShowUnreadMessageState = state;
    }

    public void showNewMessageIcon(boolean state) {
        this.isShowNewMessageState = state;
    }

    public boolean getUnreadMessageState() {
        return this.isShowUnreadMessageState;
    }

    public boolean getNewMessageState() {
        return this.isShowNewMessageState;
    }

    public String getGatheredConversationTitle(ConversationType type) {
        String title = "";
        switch (type) {
            case PRIVATE:
                title = this.getString(R.string.rc_conversation_list_my_private_conversation);
                break;
            case GROUP:
                title = this.getString(R.string.rc_conversation_list_my_group);
                break;
            case DISCUSSION:
                title = this.getString(R.string.rc_conversation_list_my_discussion);
                break;
            case CHATROOM:
                title = this.getString(R.string.rc_conversation_list_my_chatroom);
                break;
            case CUSTOMER_SERVICE:
                title = this.getString(R.string.rc_conversation_list_my_customer_service);
                break;
            case SYSTEM:
                title = this.getString(R.string.rc_conversation_list_system_conversation);
                break;
            case APP_PUBLIC_SERVICE:
                title = this.getString(R.string.rc_conversation_list_app_public_service);
                break;
            case PUBLIC_SERVICE:
                title = this.getString(R.string.rc_conversation_list_public_service);
                break;
            default:
                System.err.print("It's not the default conversation type!!");
        }

        return title;
    }

    void setReadReceiptConversationTypeList(ConversationType... types) {
        if (types == null) {
            RLog.d("RongContext", "setReadReceiptConversationTypeList parameter is null");
        } else {
            this.mReadReceiptConversationTypeList.clear();
            ConversationType[] var2 = types;
            int var3 = types.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                ConversationType type = var2[var4];
                this.mReadReceiptConversationTypeList.add(type);
            }

        }
    }

    public boolean isReadReceiptConversationType(ConversationType type) {
        if (this.mReadReceiptConversationTypeList == null) {
            RLog.d("RongContext", "isReadReceiptConversationType mReadReceiptConversationTypeList is null");
            return false;
        } else {
            return this.mReadReceiptConversationTypeList.contains(type);
        }
    }

    public void setNotificationSound(Uri uri) {
        this.notificationSound = uri;
        RongPushClient.setNotifiationSound(uri);
    }

    public Uri getNotificationSound() {
        return this.notificationSound;
    }
}
