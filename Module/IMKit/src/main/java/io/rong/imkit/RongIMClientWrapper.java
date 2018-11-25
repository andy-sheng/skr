//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit;

import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.RongIMClient.BlacklistStatus;
import io.rong.imlib.RongIMClient.ConnectCallback;
import io.rong.imlib.RongIMClient.ConnectionStatusListener;
import io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus;
import io.rong.imlib.RongIMClient.CreateDiscussionCallback;
import io.rong.imlib.RongIMClient.DiscussionInviteStatus;
import io.rong.imlib.RongIMClient.DownloadMediaCallback;
import io.rong.imlib.RongIMClient.GetBlacklistCallback;
import io.rong.imlib.RongIMClient.GetNotificationQuietHoursCallback;
import io.rong.imlib.RongIMClient.MediaType;
import io.rong.imlib.RongIMClient.OnReceiveMessageListener;
import io.rong.imlib.RongIMClient.OperationCallback;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.RongIMClient.SearchType;
import io.rong.imlib.RongIMClient.SendImageMessageCallback;
import io.rong.imlib.RongIMClient.SendImageMessageWithUploadListenerCallback;
import io.rong.imlib.RongIMClient.SendMessageCallback;
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

public class RongIMClientWrapper {
  public RongIMClientWrapper() {
  }

  /** @deprecated */
  @Deprecated
  public io.rong.imkit.RongIMClientWrapper connect(String token, ConnectCallback callback) {
    RongIM.connect(token, callback);
    return this;
  }

  /** @deprecated */
  @Deprecated
  public static void setConnectionStatusListener(ConnectionStatusListener listener) {
    RongIM.setConnectionStatusListener(listener);
  }

  /** @deprecated */
  @Deprecated
  public static void registerMessageType(Class<? extends MessageContent> type) throws AnnotationNotFoundException {
    RongIM.registerMessageType(type);
  }

  /** @deprecated */
  @Deprecated
  public ConnectionStatus getCurrentConnectionStatus() {
    return RongIM.getInstance().getCurrentConnectionStatus();
  }

  /** @deprecated */
  @Deprecated
  public void disconnect() {
    RongIM.getInstance().disconnect();
  }

  /** @deprecated */
  @Deprecated
  public void disconnect(boolean isReceivePush) {
    RongIM.getInstance().disconnect(isReceivePush);
  }

  /** @deprecated */
  @Deprecated
  public void logout() {
    RongIM.getInstance().logout();
  }

  /** @deprecated */
  @Deprecated
  public static void setOnReceiveMessageListener(OnReceiveMessageListener listener) {
    RongIM.setOnReceiveMessageListener(listener);
  }

  /** @deprecated */
  @Deprecated
  public void getConversationList(ResultCallback<List<Conversation>> callback) {
    RongIM.getInstance().getConversationList(callback);
  }

  /** @deprecated */
  @Deprecated
  public List<Conversation> getConversationList() {
    return RongIM.getInstance().getConversationList();
  }

  /** @deprecated */
  @Deprecated
  public void getConversationList(ResultCallback<List<Conversation>> callback, ConversationType... types) {
    RongIM.getInstance().getConversationList(callback, types);
  }

  /** @deprecated */
  @Deprecated
  public List<Conversation> getConversationList(ConversationType... types) {
    return RongIM.getInstance().getConversationList(types);
  }

  /** @deprecated */
  @Deprecated
  public void getConversation(ConversationType type, String targetId, ResultCallback<Conversation> callback) {
    RongIM.getInstance().getConversation(type, targetId, callback);
  }

  /** @deprecated */
  @Deprecated
  public Conversation getConversation(ConversationType type, String targetId) {
    return RongIM.getInstance().getConversation(type, targetId);
  }

  /** @deprecated */
  @Deprecated
  public void removeConversation(ConversationType type, String targetId, ResultCallback<Boolean> callback) {
    RongIM.getInstance().removeConversation(type, targetId, callback);
  }

  /** @deprecated */
  @Deprecated
  public boolean removeConversation(ConversationType type, String targetId) {
    return RongIM.getInstance().removeConversation(type, targetId);
  }

  /** @deprecated */
  @Deprecated
  public void setConversationToTop(ConversationType type, String id, boolean isTop, ResultCallback<Boolean> callback) {
    RongIM.getInstance().setConversationToTop(type, id, isTop, callback);
  }

