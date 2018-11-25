//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit;

import android.text.Spannable;
import android.text.TextUtils;

import java.util.concurrent.ConcurrentHashMap;

import io.rong.common.RLog;
import io.rong.imkit.model.ConversationKey;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Conversation.PublicServiceType;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.MentionedInfo.MentionedType;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.PublicServiceProfile;
import io.rong.imlib.model.UserInfo;
import io.rong.message.RecallNotificationMessage;
import io.rong.push.RongPushClient;
import io.rong.push.notification.PushNotificationMessage;
import io.rong.push.notification.PushNotificationMessage.PushSourceType;

public class RongNotificationManager {
  private static final String TAG = "RongNotificationManager";
  private static io.rong.imkit.RongNotificationManager sS = new io.rong.imkit.RongNotificationManager();
  RongContext mContext;
  ConcurrentHashMap<String, Message> messageMap = new ConcurrentHashMap();

  private RongNotificationManager() {
  }

  public void init(RongContext context) {
    this.mContext = context;
    this.messageMap.clear();
    if (!context.getEventBus().isRegistered(this)) {
      context.getEventBus().register(this);
    }

  }

  public static io.rong.imkit.RongNotificationManager getInstance() {
    if (sS == null) {
      sS = new io.rong.imkit.RongNotificationManager();
    }

    return sS;
  }

