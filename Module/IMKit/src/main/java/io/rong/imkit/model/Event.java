//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.model;

import android.app.Activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.imkit.model.GroupUserInfo;
import io.rong.imlib.RongIMClient.DiscussionInviteStatus;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.model.Conversation.ConversationNotificationStatus;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.Message.SentStatus;
import io.rong.message.RecallNotificationMessage;

public class Event {
  public Event() {
  }

  public static class ShowLoginBarEvent {
    private boolean isShowBar;

    public ShowLoginBarEvent(boolean isShowBar) {
      this.isShowBar = isShowBar;
    }

    public boolean isShowBar() {
      return this.isShowBar;
    }

    public void setShowBar(boolean showBar) {
      this.isShowBar = showBar;
    }
  }

  public static class CSTerminateEvent {
    private String text;
    private Activity activity;

    public CSTerminateEvent(Activity activity, String content) {
      this.activity = activity;
      this.text = content;
    }

    public String getText() {
      return this.text;
    }

    public Activity getActivity() {
      return this.activity;
    }
  }

  public static class DraftEvent {
    private String content;
    private ConversationType conversationType;
    private String targetId;

    public DraftEvent(ConversationType conversationType, String targetId, String content) {
      this.conversationType = conversationType;
      this.targetId = targetId;
      this.content = content;
    }

    public ConversationType getConversationType() {
      return this.conversationType;
    }

    public String getTargetId() {
      return this.targetId;
    }

    public String getContent() {
      return this.content;
    }
  }

  public static class SyncReadStatusEvent {
    private ConversationType type;
    private String targetId;

    public String getTargetId() {
      return this.targetId;
    }

    public ConversationType getConversationType() {
      return this.type;
    }

    public SyncReadStatusEvent(ConversationType type, String targetId) {
      this.type = type;
      this.targetId = targetId;
    }
  }

  public static class ReadReceiptResponseEvent {
    private ConversationType type;
    private String targetId;
    private String messageUId;
    private HashMap<String, Long> responseUserIdList;

    public ConversationType getConversationType() {
      return this.type;
    }

    public String getTargetId() {
      return this.targetId;
    }

    public String getMessageUId() {
      return this.messageUId;
    }

    public HashMap<String, Long> getResponseUserIdList() {
      return this.responseUserIdList;
    }

    public ReadReceiptResponseEvent(ConversationType type, String targetId, String messageUId, HashMap<String, Long> responseUserIdList) {
      this.type = type;
      this.targetId = targetId;
      this.messageUId = messageUId;
      this.responseUserIdList = responseUserIdList;
    }
  }

  public static class ReadReceiptRequestEvent {
    private ConversationType type;
    private String targetId;
    private String messageUId;

    public ConversationType getConversationType() {
      return this.type;
    }

    public String getTargetId() {
      return this.targetId;
    }

    public String getMessageUId() {
      return this.messageUId;
    }

    public ReadReceiptRequestEvent(ConversationType type, String targetId, String messageUId) {
      this.type = type;
      this.targetId = targetId;
      this.messageUId = messageUId;
    }
  }

  public static class RemoteMessageRecallEvent {
    private int mMessageId;
    private RecallNotificationMessage mRecallNotificationMessage;
    private boolean mRecallSuccess;
    private ConversationType mConversationType;
    private String mTargetId;

    public int getMessageId() {
      return this.mMessageId;
    }

    public ConversationType getConversationType() {
      return this.mConversationType;
    }

    public RecallNotificationMessage getRecallNotificationMessage() {
      return this.mRecallNotificationMessage;
    }

    public boolean isRecallSuccess() {
      return this.mRecallSuccess;
    }

    public RemoteMessageRecallEvent(int messageId, ConversationType type, RecallNotificationMessage recallNotificationMessage, boolean recallSuccess, String targetId) {
      this.mMessageId = messageId;
      this.mRecallNotificationMessage = recallNotificationMessage;
      this.mRecallSuccess = recallSuccess;
      this.mConversationType = type;
      this.mTargetId = targetId;
    }