  /** @deprecated */
  @Deprecated
  public boolean setConversationToTop(ConversationType conversationType, String targetId, boolean isTop) {
    return RongIM.getInstance().setConversationToTop(conversationType, targetId, isTop);
  }

  /** @deprecated */
  @Deprecated
  public void getTotalUnreadCount(ResultCallback<Integer> callback) {
    RongIM.getInstance().getTotalUnreadCount(callback);
  }

  /** @deprecated */
  @Deprecated
  public int getTotalUnreadCount() {
    return RongIM.getInstance().getTotalUnreadCount();
  }

  /** @deprecated */
  @Deprecated
  public void getUnreadCount(ConversationType conversationType, String targetId, ResultCallback<Integer> callback) {
    RongIM.getInstance().getUnreadCount(conversationType, targetId, callback);
  }

  /** @deprecated */
  @Deprecated
  public int getUnreadCount(ConversationType conversationType, String targetId) {
    return RongIM.getInstance().getUnreadCount(conversationType, targetId);
  }

  /** @deprecated */
  @Deprecated
  public void getUnreadCount(ResultCallback<Integer> callback, ConversationType... conversationTypes) {
    RongIM.getInstance().getUnreadCount(callback, conversationTypes);
  }

  /** @deprecated */
  @Deprecated
  public int getUnreadCount(ConversationType... conversationTypes) {
    return RongIM.getInstance().getUnreadCount(conversationTypes);
  }

  /** @deprecated */
  @Deprecated
  public void getUnreadCount(ConversationType[] conversationTypes, ResultCallback<Integer> callback) {
    RongIM.getInstance().getUnreadCount(conversationTypes, callback);
  }

  /** @deprecated */
  @Deprecated
  public List<Message> getLatestMessages(ConversationType conversationType, String targetId, int count) {
    return RongIM.getInstance().getLatestMessages(conversationType, targetId, count);
  }

  /** @deprecated */
  @Deprecated
  public void getLatestMessages(ConversationType conversationType, String targetId, int count, ResultCallback<List<Message>> callback) {
    RongIM.getInstance().getLatestMessages(conversationType, targetId, count, callback);
  }

  /** @deprecated */
  @Deprecated
  public List<Message> getHistoryMessages(ConversationType conversationType, String targetId, int oldestMessageId, int count) {
    return RongIM.getInstance().getHistoryMessages(conversationType, targetId, oldestMessageId, count);
  }

  /** @deprecated */
  @Deprecated
  public List<Message> getHistoryMessages(ConversationType conversationType, String targetId, String objectName, int oldestMessageId, int count) {
    return RongIM.getInstance().getHistoryMessages(conversationType, targetId, objectName, oldestMessageId, count);
  }

  /** @deprecated */
  @Deprecated
  public void getHistoryMessages(ConversationType conversationType, String targetId, String objectName, int oldestMessageId, int count, ResultCallback<List<Message>> callback) {
    RongIM.getInstance().getHistoryMessages(conversationType, targetId, objectName, oldestMessageId, count, callback);
  }

  /** @deprecated */
  @Deprecated
  public void getHistoryMessages(ConversationType conversationType, String targetId, int oldestMessageId, int count, ResultCallback<List<Message>> callback) {
    RongIM.getInstance().getHistoryMessages(conversationType, targetId, oldestMessageId, count, callback);
  }

  /** @deprecated */
  @Deprecated
  public void getRemoteHistoryMessages(ConversationType conversationType, String targetId, long dataTime, int count, ResultCallback<List<Message>> callback) {
    RongIM.getInstance().getRemoteHistoryMessages(conversationType, targetId, dataTime, count, callback);
  }

  /** @deprecated */
  @Deprecated
  public boolean deleteMessages(int[] messageIds) {
    return RongIM.getInstance().deleteMessages(messageIds);
  }

  /** @deprecated */
  @Deprecated
  public void deleteMessages(int[] messageIds, ResultCallback<Boolean> callback) {
    RongIM.getInstance().deleteMessages(messageIds, callback);
  }

  /** @deprecated */
  @Deprecated
  public boolean clearMessages(ConversationType conversationType, String targetId) {
    return RongIM.getInstance().clearMessages(conversationType, targetId);
  }

  /** @deprecated */
  @Deprecated
  public void clearMessages(ConversationType conversationType, String targetId, ResultCallback<Boolean> callback) {
    RongIM.getInstance().clearMessages(conversationType, targetId, callback);
  }

  /** @deprecated */
  @Deprecated
  public boolean clearMessagesUnreadStatus(ConversationType conversationType, String targetId) {
    return RongIM.getInstance().clearMessagesUnreadStatus(conversationType, targetId);
  }