  public void onReceiveMessageFromApp(Message message) {
    ConversationType type = message.getConversationType();
    String targetName = null;
    String userName = "";
    MessageProvider provider = RongContext.getInstance().getMessageTemplate(message.getContent().getClass());
    if (provider != null) {
      Spannable content = provider.getContentSummary(this.mContext, message.getContent());
      ConversationKey targetKey = ConversationKey.obtain(message.getTargetId(), message.getConversationType());
      if (targetKey == null) {
        RLog.e("RongNotificationManager", "onReceiveMessageFromApp targetKey is null");
      }

      RLog.i("RongNotificationManager", "onReceiveMessageFromApp. conversationType:" + type);
      if (content == null) {
        RLog.e("RongNotificationManager", "onReceiveMessageFromApp Content is null. Return directly.");
      } else {
        PushNotificationMessage pushMsg;
        if (!type.equals(ConversationType.PRIVATE) && !type.equals(ConversationType.CUSTOMER_SERVICE) && !type.equals(ConversationType.CHATROOM) && !type.equals(ConversationType.SYSTEM)) {
          UserInfo userInfo;
          ConversationKey senderKey;
          String notificationContent;
          if (type.equals(ConversationType.GROUP)) {
            Group groupInfo = RongUserInfoManager.getInstance().getGroupInfo(message.getTargetId());
            userInfo = RongUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());
            if (groupInfo != null) {
              targetName = groupInfo.getName();
            }

            if (userInfo != null) {
              userName = userInfo.getName();
            }

            if (!TextUtils.isEmpty(targetName) && !TextUtils.isEmpty(userName)) {
              if (this.isMentionedMessage(message)) {
                if (TextUtils.isEmpty(message.getContent().getMentionedInfo().getMentionedContent())) {
                  notificationContent = this.mContext.getString(R.string.rc_message_content_mentioned) + userName + " : " + content.toString();
                } else {
                  notificationContent = message.getContent().getMentionedInfo().getMentionedContent();
                }
              } else if (message.getContent() instanceof RecallNotificationMessage) {
                notificationContent = content.toString();
              } else {
                notificationContent = userName + " : " + content.toString();
              }

              pushMsg = this.transformToPushMessage(message, notificationContent, targetName, "");
              RongPushClient.sendNotification(this.mContext, pushMsg);
            } else {
              if (TextUtils.isEmpty(targetName) && targetKey != null) {
                this.messageMap.put(targetKey.getKey(), message);
              }

              if (TextUtils.isEmpty(userName)) {
                senderKey = ConversationKey.obtain(message.getSenderUserId(), type);
                if (senderKey != null) {
                  this.messageMap.put(senderKey.getKey(), message);
                } else {
                  RLog.e("RongNotificationManager", "onReceiveMessageFromApp senderKey is null");
                }
              }

              RLog.e("RongNotificationManager", "No popup notification cause of the sender name is null, please set UserInfoProvider");
            }
          } else if (type.equals(ConversationType.DISCUSSION)) {
            Discussion discussionInfo = RongUserInfoManager.getInstance().getDiscussionInfo(message.getTargetId());
            userInfo = RongUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());
            if (discussionInfo != null) {
              targetName = discussionInfo.getName();
            }

            if (userInfo != null) {
              userName = userInfo.getName();
            }

            if (!TextUtils.isEmpty(targetName) && !TextUtils.isEmpty(userName)) {
              if (this.isMentionedMessage(message)) {
                if (TextUtils.isEmpty(message.getContent().getMentionedInfo().getMentionedContent())) {
                  notificationContent = this.mContext.getString(R.string.rc_message_content_mentioned) + userName + " : " + content.toString();
                } else {
                  notificationContent = message.getContent().getMentionedInfo().getMentionedContent();
                }
              } else {
                notificationContent = userName + " : " + content.toString();
              }

              pushMsg = this.transformToPushMessage(message, notificationContent, targetName, "");
              RongPushClient.sendNotification(this.mContext, pushMsg);
            } else {
              if (TextUtils.isEmpty(targetName) && targetKey != null) {
                this.messageMap.put(targetKey.getKey(), message);
              }

              if (TextUtils.isEmpty(userName)) {
                senderKey = ConversationKey.obtain(message.getSenderUserId(), type);
                if (senderKey != null) {
                  this.messageMap.put(senderKey.getKey(), message);
                } else {
                  RLog.e("RongNotificationManager", "onReceiveMessageFromApp senderKey is null");
                }
              }

              RLog.e("RongNotificationManager", "No popup notification cause of the sender name is null, please set UserInfoProvider");
            }
          } else if (type.getName().equals(ConversationType.PUBLIC_SERVICE.getName()) || type.getName().equals(PublicServiceType.APP_PUBLIC_SERVICE.getName())) {
            if (targetKey != null) {
              PublicServiceProfile info = RongContext.getInstance().getPublicServiceInfoFromCache(targetKey.getKey());
              if (info != null) {
                targetName = info.getName();
              }
            }

            if (!TextUtils.isEmpty(targetName)) {
              pushMsg = this.transformToPushMessage(message, content.toString(), targetName, "");
              RongPushClient.sendNotification(this.mContext, pushMsg);
            } else {
              if (targetKey != null) {
                this.messageMap.put(targetKey.getKey(), message);
              }

              RLog.e("RongNotificationManager", "No popup notification cause of the sender name is null, please set UserInfoProvider");
            }
          }
        } else {
          UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(message.getTargetId());
          if (userInfo != null) {
            targetName = userInfo.getName();
          }

          if (!TextUtils.isEmpty(targetName)) {
            pushMsg = this.transformToPushMessage(message, content.toString(), targetName, targetName);
            RongPushClient.sendNotification(this.mContext, pushMsg);
          } else {
            if (targetKey != null) {
              this.messageMap.put(targetKey.getKey(), message);
            }

            RLog.e("RongNotificationManager", "No popup notification cause of the sender name is null, please set UserInfoProvider");
          }
        }

      }
    }
  }

  public void onEventMainThread(UserInfo userInfo) {
    ConversationType[] types = new ConversationType[]{ConversationType.PRIVATE, ConversationType.GROUP, ConversationType.DISCUSSION, ConversationType.CUSTOMER_SERVICE, ConversationType.CHATROOM, ConversationType.SYSTEM};
    RLog.i("RongNotificationManager", "onEventMainThread. userInfo" + userInfo);
    ConversationType[] var5 = types;
    int var6 = types.length;

    for(int var7 = 0; var7 < var6; ++var7) {
      ConversationType type = var5[var7];
      String key = ConversationKey.obtain(userInfo.getUserId(), type).getKey();
      if (this.messageMap.containsKey(key)) {
        Message message = (Message)this.messageMap.get(key);
        String targetName = "";
        String notificationContent = "";
        Spannable content = RongContext.getInstance().getMessageTemplate(message.getContent().getClass()).getContentSummary(this.mContext, message.getContent());
        this.messageMap.remove(key);
        if (type.equals(ConversationType.GROUP)) {
          Group groupInfo = RongUserInfoManager.getInstance().getGroupInfo(message.getTargetId());
          if (groupInfo == null) {
            RLog.e("RongNotificationManager", "onEventMainThread userInfo : groupInfo is null, return directly");
            return;
          }

          targetName = groupInfo.getName();
          if (this.isMentionedMessage(message)) {
            if (TextUtils.isEmpty(message.getContent().getMentionedInfo().getMentionedContent())) {
              notificationContent = this.mContext.getString(R.string.rc_message_content_mentioned) + userInfo.getName() + " : " + content.toString();
            } else {
              notificationContent = message.getContent().getMentionedInfo().getMentionedContent();
            }
          } else {
            notificationContent = userInfo.getName() + " : " + content.toString();
          }
        } else if (type.equals(ConversationType.DISCUSSION)) {
          Discussion discussion = RongUserInfoManager.getInstance().getDiscussionInfo(message.getTargetId());
          if (discussion != null) {
            targetName = discussion.getName();
          }

          if (this.isMentionedMessage(message)) {
            if (TextUtils.isEmpty(message.getContent().getMentionedInfo().getMentionedContent())) {
              notificationContent = this.mContext.getString(R.string.rc_message_content_mentioned) + userInfo.getName() + " : " + content.toString();
            } else {
              notificationContent = message.getContent().getMentionedInfo().getMentionedContent();
            }
          } else {
            notificationContent = userInfo.getName() + " : " + content.toString();
          }
        } else {
          targetName = userInfo.getName();
          notificationContent = content.toString();
        }

        if (TextUtils.isEmpty(targetName)) {
          return;
        }

        PushNotificationMessage pushMsg = this.transformToPushMessage(message, notificationContent, targetName, "");
        RongPushClient.sendNotification(this.mContext, pushMsg);
      }
    }

  }

  public void onEventMainThread(Group groupInfo) {
    String key = ConversationKey.obtain(groupInfo.getId(), ConversationType.GROUP).getKey();
    RLog.i("RongNotificationManager", "onEventMainThread. groupInfo" + groupInfo);
    if (this.messageMap.containsKey(key)) {
      Message message = (Message)this.messageMap.get(key);
      String userName = "";
      Spannable content = RongContext.getInstance().getMessageTemplate(message.getContent().getClass()).getContentSummary(this.mContext, message.getContent());
      this.messageMap.remove(key);
      UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());
      if (userInfo == null) {
        RLog.e("RongNotificationManager", "onEventMainThread Group : userInfo is null, return directly");
        return;
      }

      userName = userInfo.getName();
      if (TextUtils.isEmpty(userName)) {
        RLog.e("RongNotificationManager", "onEventMainThread Group : userName is empty, return directly");
        return;
      }

      String pushContent = userName + " : " + content.toString();
      PushNotificationMessage pushMsg = this.transformToPushMessage(message, pushContent, groupInfo.getName(), "");
      RongPushClient.sendNotification(this.mContext, pushMsg);
    }

  }

  public void onEventMainThread(Discussion discussion) {
    String key = ConversationKey.obtain(discussion.getId(), ConversationType.DISCUSSION).getKey();
    if (this.messageMap.containsKey(key)) {
      String userName = "";
      Message message = (Message)this.messageMap.get(key);
      Spannable content = RongContext.getInstance().getMessageTemplate(message.getContent().getClass()).getContentSummary(this.mContext, message.getContent());
      this.messageMap.remove(key);
      UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());
      if (userInfo != null) {
        userName = userInfo.getName();
        if (TextUtils.isEmpty(userName)) {
          return;
        }
      }

      String pushContent = userName + " : " + content.toString();
      PushNotificationMessage pushMsg = this.transformToPushMessage(message, pushContent, discussion.getName(), "");
      RongPushClient.sendNotification(this.mContext, pushMsg);
    }

  }

  public void onEventMainThread(PublicServiceProfile info) {
    String key = ConversationKey.obtain(info.getTargetId(), info.getConversationType()).getKey();
    if (this.messageMap.containsKey(key)) {
      Message message = (Message)this.messageMap.get(key);
      Spannable content = RongContext.getInstance().getMessageTemplate(message.getContent().getClass()).getContentSummary(this.mContext, message.getContent());
      PushNotificationMessage pushMsg = this.transformToPushMessage(message, content.toString(), info.getName(), "");
      RongPushClient.sendNotification(this.mContext, pushMsg);
      this.messageMap.remove(key);
    }

  }

  private boolean isMentionedMessage(Message message) {
    MentionedInfo mentionedInfo = message.getContent().getMentionedInfo();
    return mentionedInfo != null && (mentionedInfo.getType().equals(MentionedType.ALL) || mentionedInfo.getType().equals(MentionedType.PART) && mentionedInfo.getMentionedUserIdList() != null && mentionedInfo.getMentionedUserIdList().contains(RongIMClient.getInstance().getCurrentUserId()));
  }

  private PushNotificationMessage transformToPushMessage(Message message, String content, String targetUserName, String senderName) {
    PushNotificationMessage pushMsg = new PushNotificationMessage();
    pushMsg.setPushContent(content);
    pushMsg.setConversationType(io.rong.push.RongPushClient.ConversationType.setValue(message.getConversationType().getValue()));
    pushMsg.setTargetId(message.getTargetId());
    pushMsg.setTargetUserName(targetUserName);
    pushMsg.setSenderId(message.getSenderUserId());
    pushMsg.setSenderName(senderName);
    if (message.getContent() instanceof RecallNotificationMessage) {
      pushMsg.setObjectName("RC:RcNtf");
    } else {
      pushMsg.setObjectName(message.getObjectName());
    }

    pushMsg.setPushFlag("false");
    pushMsg.setToId(RongIMClient.getInstance().getCurrentUserId());
    pushMsg.setSourceType(PushSourceType.LOCAL_MESSAGE);
    pushMsg.setPushId(message.getUId());
    return pushMsg;
  }
}
