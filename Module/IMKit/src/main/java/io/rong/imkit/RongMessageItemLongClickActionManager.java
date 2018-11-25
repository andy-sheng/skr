//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.rong.common.RLog;
import io.rong.imkit.manager.AudioPlayManager;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.provider.MessageItemLongClickAction;
import io.rong.imkit.widget.provider.MessageItemLongClickAction.Builder;
import io.rong.imkit.widget.provider.MessageItemLongClickAction.Filter;
import io.rong.imkit.widget.provider.MessageItemLongClickAction.MessageItemLongClickListener;
import io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.location.message.RealTimeLocationStartMessage;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message.SentStatus;
import io.rong.imlib.model.UnknownMessage;
import io.rong.imlib.model.UserInfo;
import io.rong.message.HandshakeMessage;
import io.rong.message.NotificationMessage;
import io.rong.message.PublicServiceMultiRichContentMessage;
import io.rong.message.PublicServiceRichContentMessage;
import io.rong.message.RecallNotificationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

public class RongMessageItemLongClickActionManager {
  private static final String TAG = io.rong.imkit.RongMessageItemLongClickActionManager.class.getSimpleName();
  private List<MessageItemLongClickAction> messageItemLongClickActions;

  public static io.rong.imkit.RongMessageItemLongClickActionManager getInstance() {
    return io.rong.imkit.RongMessageItemLongClickActionManager.Holder.instance;
  }

  private RongMessageItemLongClickActionManager() {
  }

  public void init() {
    if (this.messageItemLongClickActions == null) {
      this.messageItemLongClickActions = new ArrayList();
      this.initCommonMessageItemLongClickActions();
    }

  }

  public List<MessageItemLongClickAction> getMessageItemLongClickActions() {
    return this.messageItemLongClickActions;
  }

  public void addMessageItemLongClickAction(MessageItemLongClickAction action) {
    this.addMessageItemLongClickAction(action, -1);
  }

  public void addMessageItemLongClickAction(MessageItemLongClickAction action, int index) {
    if (this.messageItemLongClickActions.contains(action)) {
      this.messageItemLongClickActions.remove(action);
    }

    if (index < 0) {
      this.messageItemLongClickActions.add(action);
    } else {
      this.messageItemLongClickActions.add(index, action);
    }

  }

  public void removeMessageItemLongClickAction(MessageItemLongClickAction action) {
    this.messageItemLongClickActions.remove(action);
  }

  public List<MessageItemLongClickAction> getMessageItemLongClickActions(UIMessage uiMessage) {
    List<MessageItemLongClickAction> actions = new ArrayList();
    Iterator var3 = this.messageItemLongClickActions.iterator();

    while(var3.hasNext()) {
      MessageItemLongClickAction action = (MessageItemLongClickAction)var3.next();
      if (action.filter(uiMessage)) {
        actions.add(action);
      }
    }

    return actions;
  }

  private void initCommonMessageItemLongClickActions() {
    MessageItemLongClickAction messageItemLongClickAction = (new Builder()).titleResId(R.string.rc_dialog_item_message_copy).actionListener(new MessageItemLongClickListener() {
      public boolean onMessageItemLongClick(Context context, UIMessage message) {
        ClipboardManager clipboard = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (message.getContent() instanceof RecallNotificationMessage) {
          return false;
        } else {
          clipboard.setText(((TextMessage)message.getContent()).getContent());
          return true;
        }
      }
    }).showFilter(new Filter() {
      public boolean filter(UIMessage message) {
        return message.getContent() instanceof TextMessage;
      }
    }).build();
    this.addMessageItemLongClickAction(messageItemLongClickAction);
    messageItemLongClickAction = (new Builder()).titleResId(R.string.rc_dialog_item_message_recall).actionListener(new MessageItemLongClickListener() {
      public boolean onMessageItemLongClick(Context context, UIMessage message) {
        if (RongIM.getInstance().getCurrentConnectionStatus() == ConnectionStatus.NETWORK_UNAVAILABLE) {
          Toast.makeText(context, context.getResources().getString(R.string.rc_recall_failed_for_network_unavailable), Toast.LENGTH_SHORT).show();
          return true;
        } else {
          RongIM.getInstance().recallMessage(message.getMessage(), io.rong.imkit.RongMessageItemLongClickActionManager.this.getPushContent(context, message));
          return true;
        }
      }
    }).showFilter(new Filter() {
      public boolean filter(UIMessage message) {
        if (!(message.getContent() instanceof NotificationMessage) && !(message.getContent() instanceof HandshakeMessage) && !(message.getContent() instanceof PublicServiceRichContentMessage) && !(message.getContent() instanceof RealTimeLocationStartMessage) && !(message.getContent() instanceof UnknownMessage) && !(message.getContent() instanceof PublicServiceMultiRichContentMessage) && !message.getSentStatus().equals(SentStatus.CANCELED)) {
          long deltaTime = RongIM.getInstance().getDeltaTime();
          long normalTime = System.currentTimeMillis() - deltaTime;
          boolean enableMessageRecall = false;
          int messageRecallInterval = -1;
          boolean hasSent = !message.getSentStatus().equals(SentStatus.SENDING) && !message.getSentStatus().equals(SentStatus.FAILED);

          try {
            enableMessageRecall = RongContext.getInstance().getResources().getBoolean(R.bool.rc_enable_message_recall);
            messageRecallInterval = RongContext.getInstance().getResources().getInteger(R.integer.rc_message_recall_interval);
          } catch (NotFoundException var10) {
            RLog.e(io.rong.imkit.RongMessageItemLongClickActionManager.TAG, "rc_message_recall_interval not configure in rc_config.xml");
            var10.printStackTrace();
          }

          return hasSent && enableMessageRecall && normalTime - message.getSentTime() <= (long)(messageRecallInterval * 1000) && message.getSenderUserId().equals(RongIM.getInstance().getCurrentUserId()) && !message.getConversationType().equals(ConversationType.CUSTOMER_SERVICE) && !message.getConversationType().equals(ConversationType.APP_PUBLIC_SERVICE) && !message.getConversationType().equals(ConversationType.PUBLIC_SERVICE) && !message.getConversationType().equals(ConversationType.SYSTEM) && !message.getConversationType().equals(ConversationType.CHATROOM);
        } else {
          return false;
        }
      }
    }).build();
    this.addMessageItemLongClickAction(messageItemLongClickAction);
    messageItemLongClickAction = (new Builder()).titleResId(R.string.rc_dialog_item_message_delete).actionListener(new MessageItemLongClickListener() {
      public boolean onMessageItemLongClick(Context context, UIMessage message) {
        if (message.getMessage().getContent() instanceof VoiceMessage) {
          Uri uri = ((VoiceMessage)message.getMessage().getContent()).getUri();
          Uri playingUri = AudioPlayManager.getInstance().getPlayingUri();
          if (playingUri != null && playingUri == uri) {
            AudioPlayManager.getInstance().stopPlay();
          }
        }

        RongIM.getInstance().deleteMessages(new int[]{message.getMessageId()}, (ResultCallback)null);
        return true;
      }
    }).build();
    this.addMessageItemLongClickAction(messageItemLongClickAction);
  }

  private String getPushContent(Context context, UIMessage message) {
    String userName = "";
    UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());
    if (userInfo != null) {
      userName = userInfo.getName();
    }

    return context.getString(R.string.rc_user_recalled_message, new Object[]{userName});
  }

  private static class Holder {
    static io.rong.imkit.RongMessageItemLongClickActionManager instance = new io.rong.imkit.RongMessageItemLongClickActionManager();

    private Holder() {
    }
  }
}
