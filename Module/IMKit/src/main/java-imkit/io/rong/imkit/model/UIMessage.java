//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.model;

import android.text.SpannableStringBuilder;

import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imlib.CustomServiceConfig;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.Message.MessageDirection;
import io.rong.imlib.model.Message.ReceivedStatus;
import io.rong.imlib.model.Message.SentStatus;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.ReadReceiptInfo;
import io.rong.imlib.model.UserInfo;
import io.rong.message.TextMessage;

public class UIMessage {
    private SpannableStringBuilder textMessageContent;
    private UserInfo mUserInfo;
    private int mProgress;
    private boolean evaluated = false;
    private boolean isHistoryMessage = true;
    private Message mMessage;
    private boolean mNickName;
    private boolean isListening;
    public boolean continuePlayAudio;
    private CustomServiceConfig csConfig;
    private boolean isChecked;

    public UIMessage() {
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }

    public boolean isListening() {
        return this.isListening;
    }

    public void setListening(boolean listening) {
        this.isListening = listening;
    }

    public boolean isNickName() {
        return this.mNickName;
    }

    public void setNickName(boolean nickName) {
        this.mNickName = nickName;
    }

    public Message getMessage() {
        return this.mMessage;
    }

    public void setMessage(Message message) {
        this.mMessage = message;
    }

    public void setReceivedStatus(ReceivedStatus receivedStatus) {
        this.mMessage.setReceivedStatus(receivedStatus);
    }

    public void setSentStatus(SentStatus sentStatus) {
        this.mMessage.setSentStatus(sentStatus);
    }

    public void setReceivedTime(long receivedTime) {
        this.mMessage.setReceivedTime(receivedTime);
    }

    public void setSentTime(long sentTime) {
        this.mMessage.setSentTime(sentTime);
    }

    public void setContent(MessageContent content) {
        this.mMessage.setContent(content);
    }

    public void setExtra(String extra) {
        this.mMessage.setExtra(extra);
    }

    public void setSenderUserId(String senderUserId) {
        this.mMessage.setSenderUserId(senderUserId);
    }

    public void setCsConfig(CustomServiceConfig csConfig) {
        this.csConfig = csConfig;
    }

    public String getUId() {
        return this.mMessage.getUId();
    }

    public ConversationType getConversationType() {
        return this.mMessage.getConversationType();
    }

    public String getTargetId() {
        return this.mMessage.getTargetId();
    }

    public int getMessageId() {
        return this.mMessage.getMessageId();
    }

    public MessageDirection getMessageDirection() {
        return this.mMessage.getMessageDirection();
    }

    public String getSenderUserId() {
        return this.mMessage.getSenderUserId();
    }

    public ReceivedStatus getReceivedStatus() {
        return this.mMessage.getReceivedStatus();
    }

    public SentStatus getSentStatus() {
        return this.mMessage.getSentStatus();
    }

    public long getReceivedTime() {
        return this.mMessage.getReceivedTime();
    }

    public long getSentTime() {
        return this.mMessage.getSentTime();
    }

    public String getObjectName() {
        return this.mMessage.getObjectName();
    }

    public MessageContent getContent() {
        return this.mMessage.getContent();
    }

    public String getExtra() {
        return this.mMessage.getExtra();
    }

    public CustomServiceConfig getCsConfig() {
        return this.csConfig;
    }

    public static io.rong.imkit.model.UIMessage obtain(Message message) {
        io.rong.imkit.model.UIMessage uiMessage = new io.rong.imkit.model.UIMessage();
        uiMessage.mMessage = message;
        uiMessage.continuePlayAudio = false;
        return uiMessage;
    }

    public SpannableStringBuilder getTextMessageContent() {
        if (this.textMessageContent == null) {
            MessageContent content = this.mMessage.getContent();
            if (content instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) content;
                if (textMessage.getContent() != null) {
                    SpannableStringBuilder spannable = new SpannableStringBuilder(textMessage.getContent());
                    SpannableStringBuilder spannable1 = AndroidEmoji.replaceEmojiWithText(spannable);
                    AndroidEmoji.ensure(spannable1);
                    this.setTextMessageContent(spannable1);
                }
            }
        }

        return this.textMessageContent;
    }

    public ReadReceiptInfo getReadReceiptInfo() {
        return this.mMessage.getReadReceiptInfo();
    }

    public void setReadReceiptInfo(ReadReceiptInfo info) {
        this.mMessage.setReadReceiptInfo(info);
    }

    public void setTextMessageContent(SpannableStringBuilder textMessageContent) {
        this.textMessageContent = textMessageContent;
    }

    public UserInfo getUserInfo() {
        return this.mUserInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        if (userInfo != null && this.mMessage != null && this.mMessage.getSenderUserId() != null) {
            if (this.mMessage.getSenderUserId().equals(userInfo.getUserId())) {
                this.mUserInfo = userInfo;
            }
        } else {
            this.mUserInfo = userInfo;
        }

    }

    public void setProgress(int progress) {
        this.mProgress = progress;
    }

    public int getProgress() {
        return this.mProgress;
    }

    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }

    public boolean getEvaluated() {
        return this.evaluated;
    }

    public void setIsHistoryMessage(boolean isHistoryMessage) {
        this.isHistoryMessage = isHistoryMessage;
    }

    public boolean getIsHistoryMessage() {
        return this.isHistoryMessage;
    }
}