  /** @deprecated */
  @Deprecated
  public void clearMessagesUnreadStatus(ConversationType conversationType, String targetId, ResultCallback<Boolean> callback) {
    RongIM.getInstance().clearMessagesUnreadStatus(conversationType, targetId, callback);
  }

  /** @deprecated */
  @Deprecated
  public boolean setMessageExtra(int messageId, String value) {
    return RongIM.getInstance().setMessageExtra(messageId, value);
  }

  /** @deprecated */
  @Deprecated
  public void setMessageExtra(int messageId, String value, ResultCallback<Boolean> callback) {
    RongIM.getInstance().setMessageExtra(messageId, value, callback);
  }

  /** @deprecated */
  @Deprecated
  public boolean setMessageReceivedStatus(int messageId, ReceivedStatus receivedStatus) {
    return RongIM.getInstance().setMessageReceivedStatus(messageId, receivedStatus);
  }

  /** @deprecated */
  @Deprecated
  public void setMessageReceivedStatus(int messageId, ReceivedStatus receivedStatus, ResultCallback<Boolean> callback) {
    RongIM.getInstance().setMessageReceivedStatus(messageId, receivedStatus, callback);
  }

  /** @deprecated */
  @Deprecated
  public boolean setMessageSentStatus(int messageId, SentStatus sentStatus) {
    return RongIM.getInstance().setMessageSentStatus(messageId, sentStatus);
  }

  /** @deprecated */
  @Deprecated
  public void setMessageSentStatus(int messageId, SentStatus sentStatus, ResultCallback<Boolean> callback) {
    RongIM.getInstance().setMessageSentStatus(messageId, sentStatus, callback);
  }

  /** @deprecated */
  @Deprecated
  public String getTextMessageDraft(ConversationType conversationType, String targetId) {
    return RongIM.getInstance().getTextMessageDraft(conversationType, targetId);
  }

  /** @deprecated */
  @Deprecated
  public boolean saveTextMessageDraft(ConversationType conversationType, String targetId, String content) {
    return RongIM.getInstance().saveTextMessageDraft(conversationType, targetId, content);
  }

  /** @deprecated */
  @Deprecated
  public boolean clearTextMessageDraft(ConversationType conversationType, String targetId) {
    return RongIM.getInstance().clearTextMessageDraft(conversationType, targetId);
  }

  /** @deprecated */
  @Deprecated
  public void getTextMessageDraft(ConversationType conversationType, String targetId, ResultCallback<String> callback) {
    RongIM.getInstance().getTextMessageDraft(conversationType, targetId, callback);
  }

  /** @deprecated */
  @Deprecated
  public void saveTextMessageDraft(ConversationType conversationType, String targetId, String content, ResultCallback<Boolean> callback) {
    RongIM.getInstance().saveTextMessageDraft(conversationType, targetId, content, callback);
  }

  /** @deprecated */
  @Deprecated
  public void clearTextMessageDraft(ConversationType conversationType, String targetId, ResultCallback<Boolean> callback) {
    RongIM.getInstance().clearTextMessageDraft(conversationType, targetId, callback);
  }

  /** @deprecated */
  @Deprecated
  public void getDiscussion(String discussionId, ResultCallback<Discussion> callback) {
    RongIM.getInstance().getDiscussion(discussionId, callback);
  }

  /** @deprecated */
  @Deprecated
  public void setDiscussionName(String discussionId, String name, OperationCallback callback) {
    RongIM.getInstance().setDiscussionName(discussionId, name, callback);
  }

  /** @deprecated */
  @Deprecated
  public void createDiscussion(String name, List<String> userIdList, CreateDiscussionCallback callback) {
    RongIM.getInstance().createDiscussion(name, userIdList, callback);
  }

  /** @deprecated */
  @Deprecated
  public void addMemberToDiscussion(String discussionId, List<String> userIdList, OperationCallback callback) {
    RongIM.getInstance().addMemberToDiscussion(discussionId, userIdList, callback);
  }

  /** @deprecated */
  @Deprecated
  public void removeMemberFromDiscussion(String discussionId, String userId, OperationCallback callback) {
    RongIM.getInstance().removeMemberFromDiscussion(discussionId, userId, callback);
  }

  /** @deprecated */
  @Deprecated
  public void quitDiscussion(String discussionId, OperationCallback callback) {
    RongIM.getInstance().quitDiscussion(discussionId, callback);
  }