    public String getTargetId() {
      return this.mTargetId;
    }
  }

  public static class MessageRecallEvent {
    private int mMessageId;
    private RecallNotificationMessage mRecallNotificationMessage;
    private boolean mRecallSuccess;

    public int getMessageId() {
      return this.mMessageId;
    }

    public RecallNotificationMessage getRecallNotificationMessage() {
      return this.mRecallNotificationMessage;
    }

    public boolean isRecallSuccess() {
      return this.mRecallSuccess;
    }

    public MessageRecallEvent(int messageId, RecallNotificationMessage recallNotificationMessage, boolean recallSuccess) {
      this.mMessageId = messageId;
      this.mRecallNotificationMessage = recallNotificationMessage;
      this.mRecallSuccess = recallSuccess;
    }
  }

  public static class ClearConversationEvent {
    private List<ConversationType> typeList = new ArrayList();

    public ClearConversationEvent() {
    }

    public static io.rong.imkit.model.Event.ClearConversationEvent obtain(ConversationType... conversationTypes) {
      io.rong.imkit.model.Event.ClearConversationEvent clearConversationEvent = new io.rong.imkit.model.Event.ClearConversationEvent();
      clearConversationEvent.setTypes(conversationTypes);
      return clearConversationEvent;
    }

    public void setTypes(ConversationType[] types) {
      if (types != null && types.length != 0) {
        this.typeList.clear();
        ConversationType[] var2 = types;
        int var3 = types.length;

        for(int var4 = 0; var4 < var3; ++var4) {
          ConversationType type = var2[var4];
          this.typeList.add(type);
        }

      }
    }

    public List<ConversationType> getTypes() {
      return this.typeList;
    }
  }

  public static class ReadReceiptEvent {
    private Message readReceiptMessage;

    public ReadReceiptEvent(Message message) {
      this.readReceiptMessage = message;
    }

    public Message getMessage() {
      return this.readReceiptMessage;
    }
  }

  public static class PlayAudioEvent {
    public int messageId;
    public boolean continuously;

    public PlayAudioEvent() {
    }

    public static io.rong.imkit.model.Event.PlayAudioEvent obtain() {
      return new io.rong.imkit.model.Event.PlayAudioEvent();
    }
  }

  public static class GroupUserInfoEvent {
    private GroupUserInfo userInfo;

    public GroupUserInfoEvent() {
    }

    public static io.rong.imkit.model.Event.GroupUserInfoEvent obtain(GroupUserInfo info) {
      io.rong.imkit.model.Event.GroupUserInfoEvent event = new io.rong.imkit.model.Event.GroupUserInfoEvent();
      event.userInfo = info;
      return event;
    }

    public GroupUserInfo getUserInfo() {
      return this.userInfo;
    }
  }

  public static class ConnectEvent {
    private boolean isConnectSuccess;

    public ConnectEvent() {
    }

    public static io.rong.imkit.model.Event.ConnectEvent obtain(boolean flag) {
      io.rong.imkit.model.Event.ConnectEvent event = new io.rong.imkit.model.Event.ConnectEvent();
      event.setConnectStatus(flag);
      return event;
    }

    public void setConnectStatus(boolean flag) {
      this.isConnectSuccess = flag;
    }

    public boolean getConnectStatus() {
      return this.isConnectSuccess;
    }
  }

  public static class NotificationPublicServiceInfoEvent {
    private String key;

    NotificationPublicServiceInfoEvent(String key) {
      this.setKey(key);
    }

    public static io.rong.imkit.model.Event.NotificationPublicServiceInfoEvent obtain(String key) {
      return new io.rong.imkit.model.Event.NotificationPublicServiceInfoEvent(key);
    }

    public String getKey() {
      return this.key;
    }

    public void setKey(String key) {
      this.key = key;
    }
  }

  public static class NotificationDiscussionInfoEvent {
    private String key;

    NotificationDiscussionInfoEvent(String key) {
      this.setKey(key);
    }