  /** @deprecated */
  @Deprecated
  public void insertMessage(ConversationType type, String targetId, String senderUserId, MessageContent content, ResultCallback<Message> callback) {
    RongIM.getInstance().insertMessage(type, targetId, senderUserId, content, callback);
  }

  /** @deprecated */
  @Deprecated
  public Message insertMessage(ConversationType type, String targetId, String senderUserId, MessageContent content) {
    return RongIM.getInstance().insertMessage(type, targetId, senderUserId, content);
  }

  /** @deprecated */
  @Deprecated
  public Message sendMessage(ConversationType type, String targetId, MessageContent content, String pushContent, String pushData, SendMessageCallback callback) {
    return RongIM.getInstance().sendMessage(type, targetId, content, pushContent, pushData, callback);
  }

  /** @deprecated */
  @Deprecated
  public void sendMessage(ConversationType type, String targetId, MessageContent content, String pushContent, String pushData, SendMessageCallback callback, ResultCallback<Message> resultCallback) {
    RongIM.getInstance().sendMessage(type, targetId, content, pushContent, pushData, callback, resultCallback);
  }

  /** @deprecated */
  @Deprecated
  public void sendMessage(Message message, String pushContent, String pushData, SendMessageCallback callback, ResultCallback<Message> resultCallback) {
    RongIM.getInstance().sendMessage(message, pushContent, pushData, callback, resultCallback);
  }

  /** @deprecated */
  @Deprecated
  public Message sendMessage(Message message, String pushContent, String pushData, SendMessageCallback callback) {
    return RongIM.getInstance().sendMessage(message, pushContent, pushData, callback);
  }

  /** @deprecated */
  @Deprecated
  public void sendImageMessage(ConversationType type, String targetId, MessageContent content, String pushContent, String pushData, SendImageMessageCallback callback) {
    RongIM.getInstance().sendImageMessage(type, targetId, content, pushContent, pushData, callback);
  }

  /** @deprecated */
  @Deprecated
  public void sendImageMessage(Message message, String pushContent, String pushData, SendImageMessageCallback callback) {
    RongIM.getInstance().sendImageMessage(message, pushContent, pushData, callback);
  }

  /** @deprecated */
  @Deprecated
  public void sendImageMessage(Message message, String pushContent, String pushData, SendImageMessageWithUploadListenerCallback callback) {
    RongIM.getInstance().sendImageMessage(message, pushContent, pushData, callback);
  }

  /** @deprecated */
  @Deprecated
  public void downloadMedia(ConversationType conversationType, String targetId, MediaType mediaType, String imageUrl, DownloadMediaCallback callback) {
    RongIM.getInstance().downloadMedia(conversationType, targetId, mediaType, imageUrl, callback);
  }

  /** @deprecated */
  @Deprecated
  public void downloadMedia(String imageUrl, DownloadMediaCallback callback) {
    RongIM.getInstance().downloadMedia(imageUrl, callback);
  }

  /** @deprecated */
  @Deprecated
  public void getConversationNotificationStatus(ConversationType conversationType, String targetId, ResultCallback<ConversationNotificationStatus> callback) {
    RongIM.getInstance().getConversationNotificationStatus(conversationType, targetId, callback);
  }

  /** @deprecated */
  @Deprecated
  public void setConversationNotificationStatus(ConversationType conversationType, String targetId, ConversationNotificationStatus notificationStatus, ResultCallback<ConversationNotificationStatus> callback) {
    RongIM.getInstance().setConversationNotificationStatus(conversationType, targetId, notificationStatus, callback);
  }

  /** @deprecated */
  @Deprecated
  public void setDiscussionInviteStatus(String discussionId, DiscussionInviteStatus status, OperationCallback callback) {
    RongIM.getInstance().setDiscussionInviteStatus(discussionId, status, callback);
  }

  /** @deprecated */
  @Deprecated
  public void syncGroup(List<Group> groups, OperationCallback callback) {
    RongIM.getInstance().syncGroup(groups, callback);
  }

  /** @deprecated */
  @Deprecated
  public void joinGroup(String groupId, String groupName, OperationCallback callback) {
    RongIM.getInstance().joinGroup(groupId, groupName, callback);
  }

  /** @deprecated */
  @Deprecated
  public void quitGroup(String groupId, OperationCallback callback) {
    RongIM.getInstance().quitGroup(groupId, callback);
  }