    public static io.rong.imkit.model.Event.NotificationDiscussionInfoEvent obtain(String key) {
      return new io.rong.imkit.model.Event.NotificationDiscussionInfoEvent(key);
    }

    public String getKey() {
      return this.key;
    }

    public void setKey(String key) {
      this.key = key;
    }
  }

  public static class NotificationGroupInfoEvent {
    private String key;

    NotificationGroupInfoEvent(String key) {
      this.setKey(key);
    }

    public static io.rong.imkit.model.Event.NotificationGroupInfoEvent obtain(String key) {
      return new io.rong.imkit.model.Event.NotificationGroupInfoEvent(key);
    }

    public String getKey() {
      return this.key;
    }

    public void setKey(String key) {
      this.key = key;
    }
  }

  public static class NotificationUserInfoEvent {
    private String key;

    NotificationUserInfoEvent(String key) {
      this.setKey(key);
    }

    public static io.rong.imkit.model.Event.NotificationUserInfoEvent obtain(String key) {
      return new io.rong.imkit.model.Event.NotificationUserInfoEvent(key);
    }

    public String getKey() {
      return this.key;
    }

    public void setKey(String key) {
      this.key = key;
    }
  }

  public static class AudioListenedEvent extends io.rong.imkit.model.Event.BaseConversationEvent {
    private Message message;

    public AudioListenedEvent(Message message) {
      this.message = message;
    }

    public Message getMessage() {
      return this.message;
    }
  }

  public static class VoiceInputOperationEvent {
    public static int STATUS_DEFAULT = -1;
    public static int STATUS_INPUTING = 0;
    public static int STATUS_INPUT_COMPLETE = 1;
    private int status;

    public VoiceInputOperationEvent(int status) {
      this.setStatus(status);
    }

    public static io.rong.imkit.model.Event.VoiceInputOperationEvent obtain(int status) {
      return new io.rong.imkit.model.Event.VoiceInputOperationEvent(status);
    }

    public int getStatus() {
      return this.status;
    }

    public void setStatus(int status) {
      this.status = status;
    }
  }

  public static class PublicServiceFollowableEvent extends io.rong.imkit.model.Event.BaseConversationEvent {
    private boolean isFollow = false;

    public PublicServiceFollowableEvent(String targetId, ConversationType conversationType, boolean isFollow) {
      this.setTargetId(targetId);
      this.setConversationType(conversationType);
      this.setIsFollow(isFollow);
    }

    public static io.rong.imkit.model.Event.PublicServiceFollowableEvent obtain(String targetId, ConversationType conversationType, boolean isFollow) {
      return new io.rong.imkit.model.Event.PublicServiceFollowableEvent(targetId, conversationType, isFollow);
    }

    public boolean isFollow() {
      return this.isFollow;
    }

    public void setIsFollow(boolean isFollow) {
      this.isFollow = isFollow;
    }
  }

  public static class ConversationNotificationEvent extends io.rong.imkit.model.Event.BaseConversationEvent {
    private ConversationNotificationStatus mStatus;

    public ConversationNotificationEvent(String targetId, ConversationType conversationType, ConversationNotificationStatus conversationNotificationStatus) {
      this.setTargetId(targetId);
      this.setConversationType(conversationType);
      this.setStatus(conversationNotificationStatus);
    }

    public static io.rong.imkit.model.Event.ConversationNotificationEvent obtain(String targetId, ConversationType conversationType, ConversationNotificationStatus conversationNotificationStatus) {
      return new io.rong.imkit.model.Event.ConversationNotificationEvent(targetId, conversationType, conversationNotificationStatus);
    }

    public ConversationNotificationStatus getStatus() {
      return this.mStatus;
    }

    public void setStatus(ConversationNotificationStatus status) {
      this.mStatus = status;
    }
  }

  protected static class BaseConversationEvent {
    protected ConversationType mConversationType;
    protected String mTargetId;

    protected BaseConversationEvent() {
    }

    public ConversationType getConversationType() {
      return this.mConversationType;
    }

    public void setConversationType(ConversationType conversationType) {
      this.mConversationType = conversationType;
    }

    public String getTargetId() {
      return this.mTargetId;
    }

    public void setTargetId(String targetId) {
      this.mTargetId = targetId;
    }
  }

  public static class RemoveFromBlacklistEvent {
    String userId;

    public RemoveFromBlacklistEvent(String userId) {
      this.userId = userId;
    }

    public String getUserId() {
      return this.userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }
  }

  public static class AddToBlacklistEvent {
    String userId;

    public AddToBlacklistEvent(String userId) {
      this.userId = userId;
    }

    public String getUserId() {
      return this.userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }
  }

  public static class QuitChatRoomEvent {
    String chatRoomId;

    public QuitChatRoomEvent(String chatRoomId) {
      this.chatRoomId = chatRoomId;
    }

    public String getChatRoomId() {
      return this.chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
      this.chatRoomId = chatRoomId;
    }
  }

  public static class JoinChatRoomEvent {
    String chatRoomId;
    int defMessageCount;

    public JoinChatRoomEvent(String chatRoomId, int defMessageCount) {
      this.chatRoomId = chatRoomId;
      this.defMessageCount = defMessageCount;
    }

    public String getChatRoomId() {
      return this.chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
      this.chatRoomId = chatRoomId;
    }

    public int getDefMessageCount() {
      return this.defMessageCount;
    }

    public void setDefMessageCount(int defMessageCount) {
      this.defMessageCount = defMessageCount;
    }
  }

  public static class QuitGroupEvent {
    String groupId;

    public QuitGroupEvent(String groupId) {
      this.groupId = groupId;
    }

    public String getGroupId() {
      return this.groupId;
    }

    public void setGroupId(String groupId) {
      this.groupId = groupId;
    }
  }

  public static class JoinGroupEvent {
    String groupId;
    String groupName;

    public JoinGroupEvent(String groupId, String groupName) {
      this.groupId = groupId;
      this.groupName = groupName;
    }

    public String getGroupId() {
      return this.groupId;
    }

    public void setGroupId(String groupId) {
      this.groupId = groupId;
    }

    public String getGroupName() {
      return this.groupName;
    }

    public void setGroupName(String groupName) {
      this.groupName = groupName;
    }
  }

  public static class SyncGroupEvent {
    List<Group> groups;

    public SyncGroupEvent(List<Group> groups) {
      this.groups = groups;
    }

    public List<Group> getGroups() {
      return this.groups;
    }

    public void setGroups(List<Group> groups) {
      this.groups = groups;
    }
  }

  public static class DiscussionInviteStatusEvent {
    String discussionId;
    DiscussionInviteStatus status;

    public DiscussionInviteStatusEvent(String discussionId, DiscussionInviteStatus status) {
      this.discussionId = discussionId;
      this.status = status;
    }

    public String getDiscussionId() {
      return this.discussionId;
    }

    public void setDiscussionId(String discussionId) {
      this.discussionId = discussionId;
    }

    public DiscussionInviteStatus getStatus() {
      return this.status;
    }

    public void setStatus(DiscussionInviteStatus status) {
      this.status = status;
    }
  }

  public static class QuitDiscussionEvent {
    String discussionId;

    public QuitDiscussionEvent(String discussionId) {
      this.discussionId = discussionId;
    }

    public String getDiscussionId() {
      return this.discussionId;
    }

    public void setDiscussionId(String discussionId) {
      this.discussionId = discussionId;
    }
  }

  public static class RemoveMemberFromDiscussionEvent {
    String discussionId;
    String userId;

    public RemoveMemberFromDiscussionEvent(String discussionId, String userId) {
      this.discussionId = discussionId;
      this.userId = userId;
    }

    public String getDiscussionId() {
      return this.discussionId;
    }

    public void setDiscussionId(String discussionId) {
      this.discussionId = discussionId;
    }

    public String getUserId() {
      return this.userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }
  }

  public static class AddMemberToDiscussionEvent {
    String discussionId;
    List<String> userIdList;