  /** @deprecated */
  @Deprecated
  public String getCurrentUserId() {
    return RongIM.getInstance().getCurrentUserId();
  }

  /** @deprecated */
  @Deprecated
  public long getDeltaTime() {
    return RongIM.getInstance().getDeltaTime();
  }

  /** @deprecated */
  @Deprecated
  public void joinChatRoom(String chatroomId, int defMessageCount, OperationCallback callback) {
    RongIM.getInstance().joinChatRoom(chatroomId, defMessageCount, callback);
  }

  /** @deprecated */
  @Deprecated
  public void joinExistChatRoom(String chatroomId, int defMessageCount, OperationCallback callback) {
    RongIM.getInstance().joinExistChatRoom(chatroomId, defMessageCount, callback);
  }

  /** @deprecated */
  @Deprecated
  public void quitChatRoom(String chatroomId, OperationCallback callback) {
    RongIM.getInstance().quitChatRoom(chatroomId, callback);
  }

  /** @deprecated */
  @Deprecated
  public void clearConversations(ResultCallback callback, ConversationType... conversationTypes) {
    RongIM.getInstance().clearConversations(callback, conversationTypes);
  }

  /** @deprecated */
  @Deprecated
  public boolean clearConversations(ConversationType... conversationTypes) {
    return RongIM.getInstance().clearConversations(conversationTypes);
  }

  /** @deprecated */
  @Deprecated
  public void addToBlacklist(String userId, OperationCallback callback) {
    RongIM.getInstance().addToBlacklist(userId, callback);
  }

  /** @deprecated */
  @Deprecated
  public void removeFromBlacklist(String userId, OperationCallback callback) {
    RongIM.getInstance().removeFromBlacklist(userId, callback);
  }

  /** @deprecated */
  @Deprecated
  public void getBlacklistStatus(String userId, ResultCallback<BlacklistStatus> callback) {
    RongIM.getInstance().getBlacklistStatus(userId, callback);
  }

  /** @deprecated */
  @Deprecated
  public void getBlacklist(GetBlacklistCallback callback) {
    RongIM.getInstance().getBlacklist(callback);
  }

  /** @deprecated */
  @Deprecated
  public void setNotificationQuietHours(String startTime, int spanMinutes, OperationCallback callback) {
    RongIM.getInstance().setNotificationQuietHours(startTime, spanMinutes, callback);
  }

  /** @deprecated */
  @Deprecated
  public void removeNotificationQuietHours(OperationCallback callback) {
    RongIM.getInstance().removeNotificationQuietHours(callback);
  }

  /** @deprecated */
  @Deprecated
  public void getNotificationQuietHours(GetNotificationQuietHoursCallback callback) {
    RongIM.getInstance().getNotificationQuietHours(callback);
  }

  /** @deprecated */
  @Deprecated
  public void getPublicServiceProfile(PublicServiceType publicServiceType, String publicServiceId, ResultCallback<PublicServiceProfile> callback) {
    RongIM.getInstance().getPublicServiceProfile(publicServiceType, publicServiceId, callback);
  }

  /** @deprecated */
  @Deprecated
  public void searchPublicService(SearchType searchType, String keywords, ResultCallback<PublicServiceProfileList> callback) {
    RongIM.getInstance().searchPublicService(searchType, keywords, callback);
  }

  /** @deprecated */
  @Deprecated
  public void searchPublicServiceByType(PublicServiceType publicServiceType, SearchType searchType, String keywords, ResultCallback<PublicServiceProfileList> callback) {
    RongIM.getInstance().searchPublicServiceByType(publicServiceType, searchType, keywords, callback);
  }

  /** @deprecated */
  @Deprecated
  public void subscribePublicService(PublicServiceType publicServiceType, String publicServiceId, OperationCallback callback) {
    RongIM.getInstance().subscribePublicService(publicServiceType, publicServiceId, callback);
  }

  /** @deprecated */
  @Deprecated
  public void unsubscribePublicService(PublicServiceType publicServiceType, String publicServiceId, OperationCallback callback) {
    RongIM.getInstance().unsubscribePublicService(publicServiceType, publicServiceId, callback);
  }

  /** @deprecated */
  @Deprecated
  public void getPublicServiceList(ResultCallback<PublicServiceProfileList> callback) {
    RongIM.getInstance().getPublicServiceList(callback);
  }

  /** @deprecated */
  @Deprecated
  public void syncUserData(UserData userData, OperationCallback callback) {
    RongIM.getInstance().syncUserData(userData, callback);
  }
}