    public AddMemberToDiscussionEvent(String discussionId, List<String> userIdList) {
      this.discussionId = discussionId;
      this.userIdList = userIdList;
    }

    public String getDiscussionId() {
      return this.discussionId;
    }

    public void setDiscussionId(String discussionId) {
      this.discussionId = discussionId;
    }

    public List<String> getUserIdList() {
      return this.userIdList;
    }

    public void setUserIdList(List<String> userIdList) {
      this.userIdList = userIdList;
    }
  }

  public static class CreateDiscussionEvent {
    String discussionId;
    String discussionName;
    List<String> userIdList;

    public CreateDiscussionEvent(String discussionId, String discussionName, List<String> userIdList) {
      this.discussionId = discussionId;
      this.discussionName = discussionName;
      this.userIdList = userIdList;
    }

    public String getDiscussionId() {
      return this.discussionId;
    }

    public void setDiscussionId(String discussionId) {
      this.discussionId = discussionId;
    }

    public String getDiscussionName() {
      return this.discussionName;
    }

    public void setDiscussionName(String discussionName) {
      this.discussionName = discussionName;
    }

    public List<String> getUserIdList() {
      return this.userIdList;
    }

    public void setUserIdList(List<String> userIdList) {
      this.userIdList = userIdList;
    }
  }

  public static class MessagesClearEvent {
    ConversationType type;
    String targetId;

    public MessagesClearEvent(ConversationType type, String targetId) {
      this.type = type;
      this.targetId = targetId;
    }

    public ConversationType getType() {
      return this.type;
    }

    public void setType(ConversationType type) {
      this.type = type;
    }

    public String getTargetId() {
      return this.targetId;
    }

    public void setTargetId(String targetId) {
      this.targetId = targetId;
    }
  }

  public static class MessageDeleteEvent {
    List<Integer> messageIds;

    public MessageDeleteEvent(int... ids) {
      if (ids != null && ids.length != 0) {
        this.messageIds = new ArrayList();
        int[] var2 = ids;
        int var3 = ids.length;

        for(int var4 = 0; var4 < var3; ++var4) {
          int id = var2[var4];
          this.messageIds.add(id);
        }

      }
    }

    public List<Integer> getMessageIds() {
      return this.messageIds;
    }

    public void setMessageIds(List<Integer> messageIds) {
      this.messageIds = messageIds;
    }
  }

  public static class MessageSentStatusUpdateEvent {
    Message message;
    SentStatus sentStatus;

    public MessageSentStatusUpdateEvent(Message message, SentStatus sentStatus) {
      this.message = message;
      this.sentStatus = sentStatus;
    }

    public Message getMessage() {
      return this.message;
    }

    public void setMessage(Message message) {
      this.message = message;
    }

    public SentStatus getSentStatus() {
      return this.sentStatus;
    }

    public void setSentStatus(SentStatus sentStatus) {
      this.sentStatus = sentStatus;
    }
  }

  public static class MessageSentStatusEvent {
    int messageId;
    SentStatus sentStatus;

    public MessageSentStatusEvent(int messageId, SentStatus sentStatus) {
      this.messageId = messageId;
      this.sentStatus = sentStatus;
    }

    public int getMessageId() {
      return this.messageId;
    }

    public void setMessageId(int messageId) {
      this.messageId = messageId;
    }

    public SentStatus getSentStatus() {
      return this.sentStatus;
    }

    public void setSentStatus(SentStatus sentStatus) {
      this.sentStatus = sentStatus;
    }
  }

  public static class ConversationRemoveEvent {
    ConversationType type;
    String targetId;

    public ConversationRemoveEvent(ConversationType type, String targetId) {
      this.type = type;
      this.targetId = targetId;
    }

    public ConversationType getType() {
      return this.type;
    }

    public void setType(ConversationType type) {
      this.type = type;
    }

    public String getTargetId() {
      return this.targetId;
    }

    public void setTargetId(String targetId) {
      this.targetId = targetId;
    }
  }

  public static class ConversationTopEvent extends io.rong.imkit.model.Event.BaseConversationEvent {
    boolean isTop;

    public ConversationTopEvent(ConversationType type, String targetId, boolean isTop) {
      this.setConversationType(type);
      this.setTargetId(targetId);
      this.isTop = isTop;
    }

    public boolean isTop() {
      return this.isTop;
    }

    public void setTop(boolean isTop) {
      this.isTop = isTop;
    }
  }

  public static class ConversationUnreadEvent {
    ConversationType type;
    String targetId;

    public ConversationUnreadEvent(ConversationType type, String targetId) {
      this.type = type;
      this.targetId = targetId;
    }

    public ConversationType getType() {
      return this.type;
    }

    public void setType(ConversationType type) {
      this.type = type;
    }

    public String getTargetId() {
      return this.targetId;
    }

    public void setTargetId(String targetId) {
      this.targetId = targetId;
    }
  }

  public static class OnMessageSendErrorEvent {
    Message message;
    ErrorCode errorCode;

    public OnMessageSendErrorEvent(Message message, ErrorCode errorCode) {
      this.message = message;
      this.errorCode = errorCode;
    }

    public Message getMessage() {
      return this.message;
    }

    public void setMessage(Message message) {
      this.message = message;
    }

    public ErrorCode getErrorCode() {
      return this.errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
      this.errorCode = errorCode;
    }
  }

  public static class OnReceiveMessageProgressEvent {
    Message message;
    int progress;

    public OnReceiveMessageProgressEvent() {
    }

    public int getProgress() {
      return this.progress;
    }

    public Message getMessage() {
      return this.message;
    }

    public void setMessage(Message message) {
      this.message = message;
    }

    public void setProgress(int progress) {
      this.progress = progress;
    }
  }

  public static class MessageLeftEvent {
    public int left;

    public MessageLeftEvent(int left) {
      this.left = left;
    }
  }

  public static class OnReceiveMessageEvent {
    Message message;
    int left;

    public OnReceiveMessageEvent(Message message, int left) {
      this.message = message;
      this.left = left;
    }

    public Message getMessage() {
      return this.message;
    }

    public void setMessage(Message message) {
      this.message = message;
    }

    public int getLeft() {
      return this.left;
    }

    public void setLeft(int left) {
      this.left = left;
    }
  }

  public static class MediaFileEvent {
    String uid;
    int progress;
    int callBackType;
    ErrorCode errorCode;

    public MediaFileEvent(String uid, int progress, int callBackType, ErrorCode errorCode) {
      this.uid = uid;
      this.progress = progress;
      this.callBackType = callBackType;
      this.errorCode = errorCode;
    }

    public int getProgress() {
      return this.progress;
    }

    public void setProgress(int progress) {
      this.progress = progress;
    }

    public int getCallBackType() {
      return this.callBackType;
    }

    public void setCallBackType(int callBackType) {
      this.callBackType = callBackType;
    }

    public ErrorCode getErrorCode() {
      return this.errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
      this.errorCode = errorCode;
    }

    public void setUid(String uid) {
      this.uid = uid;
    }

    public String getUid() {
      return this.uid;
    }
  }

  public static class FileMessageEvent {
    Message message;
    int progress;
    int callBackType;
    ErrorCode errorCode;

    public FileMessageEvent(Message message, int progress, int callBackType, ErrorCode errorCode) {
      this.message = message;
      this.progress = progress;
      this.callBackType = callBackType;
      this.errorCode = errorCode;
    }

    public Message getMessage() {
      return this.message;
    }

    public void setMessage(Message message) {
      this.message = message;
    }

    public int getProgress() {
      return this.progress;
    }

    public void setProgress(int progress) {
      this.progress = progress;
    }

    public int getCallBackType() {
      return this.callBackType;
    }

    public void setCallBackType(int callBackType) {
      this.callBackType = callBackType;
    }

    public ErrorCode getErrorCode() {
      return this.errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
      this.errorCode = errorCode;
    }
  }
}
